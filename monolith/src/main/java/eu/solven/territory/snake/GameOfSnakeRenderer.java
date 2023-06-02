package eu.solven.territory.snake;

import java.awt.Color;

import eu.solven.territory.DeadCell;
import eu.solven.territory.IAnimal;
import eu.solven.territory.IGameRenderer;

public class GameOfSnakeRenderer implements IGameRenderer {
	@Override
	public Color getCellColor(IAnimal iAnimal) {
		if (iAnimal instanceof SnakeCell snakeCell) {
			if (snakeCell.isHead()) {
				return Color.blue;
			} else {
				return Color.green;
			}
		} else if (iAnimal instanceof DeadCell) {
			return Color.red;
		} else {
			return Color.black;
		}
	}
}
