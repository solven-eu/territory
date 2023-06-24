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
	int capacity = 1;

	final Deque<ISnakeCell> cells;

	public static WholeSnake baby() {
		SnakeCell babySnakeHead = SnakeCell.headToRight();

		return babySnakeHead.getWhole();
	}

	public WholeSnake(int capacity, Deque<ISnakeCell> cells) {
		this(UUID.randomUUID(), capacity, cells);
	}

	protected WholeSnake(UUID uuid, int capacity, Deque<ISnakeCell> cells) {
		this.uuid = uuid;
		this.capacity = capacity;
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

		if (cells.size() >= getCapacity()) {
			// Lose tail before adding new head
			loseTail();
		}

		cells.addFirst(head);
	}

	public void loseWeight() {
		loseTail();
	}

	public void loseTail() {
		cells.pollLast();
	}

	public WholeSnake copy() {
		WholeSnake newSnake = new WholeSnake(uuid, getCapacity(), new LinkedList<>());

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
		return new LeftElseRight();
	}

	public void eatApple() {
		capacity = getCapacity() + 1;
	}

	public int getCapacity() {
		return capacity;
	}

	@Nonnull
	public UUID getId() {
		return uuid;
	}
}
