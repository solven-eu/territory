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
package eu.solven.territory.snake.strategies.dummy;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import eu.solven.territory.snake.ISnakeCell;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;

public class WholeSnake {
	final UUID uuid;

	// May grow when eating fruits
	int cellCapacity = 1;

	final Deque<ISnakeCell> cells;

	public static WholeSnake baby() {
		SnakeCell babySnakeHead = SnakeCell.headToRight();

		return babySnakeHead.getWhole();
	}

	public WholeSnake makeEgg() {
		return baby();
	}

	public WholeSnake(int capacity, Deque<ISnakeCell> cells) {
		this(UUID.randomUUID(), capacity, cells);
	}

	protected WholeSnake(UUID uuid, int capacity, Deque<ISnakeCell> cells) {
		this.uuid = uuid;
		this.cellCapacity = capacity;
		this.cells = cells;
	}

	public void appendToTail(ISnakeCell head) {
		if (head.isHead() && !cells.isEmpty()) {
			throw new IllegalStateException("Can not tail a head is the queue is not empty");
		}

		cells.addLast(head);
	}

	public void appendAsHead(ISnakeCell head) {
		if (head.isHead() && cells.stream().anyMatch(c -> c.isHead())) {
			throw new IllegalStateException("Can not append a head is there is already one");
		}

		if (cells.size() >= getCellCapacity()) {
			// Lose tail before adding new head
			loseTail();
		}

		cells.addFirst(head);
	}

	public void spendEnergy() {
		loseTail();
	}

	public ISnakeCell loseTail() {
		return cells.pollLast();
	}

	public WholeSnake copy() {
		var newSnake = new WholeSnake(uuid, getCellCapacity(), new LinkedList<>());

		LinkedList<ISnakeCell> newCells =
				cells.stream().map(cell -> cell.editSnake(newSnake)).collect(Collectors.toCollection(LinkedList::new));

		newCells.forEach(newSnake::appendToTail);

		return newSnake;
	}

	public ISnakeCell loseHead() {
		return cells.pollFirst();
	}

	public ISnakeCell getHead() {
		return cells.getFirst();
	}

	public Collection<ISnakeCell> getCells() {
		return (Collection<ISnakeCell>) cells;
	}

	public IDirectionPicker getDirectionPicker() {
		return new LeftElseRight_NeverEatSnake();
	}

	public void eatSomething() {
		cellCapacity = getCellCapacity() + 1;
	}

	public int getCellCapacity() {
		return cellCapacity;
	}

	@Nonnull
	public UUID getId() {
		return uuid;
	}

	public boolean isHatching() {
		return false;
	}

	public void decreaseCellCapacity() {
		cellCapacity--;
	}

	public IBirthDecider getBirthDecider() {
		return () -> false;
	}
}
