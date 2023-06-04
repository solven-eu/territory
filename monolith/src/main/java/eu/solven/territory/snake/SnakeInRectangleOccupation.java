package eu.solven.territory.snake;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.solven.territory.ICellMarker;
import eu.solven.territory.ICellPosition;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.snake.ISnakeMarkers.IsApple;
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
public class SnakeInRectangleOccupation implements IWorldOccupation<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SnakeInRectangleOccupation.class);

	final IIsRectangle map;
	final WholeSnake snake;

	final AtomicReference<TwoDimensionPosition> refHeadPosition;

	final Set<TwoDimensionPosition> apples = new HashSet<>();

	public SnakeInRectangleOccupation(IIsRectangle map, WholeSnake snake, TwoDimensionPosition headPosition) {
		this.map = map;
		this.snake = snake;

		this.refHeadPosition = new AtomicReference<TwoDimensionPosition>(headPosition);
	}

	public SnakeInRectangleOccupation(IIsRectangle map,
			WholeSnake snake,
			TwoDimensionPosition headPosition,
			Set<TwoDimensionPosition> apples) {
		this.map = map;
		this.snake = snake;

		this.refHeadPosition = new AtomicReference<TwoDimensionPosition>(headPosition);
		this.apples.addAll(apples);
	}

	public static <S extends ISnakeWorldItem> SnakeInRectangleOccupation baby(SquareMap map,
			TwoDimensionPosition headPosition,
			WholeSnake baby) {
		if (!map.isRectangleLike()) {
			throw new IllegalArgumentException("!rectangle");
		}

		return new SnakeInRectangleOccupation(map, baby, headPosition);
	}

	@Override
	public IWorldOccupation<ISnakeWorldItem> mutableCopy() {
		return new SnakeInRectangleOccupation(map, snake.copy(), refHeadPosition.get(), apples);
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
			{
				TwoDimensionPosition cellPosition = refHeadPosition.get();

				for (ISnakeCell currentCell : snake.getCells()) {
					if (marker.isAssignableFrom(currentCell.getClass())) {
						fillAndCheckCallback(marker, cellPositionConsumer, rectangleWindow, cellPosition);
					}

					// Walk-back the snake
					cellPosition = GameOfSnake.nextHead(cellPosition, GameOfSnake.behind(currentCell));
				}
			}

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

		TwoDimensionPosition headPosition = refHeadPosition.get();

		for (ISnakeCell currentCell : snake.getCells()) {
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
					// We set back previous value, as the apple is considered as not eadable yet, hence it can be kind
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

	public void headPosition(TwoDimensionPosition newHeadPosition) {
		refHeadPosition.set(newHeadPosition);
	}

	public void newHead(ISnakeCell head, int direction) {
		snake.getHead().newHead(direction);

		if (snake.getCells().stream().filter(c -> c.isHead()).count() != 1) {
			throw new IllegalArgumentException("We have multiple heads");
		}
	}

	public void appleConsumed(TwoDimensionPosition newHeadPosition) {
		if (!apples.remove(newHeadPosition)) {
			throw new IllegalStateException("Can not consumer inexistant apple");
		}
	}

	public WholeSnake getSnake() {
		return snake;
	}
}
