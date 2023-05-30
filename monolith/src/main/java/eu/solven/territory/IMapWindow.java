package eu.solven.territory;

import java.util.function.Consumer;

/**
 * Typically, a fraction of the {@link ITerritoryMap} within given cell can interact with. It suggests a cell can only
 * have local-impacts.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IMapWindow {

	/**
	 * 
	 * @return the value at the center of the {@link IMapWindow}
	 */
	int getCenter();

	/**
	 * 
	 * @param predicate
	 * @return how many cells in the {@link IMapWindow} matches given value
	 */
	long count(int predicate);

	void forEachCell(Consumer<ICellPosition> object);

}
