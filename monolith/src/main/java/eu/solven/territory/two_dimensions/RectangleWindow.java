package eu.solven.territory.two_dimensions;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IWorldCell;

/**
 * A centered rectangle {@link IMapWindow}. It is guaranteed to have odd-size width and height, to have an unambiguous
 * center.
 * 
 * @author Benoit Lacelle
 *
 */
public class RectangleWindow<A extends IWorldCell> implements IMapWindow<A>, IIsRectangle {
	final List<A> window;

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
	public RectangleWindow(List<A> window, int halfWidth, int halfHeight) {
		this.width = halfToFull(halfWidth);
		this.height = halfToFull(halfHeight);
		if (halfWidth == 0) {
			assert window.isEmpty();
		} else if (halfHeight == 0) {
			assert window.isEmpty();
		} else {
			assert window.size() == width * height;
		}
		this.window = window;

	}

	private static int halfToFull(int half) {
		if (half == 0) {
			return 0;
		}
		return 1 + 2 * (half - 1);
	}

	public static int fullToHalf(int full) {
		if (full == 0) {
			return 0;
		}
		return (full - 1) / 2;
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
	public A getCenter() {
		if (window.size() % 2 != 1) {
			throw new IllegalStateException("!centered");
		}
		return window.get(window.size() / 2);
	}

	@Override
	public long count(Predicate<A> predicate) {
		return window.stream().filter(i -> predicate.test(i)).count();
	}

	public static <A extends IWorldCell> IMapWindow<A> empty(int halfWidth, int halfHeight) {
		int width = halfToFull(halfWidth);
		int height = halfToFull(halfHeight);
		Object[] raw = new Object[width * height];
		List<A> asList = (List<A>) Arrays.asList(raw);
		return new RectangleWindow<A>(asList, halfWidth, halfHeight);
	}

	public void forEachCoordinate(IntIntConsumer consumer) {
		int halfWidth = fullToHalf(getWidth());
		int halfHeight = fullToHalf(getHeight());

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
	// @Override
	public void setValue(int centeredX, int centeredY, A value) {
		int halfWidth = fullToHalf(getWidth());
		int halfHeight = fullToHalf(getHeight());

		if (Math.abs(centeredX) > halfWidth) {
			throw new IllegalArgumentException("Too large");
		} else if (Math.abs(centeredY) > halfHeight) {
			throw new IllegalArgumentException("Too large");
		}

		int shiftedX = halfWidth + centeredX;
		int shiftedY = halfHeight + centeredY;
		window.set(shiftedX + width * shiftedY, value);
	}

	@Override
	public void setOffWorld(int shiftX, int shiftY) {
		setValue(shiftX, shiftY, null);
	}

	/**
	 * Set all cells to dead. Useful before filling only relevant cells
	 */
	public void reset() {
		forEachCoordinate((x, y) -> setValue(x, y, null));
	}

}
