package eu.solven.territory.game_of_life;

import eu.solven.territory.IWorldCell;
import eu.solven.territory.ICellMarker;
import lombok.Data;

@Data
public class LiveCell implements IWorldCell, ICellMarker {

	// public static final LiveCell DEAD = new LiveCell(GameOfLife.DEAD);
	public static final LiveCell LIVE = new LiveCell(GameOfLife.LIVE);

	final int asInt;

}
