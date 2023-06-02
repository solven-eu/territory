package eu.solven.territory.snake;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class WholeSnake {
	// May grow when eating fruits
	int capacity = 1;

	final Deque<SnakeCell> cells;

	public static WholeSnake baby() {
		SnakeCell babySnakeHead = SnakeCell.headToRight();

		return babySnakeHead.getWhole();
	}

	protected WholeSnake(int capacity, Deque<SnakeCell> cells) {
		this.capacity = capacity;
		this.cells = cells;
	}

	public void appendToTail(SnakeCell head) {
		if (head.isHead() && !cells.isEmpty()) {
			throw new IllegalStateException("Can not tail a head is the queue is not empty");
		}

		cells.addLast(head);
	}

	public void appendAsHead(SnakeCell head) {
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

		LinkedList<SnakeCell> newCells =
				cells.stream().map(cell -> cell.copySnake(newSnake)).collect(Collectors.toCollection(LinkedList::new));

		newCells.forEach(newSnake::appendToTail);

		return newSnake;
	}

	public SnakeCell loseHead() {
		return cells.pollFirst();
	}

	public SnakeCell getHead() {
		return cells.getFirst();
	}
}
