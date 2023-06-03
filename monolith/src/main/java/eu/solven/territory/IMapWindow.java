package eu.solven.territory;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Typically, a fraction of the {@link ITerritoryMap} within given cell can interact with. It suggests a cell can only
 * have local-impacts.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IMapWindow<A extends IWorldCell> {

	/**
	 * 
	 * @return the value at the center of the {@link IMapWindow}
	 */
	A getCenter();

	/**
	 * 
	 * @param predicate
	 * @return how many cells in the {@link IMapWindow} matches given value
	 */
	long count(Predicate<A> predicate);

	void forEachCell(Consumer<ICellPosition> object);

	void setOffWorld(int shiftX, int shiftY);

}
