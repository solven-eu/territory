package eu.solven.territory.snake.v0_only_snake;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.snake.Apple;
import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.ISnakeMarkers;
import eu.solven.territory.snake.ISnakeWorldItem;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeInRectangleOccupation;
import eu.solven.territory.snake.SnakeTurnContext;
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

	public GameOfSnake(SquareMap map) {
		this.map = map;
	}

	@Override
	public IWorldOccupation<ISnakeWorldItem> cycle(IWorldOccupation<ISnakeWorldItem> occupation) {
		IWorldOccupation<ISnakeWorldItem> rawCopy = occupation.mutableCopy();

		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow<ISnakeWorldItem> windowBuffer = occupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		SnakeTurnContext context = buildContext(occupation, windowBuffer);

		if (rawCopy instanceof SnakeInRectangleOccupation snakeCopy) {
			int nbApples = context.getOccupiedByApple().size();

			int worldSize = map.size();

			// 1 apple plus one apple per 10x10 blocks
			int targetNbApples = 1 + worldSize / (10 * 10);

			int missingApples = Math.max(0, targetNbApples - nbApples);

			if (missingApples > 0) {
				// Apples can pop anywhere, even under the snake
				ICellPosition randomPosition = map.randomPosition();
				snakeCopy.setValue(randomPosition, new Apple());
			}

			// Process the head
			occupation.forEachLiveCell(ISnakeMarkers.IsSnake.class, windowBuffer, position -> {
				SnakeCell currentHead = (SnakeCell) windowBuffer.getCenter();
				// assert currentHead.getCellIndex() == SNAKE;

				if (!currentHead.isHead()) {
					return;
				}

				TwoDimensionPosition newHeadPosition = null;
				int newDirection = -1;

				for (int optDirection : new int[] {
						// Try moving forward
						currentHead.getDirection(),
						// Can not move forward: try turning left or right
						turnLeft(currentHead),
						// Left is also outOfWorld, let's turn right
						turnRight(currentHead) }) {
					TwoDimensionPosition optNextHead = nextHead(position, optDirection);

					if (canBeNextHead(context, optNextHead)) {
						newHeadPosition = optNextHead;
						newDirection = optDirection;

						break;
					}
				}

				if (newDirection == -1) {
					// Losing its tail, until length==1 shall unlock the Snake, except if world is size== 1
					LOGGER.info("Snake can not move: it loses its tail");
					currentHead.getWhole().loseTail();
				} else {
					if (context.isApple(newHeadPosition)) {
						snakeCopy.appleConsumed(newHeadPosition);
						snakeCopy.eatApple();
					}

					snakeCopy.newHead(currentHead, newDirection);
					snakeCopy.headPosition(newHeadPosition);
				}
			});
		}

		return rawCopy;
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

	protected boolean canBeNextHead(SnakeTurnContext context, TwoDimensionPosition position) {
		Set<ICellPosition> occupiedBySnake = context.getOccupiedBySnake();

		return !isOutOfWorld(position) && !occupiedBySnake.contains(position);
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

	private boolean isOutOfWorld(TwoDimensionPosition optNextHead) {
		if (optNextHead.getX() < 0 || optNextHead.getX() >= map.getWidth()) {
			return true;
		} else if (optNextHead.getY() < 0 || optNextHead.getY() >= map.getHeight()) {
			return true;
		} else {
			return false;
		}
	}

}
