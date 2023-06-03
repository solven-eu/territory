package eu.solven.territory.snake.v0_only_snake;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;

public interface IDirectionPicker {

	int NO_DIRECTION = -1;

	int pickDirection(ITerritoryMap map, SnakeTurnContext context, ICellPosition position, SnakeCell currentHead);

}
