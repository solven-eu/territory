package eu.solven.territory.two_dimensions;

import eu.solven.territory.ICellPosition;
import lombok.Data;

@Data
public class TwoDimensionPosition implements ICellPosition, IIsRectangle {
	/**
	 * 
	 * @return coordinate along width
	 */
	final int x;
	/**
	 * 
	 * @return coordinate along height
	 */
	final int y;

	@Override
	public int getWidth() {
		return getX();
	}

	@Override
	public int getHeight() {
		return getY();
	}

	@Override
	public ICellPosition shift(ICellPosition cellPosition) {
		if (cellPosition instanceof TwoDimensionPosition other) {
			return new TwoDimensionPosition(x + other.getX(), y + other.getY());
		} else {
			throw new IllegalArgumentException("!2D");
		}
	}

	public TwoDimensionPosition back(ICellPosition cellPosition) {
		if (cellPosition instanceof TwoDimensionPosition other) {
			return new TwoDimensionPosition(x - other.getX(), y - other.getY());
		} else {
			throw new IllegalArgumentException("!2D");
		}
	}
}
