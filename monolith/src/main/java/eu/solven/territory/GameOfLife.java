package eu.solven.territory;

/**
 * The standard/Conway Game-of-Life rule.
 * 
 * @author Benoit Lacelle
 *
 */
// https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
public class GameOfLife implements IExpansionCycleRule {

	public static final int OFF_WORLD = -1;
	public static final int DEAD = 0;
	public static final int LIVE = 1;

	@Override
	public IPlayerOccupation cycle(IPlayerOccupation occupation) {
		IPlayerOccupation copy = occupation.mutableCopy();

		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow windowBuffer = occupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		occupation.forEachLiveCell(windowBuffer, position -> {
			// Assert the center of the window is indeed a live cell
			assert windowBuffer.getCenter() == LIVE;

			// Any live cell with 2 neighbours remains live
			long liveInAreaExcludingSelf = windowBuffer.count(LIVE) - 1;
			if (liveInAreaExcludingSelf <= 1) {
				// Any live cell with fewer than two live neighbours dies, as if by underpopulation.
				copy.setValue(position, DEAD);
			} else if (liveInAreaExcludingSelf >= 3) {
				// Any live cell with more than three live neighbours dies, as if by overpopulation.
				copy.setValue(position, DEAD);
			}
		});

		occupation.forEachDeadButNearLiveCell(gameOfLifeRadius, windowBuffer, position -> {
			// Assert the center of the window is indeed a dead cell
			assert windowBuffer.getCenter() == DEAD;

			if (windowBuffer.count(LIVE) == 3) {
				// Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
				copy.setValue(position, LIVE);
			}
		});

		return copy;
	}

}
