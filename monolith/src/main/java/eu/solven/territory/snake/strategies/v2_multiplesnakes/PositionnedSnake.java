package eu.solven.territory.snake.strategies.v2_multiplesnakes;

import java.util.concurrent.atomic.AtomicReference;

import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;
import lombok.Data;

@Data
public class PositionnedSnake {
	final WholeSnake snake;
	final AtomicReference<TwoDimensionPosition> refHeadPosition;

	public PositionnedSnake(WholeSnake snake, TwoDimensionPosition headPosition) {
		this.snake = snake;
		this.refHeadPosition = new AtomicReference<TwoDimensionPosition>(headPosition);
	}

	public PositionnedSnake copy() {
		return new PositionnedSnake(snake.copy(), refHeadPosition.get());
	}

	public TwoDimensionPosition getHeadPosition() {
		return refHeadPosition.get();
	}

	public void setPosition(TwoDimensionPosition newHeadPosition) {
		refHeadPosition.set(newHeadPosition);
	}
	
}
