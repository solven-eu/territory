package eu.solven.territory;

import java.util.function.Consumer;

/**
 * A basic Rectangle {@link IPlayerOccupation}.
 * 
 * `0` means empty.
 * 
 * @author Benoit Lacelle
 *
 */
public class RectangleOccupation implements IPlayerOccupation {

	final IIsRectangle map;
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
		int radiusWithSelf = 1 + radius;
		return RectangleWindow.empty(radiusWithSelf, radiusWithSelf);
	}

	@Override
	public void forEachLiveCell(IMapWindow windowBuffer, Consumer<ICellPosition> cellPositionConsumer) {
		if (windowBuffer instanceof RectangleWindow rectangleWindow) {
			for (int x = 0; x < map.getWidth(); x++) {
				for (int y = 0; y < map.getHeight(); y++) {
					fill(x, y, rectangleWindow);

					cellPositionConsumer.accept(new TwoDimensionPosition(x, y));
				}
			}
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	@Override
	public void forEachDeadButNearLiveCell(int gameOfLifeRadius,
			IMapWindow windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		// TODO Auto-generated method stub

	}

	private void fill(int x, int y, RectangleWindow windowBuffer) {
		windowBuffer.forEachCoordinate((shiftX, shiftY) -> {
			int shiftedX = x + shiftX;
			int shiftedY = y + shiftY;
			windowBuffer.setValue(shiftX, shiftY, raw[shiftedX * shiftedY]);
		});

	}

	@Override
	public void setValue(ICellPosition position, int cellValue) {
		if (position instanceof IIsRectangle rectangle) {
			this.raw[rectangle.getWidth() * rectangle.getHeight()] = cellValue;
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

}
