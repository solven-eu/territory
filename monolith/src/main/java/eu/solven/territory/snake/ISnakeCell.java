package eu.solven.territory.snake;

import eu.solven.territory.snake.strategies.dummy.WholeSnake;

public interface ISnakeCell extends ISnakeWorldItem {

	boolean isHead();

	ISnakeCell editSnake(WholeSnake newSnake);

	int getCellIndex();

	int getDirection();

	void newHead(int direction);

}
