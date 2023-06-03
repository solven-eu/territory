package eu.solven.territory.two_dimensions;

import java.util.Random;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;

/**
 * The simplest {@link ITerritoryMap}, represented by a square.
 * 
 * @author Benoit Lacelle
 *
 */
public class SquareMap implements ITerritoryMap, IIsRectangle {

	final Random r = new Random(0);
	final int width;

	public SquareMap(int width) {
		this.width = width;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return getWidth();
	}

	@Override
	public boolean isRectangleLike() {
		return true;
	}

	@Override
	public boolean isOutOfWorld(ICellPosition cellPosition) {
		if (cellPosition instanceof IIsRectangle rectangle) {
			if (rectangle.getWidth() < 0 || rectangle.getWidth() >= getWidth()) {
				return true;
			} else if (rectangle.getHeight() < 0 || rectangle.getHeight() >= getHeight()) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public int size() {
		return getWidth() * getHeight();
	}

	public ICellPosition randomPosition() {
		return new TwoDimensionPosition(r.nextInt(getWidth()), r.nextInt(getHeight()));
	}

}
