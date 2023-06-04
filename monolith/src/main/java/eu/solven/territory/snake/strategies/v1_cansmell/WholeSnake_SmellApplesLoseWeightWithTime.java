package eu.solven.territory.snake.strategies.v1_cansmell;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import lombok.ToString;

@ToString
public class WholeSnake_SmellApplesLoseWeightWithTime extends WholeSnake implements ICanSmell {
	// hunger typically goes from 100 to 0
	// 100 means the stomach is full
	// 0 means we are starving
	int hunger = 100;

	// How many step in the past we can remember the distance from the nearest apple
	int memoryCapacity = 2;

	// `[0]` holds the most recent distance from food
	// `[4]` holds the distance from food 4 steps ago
	final DoubleList distances = new DoubleArrayList();

	final Supplier<Random> randomSupplier;

	public static WholeSnake babyCanSmell(Supplier<Random> randomSupplier) {
		SnakeCell babySnakeHead = SnakeCell.headToRight_canSmell(randomSupplier);

		return babySnakeHead.getWhole();
	}

	public WholeSnake_SmellApplesLoseWeightWithTime(Supplier<Random> randomSupplier,
			int capacity,
			Deque<ISnakeCell> cells) {
		super(capacity, cells);

		this.randomSupplier = randomSupplier;

		IntStream.range(0, memoryCapacity).forEach(i -> distances.add(Double.MAX_VALUE));
	}

	public WholeSnake copy() {
		WholeSnake_SmellApplesLoseWeightWithTime newSnake =
				new WholeSnake_SmellApplesLoseWeightWithTime(randomSupplier, getCapacity(), new LinkedList<>());

		LinkedList<ISnakeCell> newCells = getCells().stream()
				.map(cell -> cell.editSnake(newSnake))
				.collect(Collectors.toCollection(LinkedList::new));

		newCells.forEach(newSnake::appendToTail);

		distances.forEach(newSnake::smells);

		newSnake.hunger = hunger;

		return newSnake;
	}

	@Override
	public void smells(double distanceFromApple) {
		distances.removeDouble(0);
		distances.add(distanceFromApple);
	}

	public void loseWeight() {
		hunger--;

		if (hunger == 0) {
			// The snake eats itself: it refills the hunger
			loseTail();
			hunger += 100;
		}
	}

	public IDirectionPicker getDirectionPicker() {
		return new DirectionBasedOnSmells(randomSupplier, distances, getCells());
	}
}