package eu.solven.territory.game_of_life;

import eu.solven.territory.IAnimal;
import eu.solven.territory.ICellMarker;
import lombok.Data;

@Data
public class LiveCell implements IAnimal, ICellMarker {

	// public static final LiveCell DEAD = new LiveCell(GameOfLife.DEAD);
	public static final LiveCell LIVE = new LiveCell(GameOfLife.LIVE);

	final int asInt;

}
