package eu.solven.territory.snake.strategies.dummy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * A very dummy {@link IDirectionPicker}. We prefer moving forward. If not possi9ble, we turn left, else right. No
 * random.
 * 
 * @author Benoit Lacelle
 *
 */
public class LeftElseRight_MayEatSnake implements IDirectionPicker {

	final List<IDirectionPickerStrategy> strategies = Arrays.asList(
			// Search for a direction not eating a snake
			this::canBeNextHead_noSnake,
			// We have no direction: let's accept eating a snake
			this::canBeNextHead_maySnake);

	public List<IDirectionPickerStrategy> getStrategies() {
		return strategies;
	}

	@Override
	public int pickDirection(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			SnakeCell currentHead) {
		int newDirection = IDirectionPicker.NO_DIRECTION;

		for (IDirectionPickerStrategy strategy : getStrategies()) {
			for (int optDirection : directionCandidates(currentHead)) {
				if (strategy.canBeNextHead(map, context, position, optDirection)) {
					newDirection = optDirection;

					break;
				}
			}

			if (newDirection != IDirectionPicker.NO_DIRECTION) {
				break;
			}
		}

		return newDirection;
	}

	protected int[] directionCandidates(SnakeCell currentHead) {
		return new int[] {
				// Try moving forward
				currentHead.getDirection(),
				// Can not move forward: try turning left or right
				GameOfSnake.turnLeft(currentHead),
				// Left is also outOfWorld, let's turn right
				GameOfSnake.turnRight(currentHead) };
	}

	protected boolean canBeNextHead_noSnake(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			int optDirection) {
		TwoDimensionPosition optNextHead = GameOfSnake.nextHead(position, optDirection);

		return canBeNextHead_noSnake(map, context, optNextHead);
	}

	protected boolean canBeNextHead_noSnake(ITerritoryMap map,
			SnakeTurnContext context,
			TwoDimensionPosition optNextHead) {
		if (map.isOutOfWorld(optNextHead)) {
			return false;
		}

		Set<ICellPosition> occupiedBySnake = context.getOccupiedBySnake();

		return !occupiedBySnake.contains(optNextHead);
	}

	public boolean canBeNextHead_maySnake(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			int optDirection) {
		TwoDimensionPosition optNextHead = GameOfSnake.nextHead(position, optDirection);

		return canBeNextHead_maySnake(map, context, optNextHead);
	}

	protected boolean canBeNextHead_maySnake(ITerritoryMap map,
			SnakeTurnContext context,
			TwoDimensionPosition optNextHead) {
		return !map.isOutOfWorld(optNextHead);
	}

}
