package eu.solven.territory.snake;

public interface ISnakeCell extends ISnakeWorldItem {

	boolean isHead();

	ISnakeCell editSnake(WholeSnake newSnake);

	int getCellIndex();

	int getDirection();

	void newHead(int direction);

}
