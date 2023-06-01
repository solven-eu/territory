package eu.solven.territory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * A basic Rectangle {@link IPlayerOccupation}.
 * 
 * `0` means empty.
 * 
 * @author Benoit Lacelle
 *
 */
public class RectangleOccupation implements IPlayerOccupation {
	private static final Logger LOGGER = LoggerFactory.getLogger(RectangleOccupation.class);

	final IIsRectangle map;
	// [0 -> width] holds the first/bottom row
	final int[] raw;

	public RectangleOccupation(IIsRectangle map, int[] raw) {
		this.map = map;
		this.raw = raw;
	}

	public static RectangleOccupation empty(SquareMap map) {
		if (!map.isRectangleLike()) {
			throw new IllegalArgumentException("!rectangle");
		}

		SquareMap squareMap = (SquareMap) map;
		int[] asArray = new int[squareMap.getWidth() * squareMap.getHeight()];

		return new RectangleOccupation(map, asArray);
	}

	@Override
	public IPlayerOccupation mutableCopy() {
		return new RectangleOccupation(map, raw.clone());
	}

	@Override
	public IMapWindow makeWindowBuffer(int radius) {
		return RectangleWindow.empty(radius, radius);
	}

	@Override
	public void forEachLiveCell(IMapWindow windowBuffer, Consumer<ICellPosition> cellPositionConsumer) {
		if (windowBuffer instanceof RectangleWindow rectangleWindow) {
			for (int x = 0; x < map.getWidth(); x++) {
				for (int y = 0; y < map.getHeight(); y++) {
					int oneDimensionalIndex = x + map.getWidth() * y;
					if (raw[oneDimensionalIndex] == GameOfLife.LIVE) {
						TwoDimensionPosition position = new TwoDimensionPosition(x, y);
						fill(position, rectangleWindow);
						cellPositionConsumer.accept(position);
					}

				}
			}
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	@Override
	public void forEachDeadButNearLiveCell(int radius,
			IMapWindow windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		// assert windowBuffer.getRadius() >= radius
		
		Set<ICellPosition> live = new HashSet<>();
		Set<ICellPosition> nearLive = new HashSet<>();

		// We assume there is much less live than dead cells
		// Then, to find dead-near-live cells, we iterate around live cells to later iterate over this neighborhood,
		// excluding the live cells
		forEachLiveCell(windowBuffer, cellPosition -> {
			live.add(cellPosition);

			windowBuffer.forEachCell(c -> nearLive.add(c.shift(cellPosition)));
		});

		if (windowBuffer instanceof RectangleWindow rectangleWindow) {
			Sets.difference(nearLive, live).stream().map(TwoDimensionPosition.class::cast).forEach(deadButNearLive -> {
				fill(deadButNearLive, rectangleWindow);
				cellPositionConsumer.accept(deadButNearLive);
			});
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	private void fill(TwoDimensionPosition position, RectangleWindow windowBuffer) {
		int x = position.getX();
		int y = position.getY();

		windowBuffer.forEachCoordinate((shiftX, shiftY) -> {
			int shiftedX = x + shiftX;
			int shiftedY = y + shiftY;

			if (shiftedX < 0 || shiftedX >= map.getWidth() || shiftedY < 0 || shiftedY >= map.getHeight()) {
				windowBuffer.setValue(shiftX, shiftY, GameOfLife.OFF_WORLD);
			} else {
				windowBuffer.setValue(shiftX, shiftY, raw[shiftedX + map.getWidth() * shiftedY]);
			}

		});

	}

	@Override
	public void setValue(ICellPosition position, int cellValue) {
		if (position instanceof IIsRectangle rectangle) {
			int x = rectangle.getWidth();
			int y = rectangle.getHeight();
			LOGGER.debug("We turn {} to {}", position, cellValue);

			int oneDimensionalIndex = x + map.getWidth() * y;
			this.raw[oneDimensionalIndex] = cellValue;
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

}
