package eu.solven.territory.snake.strategies.v1_cansmell;

import java.util.Collection;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.strategies.dummy.IDirectionPickerStrategy;
import eu.solven.territory.snake.strategies.dummy.LeftElseRight_MayEatSnake;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DirectionBasedOnSmells implements IDirectionPicker {
	final DoubleList distances;
	final Collection<ISnakeCell> cells;

	final LeftElseRight_MayEatSnake fallback = new LeftElseRight_MayEatSnake();

	final Random r;

	public DirectionBasedOnSmells(Supplier<Random> randomSupplier, DoubleList distances, Collection<ISnakeCell> cells) {
		this.r = randomSupplier.get();

		this.distances = distances;
		this.cells = cells;
	}

	@Override
	public int pickDirection(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			SnakeCell currentHead) {
		double oldestDistance = distances.getDouble(0);
		double recentDistance = distances.getDouble(distances.size() - 1);

		if (oldestDistance == Double.MAX_VALUE || recentDistance == Double.MAX_VALUE) {
			// No recent smell
			return fallback.pickDirection(map, context, position, currentHead);
		} else {
			int currentDirection = currentHead.getDirection();

			for (IDirectionPickerStrategy strategy : fallback.getStrategies()) {
				if (strategy.canBeNextHead(map, context, position, currentDirection)) {

					if (recentDistance == 1) {
						// This is a very interesting case: when the snake turns around an apple, we need a special
						// rule.
						// Else,
						// it walks tangentially to the apple, not turning as by `1` by diagonal is farther than being
						// `1` by
						// horizontal|vertical. It leads to moving to another diagonal, and circling around the apple.

						// Then, this expert-system has a special rule: optionally turning when we may be tangentially
						// to the
						// apple
						if (r.nextBoolean()) {
							return currentDirection;
						} else {
							// fall-back to the turn-random policy
						}
					} else if (recentDistance <= oldestDistance
							&& strategy.canBeNextHead(map, context, position, currentDirection)) {
						// We are approaching: keep the direction
						return currentDirection;
					}
				}
			}

			for (IDirectionPickerStrategy strategy : fallback.getStrategies()) {
				// We are getting away the food: turn!
				int left = GameOfSnake.turnLeft(currentHead);
				int right = GameOfSnake.turnRight(currentHead);

				// Random selection of left or right
				IntStream directionsStream;
				if (r.nextBoolean()) {
					directionsStream = IntStream.of(left, right);
				} else {
					directionsStream = IntStream.of(right, left);
				}

				// Some directions may not be available: fallback on the other direction
				OptionalInt optDirection =
						directionsStream.filter(d -> strategy.canBeNextHead(map, context, position, d)).findFirst();

				if (optDirection.isPresent() && optDirection.getAsInt() != NO_DIRECTION) {
					return optDirection.getAsInt();
				}
			}

			return NO_DIRECTION;
		}
	}

}
