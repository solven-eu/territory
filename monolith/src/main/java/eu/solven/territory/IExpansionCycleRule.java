package eu.solven.territory;

public interface IExpansionCycleRule {
	/**
	 * 
	 * @param occupation
	 *            previous turn {@link IPlayerOccupation}
	 * @return next turn {@link IPlayerOccupation}
	 */
	IPlayerOccupation cycle(IPlayerOccupation occupation);
}
