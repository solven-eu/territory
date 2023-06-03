package eu.solven.territory.snake;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class WholeSnake {
	// May grow when eating fruits
	int capacity = 1;

	final Deque<ISnakeCell> cells;

	public static WholeSnake baby() {
		SnakeCell babySnakeHead = SnakeCell.headToRight();

		return babySnakeHead.getWhole();
	}

	protected WholeSnake(int capacity, Deque<ISnakeCell> cells) {
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

		if (cells.size() >= capacity) {
			// Lose tail before adding new head
			loseTail();
		}

		cells.addFirst(head);
	}

	public void loseTail() {
		cells.pollLast();
	}

	public WholeSnake copy() {
		WholeSnake newSnake = new WholeSnake(capacity, new LinkedList<>());

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

	public Iterable<ISnakeCell> getCells() {
		return (Iterable<ISnakeCell>) cells;
	}
}
