package eu.solven.territory.snake;

import java.util.LinkedList;

import eu.solven.territory.snake.ISnakeMarkers.Snake;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SnakeCell extends SnakeOrApple implements Snake {

	// public static final SnakeCell BORDER = new SnakeCell(-1, 0);

	final WholeSnake whole;

	final boolean isHead;

	/**
	 * Given the snake cells, index==0 is the head, incremented until the tail
	 */
	final int cellIndex;

	/**
	 * 0 is right, 1 is up, 2 is left, 3 is bottom. This is relevant only for the head
	 */
	final int direction;

	public static SnakeCell headToRight() {
		WholeSnake wholeSnake = new WholeSnake(1, new LinkedList<>());
		SnakeCell head = new SnakeCell(wholeSnake, true, 0, 0);
		wholeSnake.appendAsHead(head);

		return head;
	}

	public SnakeCell newHead(int direction) {
		SnakeCell previousHead = whole.loseHead();

		int previousHeadCellIndex = previousHead.getCellIndex();
		SnakeCell behindNewHead = new SnakeCell(whole, false, previousHeadCellIndex, previousHead.getDirection());
		whole.appendAsHead(behindNewHead);

		SnakeCell futureHead = new SnakeCell(whole, true, previousHeadCellIndex + 1, direction);
		whole.appendAsHead(futureHead);
		return futureHead;
	}

	public SnakeCell copySnake(WholeSnake newSnake) {
		return new SnakeCell(newSnake, isHead, cellIndex, direction);
	}
}
