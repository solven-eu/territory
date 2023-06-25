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
