package eu.solven.territory;

public class TwoDimensionPosition implements ICellPosition, IIsRectangle {
	final int x;
	final int y;

	public TwoDimensionPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @return coordinate along width
	 */
	public int getX() {
		return x;
	}

	/**
	 * 
	 * @return coordinate along height
	 */
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return getX();
	}

	@Override
	public int getHeight() {
		return getY();
	}
}
