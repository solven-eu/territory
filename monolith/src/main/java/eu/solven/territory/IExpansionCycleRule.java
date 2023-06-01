package eu.solven.territory;

public interface IExpansionCycleRule<A extends IAnimal> {
	/**
	 * 
	 * @param occupation
	 *            previous turn {@link IPlayerOccupation}
	 * @return next turn {@link IPlayerOccupation}
	 */
	IPlayerOccupation<A> cycle(IPlayerOccupation<A> occupation);
}
