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
package eu.solven.territory.snake;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Iterables;

import eu.solven.territory.snake.strategies.dummy.WholeSnake;
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
		Assertions.assertThat(world.snake.getCellCapacity()).isEqualTo(1);

		world.setValue(new TwoDimensionPosition(2, 1), new Apple());
		Assertions.assertThat(world.apples).hasSize(1);
		Assertions.assertThat(world.snake.getCellCapacity()).isEqualTo(1);

		SnakeInRectangleOccupation nextWorld = (SnakeInRectangleOccupation) game.cycle(world);
		Assertions.assertThat(nextWorld.apples).isEmpty();
		Assertions.assertThat(nextWorld.snake.getCellCapacity()).isEqualTo(2);
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
		Assertions.assertThat(world.snake.getCellCapacity()).isEqualTo(1);

		world.setValue(new TwoDimensionPosition(2, 1), new Apple());
		Assertions.assertThat(world.apples).hasSize(1);
		Assertions.assertThat(world.snake.getCellCapacity()).isEqualTo(1);

		SnakeInRectangleOccupation nextWorld = (SnakeInRectangleOccupation) game.cycle(world);
		Assertions.assertThat(nextWorld.apples).isEmpty();
		Assertions.assertThat(nextWorld.snake.getCellCapacity()).isEqualTo(2);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).isHead()).isTrue();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 0).getDirection()).isEqualTo(1);
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).isHead()).isFalse();
		Assertions.assertThat(Iterables.get(nextWorld.snake.getCells(), 1).getDirection()).isEqualTo(1);
	}
}
