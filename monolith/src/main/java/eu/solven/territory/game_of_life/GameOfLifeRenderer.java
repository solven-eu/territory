package eu.solven.territory.game_of_life;

import java.awt.Color;

import eu.solven.territory.IAnimal;
import eu.solven.territory.IGameRenderer;

public class GameOfLifeRenderer implements IGameRenderer {
	@Override
	public Color getCellColor(IAnimal iAnimal) {
		if (iAnimal == null) {
			return Color.black;
		} else if (iAnimal.equals(LiveCell.LIVE)) {
			return Color.red;
		} else {
			return Color.green;
		}
	}
}
