package eu.solven.territory.snake;

import java.util.Set;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITurnCycleContext;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;
import lombok.Data;

@Data
public class SnakeTurnContext implements ITurnCycleContext {
	final Set<ICellPosition> occupiedBySnake;
	final Set<ICellPosition> occupiedByApple;

	public boolean isApple(TwoDimensionPosition position) {
		return occupiedByApple.contains(position);
	}

}
