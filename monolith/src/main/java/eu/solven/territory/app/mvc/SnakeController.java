package eu.solven.territory.app.mvc;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.eventbus.EventBus;

import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IGameRenderer;
import eu.solven.territory.mvc.AGameController;
import eu.solven.territory.snake.GameOfSnake;
import eu.solven.territory.snake.GameOfSnakeRenderer;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeInRectangleOccupation;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

@RestController
@RequestMapping("/snake")
public class SnakeController extends AGameController<SnakeCell> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SnakeController.class);

	final SquareMap squareMap = new SquareMap(20);
	final IExpansionCycleRule<SnakeCell> game = new GameOfSnake(squareMap);

	public SnakeController(EventBus eventBus) {
		super(eventBus);

		swing("anonymous");

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
			try {
				updateAndGetNextTurn(getGame(), "anonymous");
			} catch (RuntimeException e) {
				LOGGER.warn("ARG", e);
			}
		}, 1, 1, TimeUnit.SECONDS);

	}

	@Override
	protected Class<SnakeCell> getAnimal() {
		return SnakeCell.class;
	}

	@Override
	protected IExpansionCycleRule<SnakeCell> getGame() {
		return game;
	}

	@Override
	protected IGameRenderer getRenderer() {
		return new GameOfSnakeRenderer();
	}

	@GetMapping("/")
	public String index() {
		return "Snake";
	}

	@Override
	protected SnakeInRectangleOccupation initialOccupation(SquareMap squareMap) {
		TwoDimensionPosition initialPosition =
				new TwoDimensionPosition(squareMap.getWidth() / 2, squareMap.getHeight() / 2);

		SnakeInRectangleOccupation withBaby = SnakeInRectangleOccupation.baby(squareMap, initialPosition);

		return withBaby;
	}

}