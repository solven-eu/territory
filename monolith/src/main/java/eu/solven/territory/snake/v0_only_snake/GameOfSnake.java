package eu.solven.territory.snake.v0_only_snake;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.snake.Apple;
import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.ISnakeMarkers;
import eu.solven.territory.snake.ISnakeWorldItem;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeInRectangleOccupation;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.strategies.v1_cansmell.ICanSmell;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * The simplest Snake game.
 * 
 * @author Benoit Lacelle
 *
 */
// https://fr.wikipedia.org/wiki/Snake_(genre_de_jeu_vid%C3%A9o)
public class GameOfSnake implements IExpansionCycleRule<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameOfSnake.class);

	public static final int OFF_WORLD = -1;
	public static final int DEAD = 0;
	public static final int SNAKE = 1;
	public static final int APPLE = 2;

	final SquareMap map;

	final AtomicInteger cycleIndex = new AtomicInteger();

	public GameOfSnake(SquareMap map) {
		this.map = map;
	}

	@Override
	public IWorldOccupation<ISnakeWorldItem> cycle(IWorldOccupation<ISnakeWorldItem> occupation) {
		int cyrrentCycle = cycleIndex.getAndIncrement();

		IWorldOccupation<ISnakeWorldItem> rawCopy = rawCycle(occupation);

		LOGGER.debug("Done with {}", cyrrentCycle);

		// We compute ahead of time the next cycle. It will make it easier to see previous state given a new error
		rawCycle(rawCopy);

		return rawCopy;
	}

	private IWorldOccupation<ISnakeWorldItem> rawCycle(IWorldOccupation<ISnakeWorldItem> occupation) {
		IWorldOccupation<ISnakeWorldItem> rawCopy = occupation.mutableCopy();

		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow<ISnakeWorldItem> windowBuffer = occupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		SnakeTurnContext context = buildContext(occupation, windowBuffer);

		if (rawCopy instanceof SnakeInRectangleOccupation snakeCopy) {
			growApples(context, snakeCopy);

			// Process each snake heads
			occupation.forEachLiveCell(ISnakeMarkers.IsSnake.class, windowBuffer, position -> {
				SnakeCell currentHead = (SnakeCell) windowBuffer.getCenter();
				if (!currentHead.isHead()) {
					return;
				}

				snakeAction(context, snakeCopy, position, currentHead);
			});
		}

		return rawCopy;
	}

	private void snakeAction(SnakeTurnContext context,
			SnakeInRectangleOccupation snakeCopy,
			ICellPosition position,
			SnakeCell currentHead) {
		IDirectionPicker directionPicker = currentHead.getWhole().getDirectionPicker();

		int newDirection = directionPicker.pickDirection(map, context, position, currentHead);

		TwoDimensionPosition newHeadPosition;
		if (newDirection == IDirectionPicker.NO_DIRECTION) {
			newHeadPosition = (TwoDimensionPosition) position;

			// Losing its tail, until length==1 shall unlock the Snake, except if world is size== 1
			LOGGER.info(
					"Snake can not move: it loses weight (potentially losing its tail, potentially freeing the way");
			snakeCopy.getSnake().spendEnergy();
		} else {
			newHeadPosition = nextHead(position, newDirection);

			if (context.isApple(newHeadPosition)) {
				snakeCopy.appleConsumed(newHeadPosition);
				snakeCopy.getSnake().eatSomething();
			} else {
				snakeCopy.getSnake().spendEnergy();
			}

			snakeCopy.newHead(currentHead, newDirection);
			snakeCopy.headPosition(newHeadPosition);
		}

		if (snakeCopy.getSnake() instanceof ICanSmell canSmell) {
			double distance = distance(newHeadPosition, context.getOccupiedByApple());

			canSmell.smells(distance);
		}
	}

	private void growApples(SnakeTurnContext context, SnakeInRectangleOccupation snakeCopy) {
		int nbApples = context.getOccupiedByApple().size();

		int worldSize = map.size();

		// 1 apple plus one apple per 10x10 blocks
		int targetNbApples = 1 + worldSize / (10 * 10);

		int missingApples = Math.max(0, targetNbApples - nbApples);

		if (missingApples > 0) {
			// Apples can pop anywhere, even under the snake
			ICellPosition randomPosition = map.randomPosition();

			if (context.getOccupiedBySnake().contains(randomPosition)) {
				LOGGER.debug("We reject registration of an apple as the cell is occupied");
			} else {
				snakeCopy.setValue(randomPosition, new Apple());
			}

		}
	}

	// We consider the distance smelt is the distance to the nearest item
	private double distance(ICellPosition position, Set<ICellPosition> occupiedByApple) {
		if (position instanceof TwoDimensionPosition twoD) {
			return occupiedByApple.stream()
					.map(c -> (TwoDimensionPosition) c)
					.mapToDouble(c -> Math.pow(c.getX() - twoD.getX(), 2D) + Math.pow(c.getY() - twoD.getY(), 2D))
					.min()
					.orElse(Double.MAX_VALUE);
		} else {
			return Double.MAX_VALUE;
		}
	}

	private SnakeTurnContext buildContext(IWorldOccupation<ISnakeWorldItem> occupation,
			IMapWindow<ISnakeWorldItem> windowBuffer) {
		// Collect Snake cells. Useful to prevent the snake eating itself
		Set<ICellPosition> occupiedBySnake = new HashSet<>();
		occupation
				.forEachLiveCell(ISnakeMarkers.IsSnake.class, windowBuffer, position -> occupiedBySnake.add(position));

		Set<ICellPosition> occupiedByApple = new HashSet<>();
		occupation
				.forEachLiveCell(ISnakeMarkers.IsApple.class, windowBuffer, position -> occupiedByApple.add(position));

		return new SnakeTurnContext(occupiedBySnake, occupiedByApple);
	}

	public static int turnRight(ISnakeCell head) {
		return (head.getDirection() + 4 - 1) % 4;
	}

	public static int turnLeft(ISnakeCell head) {
		return (head.getDirection() + 1) % 4;
	}

	public static int behind(ISnakeCell head) {
		return (head.getDirection() + 2) % 4;
	}

	public static TwoDimensionPosition nextHead(ICellPosition position, int direction) {
		TwoDimensionPosition shift;
		switch (direction) {
		case 0:
			shift = new TwoDimensionPosition(1, 0);
			break;
		case 1:
			shift = new TwoDimensionPosition(0, 1);
			break;
		case 2:
			shift = new TwoDimensionPosition(-1, 0);
			break;
		case 3:
			shift = new TwoDimensionPosition(0, -1);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + direction);
		}

		return (TwoDimensionPosition) position.shift(shift);
	}

}
