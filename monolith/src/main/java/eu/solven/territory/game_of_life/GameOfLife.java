package eu.solven.territory.game_of_life;

import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IPlayerOccupation;

/**
 * The standard Conway Game-of-Life rule.
 * 
 * @author Benoit Lacelle
 *
 */
// https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
public class GameOfLife implements IExpansionCycleRule<LiveCell> {

	public static final int OFF_WORLD = -1;
	public static final int DEAD = 0;
	public static final int LIVE = 1;

	@Override
	public IPlayerOccupation<LiveCell> cycle(IPlayerOccupation<LiveCell> occupation) {
		IPlayerOccupation<LiveCell> copy = occupation.mutableCopy();

		int includeSelfRadius = 1;
		int gameOfLifeRadius = 1;
		IMapWindow<LiveCell> windowBuffer = occupation.makeWindowBuffer(includeSelfRadius + gameOfLifeRadius);

		occupation.forEachLiveCell(LiveCell.class, windowBuffer, position -> {
			// Assert the center of the window is indeed a live cell
			assert windowBuffer.getCenter().getAsInt() == LIVE;

			// Any live cell with 2 neighbours remains live
			long liveInAreaExcludingSelf = windowBuffer.count(i -> i != null && i.getAsInt() == LIVE) - 1;
			if (liveInAreaExcludingSelf <= 1) {
				// Any live cell with fewer than two live neighbours dies, as if by underpopulation.
				copy.setDead(position);
			} else if (liveInAreaExcludingSelf >= 3) {
				// Any live cell with more than three live neighbours dies, as if by overpopulation.
				copy.setDead(position);
			}
		});

		occupation.forEachDeadButNearLiveCell(LiveCell.class, gameOfLifeRadius, windowBuffer, position -> {
			// Assert the center of the window is indeed a dead cell
			assert windowBuffer.getCenter() == null;

			if (windowBuffer.count(i -> i != null && i.getAsInt() == LIVE) == 3) {
				// Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
				copy.setValue(position, LiveCell.LIVE);
			}
		});

		return copy;
	}

}
