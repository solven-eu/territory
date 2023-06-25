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

import com.google.common.collect.Iterables;

import eu.solven.territory.snake.ISnakeMarkers.IsSnake;
import eu.solven.territory.snake.strategies.dummy.WholeSnake;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SnakeCell extends SnakeOrApple implements ISnakeCell, IsSnake {

	// public static final SnakeCell BORDER = new SnakeCell(-1, 0);

	final WholeSnake whole;

	final boolean isHead;

	/**
	 * Given the snake cells, index==0 is the head, incremented until the tail
	 */
	@Deprecated
	final int cellIndex;

	/**
	 * 0 is right, 1 is up, 2 is left, 3 is bottom. This is relevant only for the head
	 */
	final int direction;

	public static SnakeCell newSnake(WholeSnake wholeSnake, int direction) {
		if (!wholeSnake.getCells().isEmpty()) {
			throw new IllegalArgumentException("We expect an empty wholeSnake");
		}

		var head = new SnakeCell(wholeSnake, true, 0, direction);
		wholeSnake.appendAsHead(head);

		return head;
	}

	public void newHead(int direction) {
		ISnakeCell previousHead = whole.loseHead();

		int previousHeadCellIndex = previousHead.getCellIndex();
		var behindNewHead = new SnakeCell(whole, false, previousHeadCellIndex, previousHead.getDirection());
		whole.appendAsHead(behindNewHead);

		var futureHead = new SnakeCell(whole, true, previousHeadCellIndex + 1, direction);
		whole.appendAsHead(futureHead);
		// return futureHead;
	}

	public SnakeCell editSnake(WholeSnake newSnake) {
		return new SnakeCell(newSnake, isHead, cellIndex, direction);
	}

	public int getCellIndex() {
		return Iterables.indexOf(getWhole().getCells(), c -> c == this);
	}

}
