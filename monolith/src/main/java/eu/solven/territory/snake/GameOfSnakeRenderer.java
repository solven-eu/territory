package eu.solven.territory.snake;

import java.awt.Color;

import eu.solven.territory.DeadCell;
import eu.solven.territory.IWorldCell;
import eu.solven.territory.snake.ISnakeMarkers.IsApple;
import eu.solven.territory.IGameRenderer;

public class GameOfSnakeRenderer implements IGameRenderer {
	@Override
	public Color getCellColor(IWorldCell iAnimal) {
		if (iAnimal instanceof SnakeCell snakeCell) {
			if (snakeCell.isHead()) {
				return Color.blue;
			} else {
				return Color.green;
			}
		} else if (iAnimal instanceof DeadCell) {
			return Color.cyan;
		} else if (iAnimal instanceof IsApple) {
			return Color.yellow;
		} else {
			return Color.black;
		}
	}
}
