package eu.solven.territory.snake.strategies.v2_multiplesnakes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.solven.territory.ICellMarker;
import eu.solven.territory.ICellPosition;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.snake.Apple;
import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.ISnakeMarkers.IsApple;
import eu.solven.territory.snake.ISnakeWorldItem;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.two_dimensions.IIsRectangle;
import eu.solven.territory.two_dimensions.RectangleWindow;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * A basic Rectangle {@link IWorldOccupation}.
 * 
 * `0` means empty.
 * 
 * @author Benoit Lacelle
 *
 */
public class MultipleSnakeInRectangleOccupation implements IWorldOccupation<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultipleSnakeInRectangleOccupation.class);

	final IIsRectangle map;

	final Map<Object, PositionnedSnake> positionnedSnakes;

	final Set<TwoDimensionPosition> apples = new HashSet<>();

	public MultipleSnakeInRectangleOccupation(IIsRectangle map, WholeSnake snake, TwoDimensionPosition headPosition) {
		this.map = map;

		this.positionnedSnakes = Map.of(snake.getId(), new PositionnedSnake(snake, headPosition));
	}

	public MultipleSnakeInRectangleOccupation(IIsRectangle map, PositionnedSnake... snakes) {
		this.map = map;
		this.positionnedSnakes = Stream.of(snakes).collect(Collectors.toMap(e -> e.getSnake().getId(), e -> e));

		this.apples.addAll(apples);
	}

	private MultipleSnakeInRectangleOccupation(IIsRectangle map,
			Map<Object, PositionnedSnake> positionnedSnakes,
			Set<TwoDimensionPosition> apples) {
		this.map = map;
		this.positionnedSnakes = positionnedSnakes;

		this.apples.addAll(apples);
	}

	public static <S extends ISnakeWorldItem> MultipleSnakeInRectangleOccupation baby(SquareMap map,
			TwoDimensionPosition headPosition,
			WholeSnake baby) {
		if (!map.isRectangleLike()) {
			throw new IllegalArgumentException("!rectangle");
		}

		return new MultipleSnakeInRectangleOccupation(map, baby, headPosition);
	}

	@Override
	public IWorldOccupation<ISnakeWorldItem> mutableCopy() {
		return new MultipleSnakeInRectangleOccupation(map,
				positionnedSnakes.entrySet()
						.stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().copy())),
				apples);
	}

	@Override
	public IMapWindow<ISnakeWorldItem> makeWindowBuffer(int radius) {
		return RectangleWindow.empty(radius, radius);
	}

	@Override
	public void forEachLiveCell(Class<? extends ICellMarker> marker,
			IMapWindow<ISnakeWorldItem> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		if (windowBuffer instanceof RectangleWindow<ISnakeWorldItem> rectangleWindow) {
			// for each snake cell
			positionnedSnakes.values().forEach(snake -> {
				TwoDimensionPosition cellPosition = snake.getHeadPosition();

				for (ISnakeCell currentCell : snake.getSnake().getCells()) {
					if (marker.isAssignableFrom(currentCell.getClass())) {
						fillAndCheckCallback(marker, cellPositionConsumer, rectangleWindow, cellPosition);
					}

					// Walk-back the snake
					cellPosition = GameOfSnake.nextHead(cellPosition, GameOfSnake.behind(currentCell));
				}
			});

			// for each apple
			for (TwoDimensionPosition cellPosition : apples) {
				if (marker.isAssignableFrom(Apple.class)) {
					fillAndCheckCallback(marker, cellPositionConsumer, rectangleWindow, cellPosition);
				}
			}
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	private void fillAndCheckCallback(Class<? extends ICellMarker> marker,
			Consumer<ICellPosition> cellPositionConsumer,
			RectangleWindow<ISnakeWorldItem> rectangleWindow,
			TwoDimensionPosition cellPosition) {
		fill(cellPosition, rectangleWindow);

		// We check for the actual center-type, as there may be collisions (e.g. an apple popping under
		// the snake)
		if (marker.isAssignableFrom(rectangleWindow.getCenter().getClass())) {
			cellPositionConsumer.accept(cellPosition);
		}
	}

	@Override
	public void forEachDeadButNearLiveCell(Class<? extends ICellMarker> marker,
			int radius,
			IMapWindow<ISnakeWorldItem> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		Set<ICellPosition> live = new HashSet<>();
		Set<ICellPosition> nearLive = new HashSet<>();

		// We assume there is much less live than dead cells
		// Then, to find dead-near-live cells, we iterate around live cells to later iterate over this neighborhood,
		// excluding the live cells
		forEachLiveCell(marker, windowBuffer, cellPosition -> {
			live.add(cellPosition);

			windowBuffer.forEachCell(c -> nearLive.add(c.shift(cellPosition)));
		});

		if (windowBuffer instanceof RectangleWindow<ISnakeWorldItem> rectangleWindow) {
			Sets.difference(nearLive, live).stream().map(TwoDimensionPosition.class::cast).forEach(deadButNearLive -> {
				fill(deadButNearLive, rectangleWindow);
				cellPositionConsumer.accept(deadButNearLive);
			});
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	private void fill(TwoDimensionPosition position, RectangleWindow<ISnakeWorldItem> windowBuffer) {
		// Reset, as we will write only live positions
		windowBuffer.reset();

		int x = position.getX();
		int y = position.getY();

		windowBuffer.forEachCoordinate((shiftX, shiftY) -> {
			int shiftedX = x + shiftX;
			int shiftedY = y + shiftY;

			if (isOutOfWorld(map, shiftedX, shiftedY)) {
				windowBuffer.setOffWorld(shiftX, shiftY);
			}
		});

		positionnedSnakes.values().forEach(snake -> {
			TwoDimensionPosition headPosition = snake.getHeadPosition();

			for (ISnakeCell currentCell : snake.getSnake().getCells()) {
				TwoDimensionPosition relativePosition = position.back(headPosition);

				int shiftX = relativePosition.getX();
				int shiftY = relativePosition.getY();

				if (isOutOfWorldCentered(windowBuffer, shiftX, shiftY)) {
					// This part of the snake is out of the window, but it may later come back into it
				} else {
					ISnakeWorldItem previous = windowBuffer.setValue(shiftX, shiftY, currentCell);
					if (previous != null) {
						// LOGGER.warn("Snake is walking itself")
						throw new IllegalStateException(
								"A cell is being occupied by both " + previous + " and " + currentCell);
					}
				}

				headPosition = GameOfSnake.nextHead(headPosition, GameOfSnake.behind(currentCell));
			}
		});

		apples.forEach(applePosition -> {
			TwoDimensionPosition relativePosition = position.back(applePosition);

			int shiftX = relativePosition.getX();
			int shiftY = relativePosition.getY();

			if (isOutOfWorldCentered(windowBuffer, shiftX, shiftY)) {
				// This part of the snake is out of the window, but it may later come back into it
			} else {
				Apple apple = new Apple();
				ISnakeWorldItem previous = windowBuffer.setValue(shiftX, shiftY, apple);
				if (previous != null) {
					// LOGGER.warn("An apple is under the snake");
					// We set back previous value, as the apple is considered as not edible yet, hence it can be kind
					// of hidden
					// windowBuffer.setValue(shiftX, shiftY, previous);
					throw new IllegalStateException("A cell is being occupied by both " + previous + " and " + apple);
				}
			}
		});
	}

	public static boolean isOutOfWorld(IIsRectangle world, int x, int y) {
		return x < 0 || x >= world.getWidth() || y < 0 || y >= world.getHeight();
	}

	public static boolean isOutOfWorldCentered(RectangleWindow<?> world, int x, int y) {
		int halfWidth = RectangleWindow.fullToHalf(world.getWidth());
		if (Math.abs(x) > halfWidth) {
			return true;
		}

		int halfHeight = RectangleWindow.fullToHalf(world.getHeight());
		if (Math.abs(y) > halfHeight) {
			return true;
		}

		return false;
	}

	@Override
	public void setValue(ICellPosition position, ISnakeWorldItem cellValue) {
		if (cellValue instanceof IsApple) {
			apples.add((TwoDimensionPosition) position);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void setDead(ICellPosition position) {
		throw new UnsupportedOperationException();
	}

	public void headPosition(ISnakeCell head, TwoDimensionPosition newHeadPosition) {
		getPositionnedHead(head).setPosition(newHeadPosition);
	}

	public void newHead(ISnakeCell head, int direction) {
		ISnakeCell copyHead = getHead(head);

		copyHead.newHead(direction);

		if (copyHead.getWhole().getCells().stream().filter(c -> c.isHead()).count() != 1) {
			throw new IllegalArgumentException("We have multiple heads");
		}
	}

	public void appleConsumed(TwoDimensionPosition newHeadPosition) {
		if (!apples.remove(newHeadPosition)) {
			throw new IllegalStateException("Can not consumer inexistant apple");
		}
	}

	public ISnakeCell getHead(ISnakeCell currentHead) {
		return getPositionnedHead(currentHead).getSnake().getHead();
	}

	private PositionnedSnake getPositionnedHead(ISnakeCell currentHead) {
		return positionnedSnakes.get(currentHead.getWhole().getId());
	}

	public void snakeEaten(TwoDimensionPosition newHeadPosition) {
		positionnedSnakes.values().forEach(snake -> {
			TwoDimensionPosition headPosition = snake.getHeadPosition();

			// boolean eaten = false;

			for (ISnakeCell currentCell : snake.getSnake().getCells()) {
				if (headPosition.equals(newHeadPosition)) {
					// Current snake and current snakeCell are the eaten one
					// eaten = true;

					while (true) {
						ISnakeCell previousTail = snake.getSnake().loseTail();

						if (previousTail.equals(currentCell)) {
							// We have lost all cells between the eaten one and the tail
							break;
						} else {
							// The tail was not yet the eater cell
							LOGGER.debug("There is more dead snakeCells");
						}
					}

					// No need to iterate through more snakes, as only one snake can live on a given cell
					break;
				}

				// if (eaten) {
				//
				// }

				headPosition = GameOfSnake.nextHead(headPosition, GameOfSnake.behind(currentCell));
			}
		});
	}
}
