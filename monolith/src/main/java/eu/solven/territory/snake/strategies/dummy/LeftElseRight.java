package eu.solven.territory.snake.strategies.dummy;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

public class LeftElseRight implements IDirectionPicker {

	@Override
	public int pickDirection(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			SnakeCell currentHead) {
		int newDirection = IDirectionPicker.NO_DIRECTION;

		for (int optDirection : new int[] {
				// Try moving forward
				currentHead.getDirection(),
				// Can not move forward: try turning left or right
				GameOfSnake.turnLeft(currentHead),
				// Left is also outOfWorld, let's turn right
				GameOfSnake.turnRight(currentHead) }) {
			if (canBeNextHead(map, context, position, optDirection)) {
				newDirection = optDirection;

				break;
			}
		}

		return newDirection;
	}

	public boolean canBeNextHead(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			int optDirection) {
		TwoDimensionPosition optNextHead = GameOfSnake.nextHead(position, optDirection);

		boolean canBeNextHead = GameOfSnake.canBeNextHead(map, context, optNextHead);
		return canBeNextHead;
	}

}
