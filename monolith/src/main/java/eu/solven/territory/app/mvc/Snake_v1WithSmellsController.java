package eu.solven.territory.app.mvc;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Suppliers;
import com.google.common.eventbus.EventBus;

import eu.solven.territory.IExpansionCycleRule;
import eu.solven.territory.IGameRenderer;
import eu.solven.territory.mvc.AGameController;
import eu.solven.territory.snake.GameOfSnakeRenderer;
import eu.solven.territory.snake.ISnakeWorldItem;
import eu.solven.territory.snake.SnakeInRectangleOccupation;
import eu.solven.territory.snake.strategies.v1_cansmell.WholeSnake_SmellApplesLoseWeightWithTime;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

@RestController
@RequestMapping("/snake_can_smell")
public class Snake_v1WithSmellsController extends AGameController<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Snake_v1WithSmellsController.class);

	final SquareMap squareMap = new SquareMap(20);
	final IExpansionCycleRule<ISnakeWorldItem> game = new GameOfSnake(squareMap);

	final Supplier<Random> randomSupplier = Suppliers.memoize(() -> new Random(0));

	public Snake_v1WithSmellsController(EventBus eventBus) {
		super(eventBus);

		// swing("anonymous");
		//
		// Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
		// try {
		// updateAndGetNextTurn(getGame(), "anonymous");
		// } catch (RuntimeException e) {
		// LOGGER.warn("ARG", e);
		// }
		// }, 10, 10, TimeUnit.MILLISECONDS);

	}

	@Override
	protected Class<ISnakeWorldItem> getAnimal() {
		return ISnakeWorldItem.class;
	}

	@Override
	protected IExpansionCycleRule<ISnakeWorldItem> getGame() {
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

		SnakeInRectangleOccupation withBaby = SnakeInRectangleOccupation.baby(squareMap,
				initialPosition,
				WholeSnake_SmellApplesLoseWeightWithTime.babyCanSmell(randomSupplier));

		return withBaby;
	}

}