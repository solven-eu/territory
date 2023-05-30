package eu.solven.territory;

/**
 * The simplest {@link ITerritoryMap}, represented by a square.
 * 
 * @author Benoit Lacelle
 *
 */
public class SquareMap implements ITerritoryMap, IIsRectangle {

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

}
