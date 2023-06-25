package eu.solven.territory.snake.strategies.dummy;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeTurnContext;

public interface IDirectionPickerStrategy {
	boolean canBeNextHead(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			int optDirection);
}
