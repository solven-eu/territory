package eu.solven.territory.snake;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Iterables;

import eu.solven.territory.snake.Apple;
import eu.solven.territory.snake.SnakeInRectangleOccupation;
import eu.solven.territory.snake.WholeSnake;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

public class TestGameOfSnake {
	@Test
	public void testEatApple() {
		SquareMap map = new SquareMap(3);
		GameOfSnake game = new GameOfSnake(map);

		WholeSnake snake = WholeSnake.baby();

		SnakeInRectangleOccupation world = new SnakeInRectangleOccupation(map, snake, new TwoDimensionPosition(1, 1));
		Assertions.assertThat(world.apples).isEmpty();
		Assertions.assertThat(world.snake.capacity).isEqualTo(1);

		world.setValue(new TwoDimensionPosition(2, 1), new Apple());
		Assertions.assertThat(world.apples).hasSize(1);
		Assertions.assertThat(world.snake.capacity).isEqualTo(1);

		SnakeInRectangleOccupation nextWorld = (SnakeInRectangleOccupation) game.cycle(world);
		Assertions.assertThat(nextWorld.apples).isEmpty();
		Assertions.assertThat(nextWorld.snake.capacity).isEqualTo(2);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).isHead()).isTrue();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).getDirection()).isEqualTo(0);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).isHead()).isFalse();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).getDirection()).isEqualTo(0);
	}

	@Test
	public void testEatApple_goingUpAlongRightWall() {
		SquareMap map = new SquareMap(3);
		GameOfSnake game = new GameOfSnake(map);

		WholeSnake snake = WholeSnake.baby();
		snake.getHead().newHead(1);

		SnakeInRectangleOccupation world = new SnakeInRectangleOccupation(map, snake, new TwoDimensionPosition(2, 0));
		Assertions.assertThat(world.apples).isEmpty();
		Assertions.assertThat(world.snake.capacity).isEqualTo(1);

		world.setValue(new TwoDimensionPosition(2, 1), new Apple());
		Assertions.assertThat(world.apples).hasSize(1);
		Assertions.assertThat(world.snake.capacity).isEqualTo(1);

		SnakeInRectangleOccupation nextWorld = (SnakeInRectangleOccupation) game.cycle(world);
		Assertions.assertThat(nextWorld.apples).isEmpty();
		Assertions.assertThat(nextWorld.snake.capacity).isEqualTo(2);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).isHead()).isTrue();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).getDirection()).isEqualTo(1);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).isHead()).isFalse();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).getDirection()).isEqualTo(1);
	}
}
