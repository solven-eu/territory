package eu.solven.territory.snake.strategies.dummy;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;

/**
 * A very dummy {@link IDirectionPicker}. We prefer moving forward. If not possible, we turn left, else right. No
 * random.
 * 
 * @author Benoit Lacelle
 *
 */
public class LeftElseRight_NeverEatSnake extends LeftElseRight_MayEatSnake {

	@Override
	public int pickDirection(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			SnakeCell currentHead) {
		int newDirection = IDirectionPicker.NO_DIRECTION;

		// Search for a direction not eating a snake
		for (int optDirection : directionCandidates(currentHead)) {
			if (canBeNextHead_noSnake(map, context, position, optDirection)) {
				newDirection = optDirection;

				break;
			}
		}

		return newDirection;
	}

}
