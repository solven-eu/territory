package eu.solven.territory;

public interface IExpansionCycleRule<A extends IWorldCell> {
	/**
	 * 
	 * @param occupation
	 *            previous turn {@link IWorldOccupation}
	 * @return next turn {@link IWorldOccupation}
	 */
	IWorldOccupation<A> cycle(IWorldOccupation<A> occupation);
}
