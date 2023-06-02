package eu.solven.territory;

/**
 * Represents the map to conquer.
 * 
 * @author Benoit Lacelle
 *
 */
public interface ITerritoryMap {
	/**
	 * 
	 * @return true if this can be viewed as a rectangle, with `(x, y)` coordinates where x going between [0,maxX) and y
	 *         between [0,maxY).
	 */
	boolean isRectangleLike();

	boolean isOutOfWorld(ICellPosition cellPosition);
}
