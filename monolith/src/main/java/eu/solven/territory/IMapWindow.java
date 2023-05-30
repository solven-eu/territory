package eu.solven.territory;

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
	 * @return how many cells in the {@link IMapWindow} matches fiven value
	 */
	long count(int predicate);

}
