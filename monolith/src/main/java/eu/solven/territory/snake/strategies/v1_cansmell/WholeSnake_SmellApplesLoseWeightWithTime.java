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
package eu.solven.territory.snake.strategies.v1_cansmell;

import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.strategies.dummy.IBirthDecider;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import lombok.ToString;

@ToString
public class WholeSnake_SmellApplesLoseWeightWithTime extends WholeSnake implements ICanSmell {
	int hatchLeft = 0;

	// hunger typically goes from 100 to 0
	// 100 means the stomach is full
	// 0 means we are starving
	int energy = 100;

	// Each turn with energy == 0
	// One may consider dead == (starve>0)
	int starve = 0;

	// How many step in the past we can remember the distance from the nearest apple
	int memoryCapacityForApple = 2;

	// `[0]` holds the most recent distance from food
	// `[4]` holds the distance from food 4 steps ago
	final DoubleList appleDistances = new DoubleArrayList();

	protected final Supplier<Random> randomSupplier;

	private static SnakeCell eggCanSmell(Supplier<Random> randomSupplier) {
		var hatchLeft = 10;
		WholeSnake wholeSnake = new WholeSnake_SmellApplesLoseWeightWithTime(randomSupplier, hatchLeft);

		var direction = randomSupplier.get().nextInt(4);
		return SnakeCell.newSnake(wholeSnake, direction);
	}

	public static WholeSnake babyCanSmell(Supplier<Random> randomSupplier) {
		SnakeCell babySnakeHead = eggCanSmell(randomSupplier);

		return babySnakeHead.getWhole();
	}

	@Override
	public WholeSnake makeEgg() {
		energy /= 2;

		return babyCanSmell(randomSupplier);
	}

	public WholeSnake_SmellApplesLoseWeightWithTime(Supplier<Random> randomSupplier, int hatchLeft) {
		this(UUID.randomUUID(), randomSupplier, 1);

		this.hatchLeft = hatchLeft;
	}

	protected WholeSnake_SmellApplesLoseWeightWithTime(UUID uuid, Supplier<Random> randomSupplier, int capacity) {
		super(uuid, capacity, new LinkedList<>());

		this.randomSupplier = randomSupplier;

		IntStream.range(0, memoryCapacityForApple).forEach(i -> appleDistances.add(Double.MAX_VALUE));
	}

	public WholeSnake copy() {
		var newSnake = new WholeSnake_SmellApplesLoseWeightWithTime(this.getId(), randomSupplier, getCellCapacity());

		copy(this, newSnake);

		return newSnake;
	}

	public static void copy(WholeSnake_SmellApplesLoseWeightWithTime from,
			WholeSnake_SmellApplesLoseWeightWithTime to) {
		LinkedList<ISnakeCell> newCells = from.getCells()
				.stream()
				.map(cell -> cell.editSnake(to))
				.collect(Collectors.toCollection(LinkedList::new));

		newCells.forEach(to::appendToTail);

		from.appleDistances.forEach(to::smellsApple);

		to.memoryCapacityForApple = from.memoryCapacityForApple;
		to.hatchLeft = from.hatchLeft;
		to.energy = from.energy;
		to.starve = from.starve;
	}

	@Override
	public void smellsApple(double distanceFromApple) {
		appleDistances.removeDouble(0);
		appleDistances.add(distanceFromApple);
	}

	@Override
	public void eatSomething() {
		super.eatSomething();

		energy = 100;
		starve = 0;
	}

	public void spendEnergy() {
		if (isHatching()) {
			hatchLeft--;
		} else {
			if (energy > 0) {
				energy--;
			}

			if (energy == 0) {
				if (getCells().size() == 1) {
					starve++;
				} else {
					// The snake eats it tail: it refills the hunger
					loseTail();
					eatSomething();
				}
			}
		}
	}

	public IDirectionPicker getDirectionPicker() {
		if (isHatching()) {
			return (map, context, position, currentHead) -> IDirectionPicker.NO_DIRECTION;
		} else {
			return new DirectionBasedOnSmells(randomSupplier, appleDistances, getCells());
		}
	}

	public IBirthDecider getBirthDecider() {
		return () -> {
			if (isHatching()) {
				// An egg can not give birth
				return false;
			} else if (getCellCapacity() >= 5 && energy >= 80) {
				// Giving birth will /2 energy
				return true;
			} else {
				return false;
			}
		};
	}

	public boolean isHatching() {
		return hatchLeft > 0;
	}
}
