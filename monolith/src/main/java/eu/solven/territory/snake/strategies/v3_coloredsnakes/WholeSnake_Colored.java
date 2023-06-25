/*
 * Copyright 2023 Benoit Lacelle - SOLVEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.solven.territory.snake.strategies.v3_coloredsnakes;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.snake.strategies.v1_cansmell.WholeSnake_SmellApplesLoseWeightWithTime;
import lombok.ToString;

@ToString
public class WholeSnake_Colored extends WholeSnake_SmellApplesLoseWeightWithTime {
	int color;

	// // How many step in the past we can remember the distance from the nearest apple
	// int memoryCapacity = 2;
	//
	// // `[0]` holds the most recent distance from food
	// // `[4]` holds the distance from food 4 steps ago
	// final DoubleList distances = new DoubleArrayList();

	private static SnakeCell coloredEgg(Supplier<Random> randomSupplier, int color) {
		var hatchLeft = 10;
		WholeSnake wholeSnake = new WholeSnake_Colored(randomSupplier, hatchLeft, color);

		var direction = randomSupplier.get().nextInt(4);
		return SnakeCell.newSnake(wholeSnake, direction);
	}

	public static WholeSnake coloredBaby(Supplier<Random> randomSupplier, int color) {
		SnakeCell babySnakeHead = coloredEgg(randomSupplier, color);

		return babySnakeHead.getWhole();
	}

	@Override
	public WholeSnake makeEgg() {
		return coloredBaby(randomSupplier, color);
	}

	public WholeSnake_Colored(Supplier<Random> randomSupplier, int hatchLeft, int color) {
		this(UUID.randomUUID(), randomSupplier, 1, color);
	}

	private WholeSnake_Colored(UUID uuid, Supplier<Random> randomSupplier, int capacity, int color) {
		super(uuid, randomSupplier, capacity);

		this.color = color;
	}

	public WholeSnake copy() {
		var newSnake = new WholeSnake_Colored(this.getId(), randomSupplier, getCellCapacity(), color);

		WholeSnake_SmellApplesLoseWeightWithTime.copy(this, newSnake);

		return newSnake;
	}

	// @Override
	// public void smells(double distanceFromApple) {
	// distances.removeDouble(0);
	// distances.add(distanceFromApple);
	// }

}
