package eu.solven.territory.game_of_life;

import java.awt.Color;

import eu.solven.territory.IGameRenderer;
import eu.solven.territory.IWorldCell;

public class GameOfLifeRenderer implements IGameRenderer {
	@Override
	public Color getCellColor(IWorldCell iAnimal) {
		if (iAnimal == null) {
			return Color.black;
		} else if (LiveCell.LIVE.equals(iAnimal)) {
			return Color.red;
		} else {
			return Color.green;
		}
	}
}
