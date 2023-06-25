package eu.solven.territory.snake.v0_only_snake;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.snake.strategies.v1_cansmell.ICanSmell;
import eu.solven.territory.snake.strategies.v2_multiplesnakes.MultipleSnakeInRectangleOccupation;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * The simplest Snake game.
 * 
 * @author Benoit Lacelle
 *
 */
// https://fr.wikipedia.org/wiki/Snake_(genre_de_jeu_vid%C3%A9o)
public class GameOfSnake_Multiple implements IExpansionCycleRule<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameOfSnake_Multiple.class);

	public static final int OFF_WORLD = -1;
	public static final int DEAD = 0;
	public static final int SNAKE = 1;
	public static final int APPLE = 2;

	final SquareMap map;

	final AtomicInteger cycleIndex = new AtomicInteger();

	public GameOfSnake_Multiple(SquareMap map) {
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

	/**
	 * We cycle through snakes to let the world progress. If the world holds multiple snakes, we need to be careful, as
	 * two snakes may be moving to the same cell. It seems simpler to process each snake one at a time, to prevent snake
	 * believing they are moving both to an empty cell.
	 * 
	 * @param occupation
	 * @return
	 */
	private IWorldOccupation<ISnakeWorldItem> rawCycle(IWorldOccupation<ISnakeWorldItem> initialOccupation) {
		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow<ISnakeWorldItem> windowBuffer =
				initialOccupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		// A world cycle allows each snake to move once
		Set<Object> snakeHavingPlayed = new HashSet<>();

		IWorldOccupation<ISnakeWorldItem> rawWorldCopy = initialOccupation;
		while (true) {
			AtomicBoolean oneSnakeMoved = new AtomicBoolean();

			IWorldOccupation<ISnakeWorldItem> previousSubCycle = rawWorldCopy;
			rawWorldCopy = rawWorldCopy.mutableCopy();

			SnakeTurnContext context = buildContext(previousSubCycle, windowBuffer);

			if (rawWorldCopy instanceof MultipleSnakeInRectangleOccupation worldCopy) {
				growApples(context, worldCopy);

				// Process each snake heads
				previousSubCycle.forEachLiveCell(ISnakeMarkers.IsSnake.class, windowBuffer, position -> {
					if (oneSnakeMoved.get()) {
						// We allow a single snake to move for per sub-cycle
						return;
					}

					SnakeCell currentHead = (SnakeCell) windowBuffer.getCenter();
					if (!currentHead.isHead()) {
						return;
					}

					if (snakeHavingPlayed.add(currentHead.getWhole().getId())) {
						oneSnakeMoved.set(true);
						snakeAction(context, worldCopy, position, currentHead);
					} else {
						// This snake already moved in this cycle
					}

				});
			}

			if (!oneSnakeMoved.get()) {
				// No snake moved in this new sub-cycle: all snake have moved in this cycle
				break;
			}
		}

		if (rawWorldCopy == initialOccupation) {
			LOGGER.warn("The world did not change, meaning it would not change ever. Is it a dead world?");
		}

		return rawWorldCopy;
	}

	private void snakeAction(SnakeTurnContext context,
			MultipleSnakeInRectangleOccupation worldCopy,
			ICellPosition position,
			SnakeCell currentHead) {
		IDirectionPicker directionPicker = currentHead.getWhole().getDirectionPicker();

		int newDirection = directionPicker.pickDirection(map, context, position, currentHead);

		ISnakeCell copyHead = worldCopy.getHead(currentHead);
		WholeSnake copySnake = copyHead.getWhole();

		TwoDimensionPosition newHeadPosition;
		if (newDirection == IDirectionPicker.NO_DIRECTION) {
			newHeadPosition = (TwoDimensionPosition) position;

			// Losing its tail, until length==1 shall unlock the Snake, except if world is size== 1
			LOGGER.info(
					"Snake can not move: it loses weight (potentially losing its tail, potentially freeing the way");
			copySnake.loseWeight();
		} else {
			newHeadPosition = nextHead(position, newDirection);

			if (context.isApple(newHeadPosition)) {
				worldCopy.appleConsumed(newHeadPosition);
				copySnake.eatSomething();
			} else if (context.getOccupiedBySnake().contains(newHeadPosition)) {
				// The eater snake grows anyway (even if eating itself)
				copySnake.eatSomething();
				// The eaten snake may be itself, or another snake
				worldCopy.snakeEaten(newHeadPosition);
			} else {
				copySnake.loseWeight();
			}

			worldCopy.newHead(currentHead, newDirection);
			worldCopy.headPosition(currentHead, newHeadPosition);
		}

		if (copySnake instanceof ICanSmell canSmell) {
			double distance = distance(newHeadPosition, context.getOccupiedByApple());

			canSmell.smells(distance);
		}
	}

	private void growApples(SnakeTurnContext context, MultipleSnakeInRectangleOccupation snakeCopy) {
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

	public static boolean canBeNextHead(ITerritoryMap map, SnakeTurnContext context, TwoDimensionPosition position) {
		Set<ICellPosition> occupiedBySnake = context.getOccupiedBySnake();

		return !map.isOutOfWorld(position) && !occupiedBySnake.contains(position);
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
