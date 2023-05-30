package eu.solven.territory;

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

	public RectangleWindow(int[] window, int halfWidth, int halfHeight) {
		this.window = window;

		this.width = 1 + 2 * halfWidth;
		this.height = 1 + 2 * halfHeight;

		assert window.length == width * height;
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
		return IntStream.of(window).filter(i -> window[i] == predicate).count();
	}

	public static IMapWindow empty(int halfWidth, int halfHeight) {
		int width = 1 + 2 * halfWidth;
		int height = 1 + 2 * halfHeight;
		return new RectangleWindow(new int[width * height], halfWidth, halfHeight);
	}

	public void forEachCoordinate(IntIntConsumer consumer) {
		// TODO Auto-generated method stub

	}

	public void setValue(int x, int y, int i) {
		window[x * y] = i;
	}

}
