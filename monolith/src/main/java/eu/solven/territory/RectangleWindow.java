package eu.solven.territory;

import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A centered rectangle {@link IMapWindow}. It is guaranteed to have odd-size width and height, to have an unambiguous
 * center.
 * 
 * @author Benoit Lacelle
 *
 */
public class RectangleWindow implements IMapWindow, IIsRectangle {
	final int[] window;

	final int width;
	final int height;

	/**
	 * 
	 * @param window
	 * @param halfWidth
	 *            if 0, we are empty. If 1, we are centered on a single point.
	 * @param halfHeight
	 *            if 0, we are empty. If 1, we are centered on a single point.
	 */
	public RectangleWindow(int[] window, int halfWidth, int halfHeight) {
		this.width = halfToFull(halfWidth);
		this.height = halfToFull(halfHeight);
		if (halfWidth == 0) {
			assert window.length == 0;
		} else if (halfHeight == 0) {
			assert window.length == 0;
		} else {
			assert window.length == width * height;
		}
		this.window = window;

	}

	private static int halfToFull(int half) {
		if (half == 0) {
			return 0;
		}
		return 1 + 2 * (half - 1);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getCenter() {
		if (window.length % 2 != 1) {
			throw new IllegalStateException("!centered");
		}
		return window[window.length / 2];
	}

	@Override
	public long count(int predicate) {
		return IntStream.of(window).filter(i -> i == predicate).count();
	}

	public static IMapWindow empty(int halfWidth, int halfHeight) {
		int width = halfToFull(halfWidth);
		int height = halfToFull(halfHeight);
		return new RectangleWindow(new int[width * height], halfWidth, halfHeight);
	}

	public void forEachCoordinate(IntIntConsumer consumer) {
		int halfWidth = (getWidth() - 1) / 2;
		int halfHeight = (getHeight() - 1) / 2;

		for (int x = -halfWidth; x <= halfWidth; x++) {
			for (int y = -halfHeight; y <= halfHeight; y++) {
				consumer.accept(x, y);
			}
		}
	}

	@Override
	public void forEachCell(Consumer<ICellPosition> object) {
		forEachCoordinate((x, y) -> object.accept(new TwoDimensionPosition(x, y)));
	}

	/**
	 * 
	 * @param centeredX
	 *            within [-halfWidth,halfWidth]
	 * @param centeredY
	 * @param value
	 */
	public void setValue(int centeredX, int centeredY, int value) {
		int halfWidth = (getWidth() - 1) / 2;
		int halfHeight = (getHeight() - 1) / 2;

		int shiftedX = halfWidth + centeredX;
		int shiftedY = halfHeight + centeredY;
		window[shiftedX + width * shiftedY] = value;
	}

}
