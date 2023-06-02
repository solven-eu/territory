package eu.solven.territory.snake;

import java.util.HashSet;
import java.util.Set;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IPlayerOccupation;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * The simplest Snake game.
 * 
 * @author Benoit Lacelle
 *
 */
// https://fr.wikipedia.org/wiki/Snake_(genre_de_jeu_vid%C3%A9o)
public class GameOfSnake implements IExpansionCycleRule<SnakeCell> {

	public static final int OFF_WORLD = -1;
	public static final int DEAD = 0;
	public static final int SNAKE = 1;
	public static final int APPLE = 2;

	final SquareMap map;

	public GameOfSnake(SquareMap map) {
		this.map = map;
	}

	@Override
	public IPlayerOccupation<SnakeCell> cycle(IPlayerOccupation<SnakeCell> occupation) {
		IPlayerOccupation<SnakeCell> rawCopy = occupation.mutableCopy();

		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow<SnakeCell> windowBuffer = occupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		// Collect Snake cells. Useful to prevent the snake eating itself
		Set<ICellPosition> occupiedBySnake = new HashSet<>();
		occupation.forEachLiveCell(ISnakeMarkers.Snake.class, windowBuffer, position -> {
			occupiedBySnake.add(position);
		});
		if (rawCopy instanceof SnakeInRectangleOccupation snakeCopy) {

			// Process the head
			occupation.forEachLiveCell(ISnakeMarkers.Snake.class, windowBuffer, position -> {
				SnakeCell currentHead = windowBuffer.getCenter();
				assert currentHead.getCellIndex() == SNAKE;

				if (!currentHead.isHead()) {
					return;
				}

				TwoDimensionPosition newHeadPosition;

				{
					TwoDimensionPosition optNextHead = nextHead(position, currentHead.getDirection());

					if (canBeNextHead(occupiedBySnake, optNextHead)) {
						newHeadPosition = optNextHead;
						snakeCopy.newHead(currentHead, currentHead.getDirection());
					} else {
						// TODO We lose
						// Or we turn left
						int turnedLeftDir = turnLeft(currentHead);
						TwoDimensionPosition headTurnedLeft = nextHead(position, turnedLeftDir);

						if (canBeNextHead(occupiedBySnake, headTurnedLeft)) {
							newHeadPosition = headTurnedLeft;
							snakeCopy.newHead(currentHead, turnedLeftDir);
						} else {
							// Left is also outOfWorld, let's turn right
							int turnedRightDir = turnRight(currentHead);
							TwoDimensionPosition headTurnedRight = nextHead(position, turnedRightDir);
							newHeadPosition = headTurnedRight;
							snakeCopy.newHead(currentHead, turnedRightDir);
						}
					}
				}

				snakeCopy.headPosition(newHeadPosition);
			});
		}

		return rawCopy;
	}

	public static int turnRight(SnakeCell head) {
		return (head.getDirection() + 4 - 1) % 4;
	}

	public static int turnLeft(SnakeCell head) {
		return (head.getDirection() + 1) % 4;
	}

	public static int behind(SnakeCell head) {
		return (head.getDirection() + 2) % 4;
	}

	private boolean canBeNextHead(Set<ICellPosition> occupiedBySnake, TwoDimensionPosition position) {
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
