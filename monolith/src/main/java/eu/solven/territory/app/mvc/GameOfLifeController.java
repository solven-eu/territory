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
import eu.solven.territory.game_of_life.GameOfLife;
import eu.solven.territory.game_of_life.GameOfLifeRenderer;
import eu.solven.territory.game_of_life.LiveCell;
import eu.solven.territory.mvc.AGameController;
import eu.solven.territory.two_dimensions.RectangleOccupation;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

@RestController
@RequestMapping("/game_of_life")
public class GameOfLifeController extends AGameController<LiveCell> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameOfLifeController.class);

	final SquareMap squareMap = new SquareMap(20);
	final IExpansionCycleRule<LiveCell> game = new GameOfLife();

	public GameOfLifeController(EventBus eventBus) {
		super(eventBus);

		// swing("anonymous");
		//
		// Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
		// try {
		// updateAndGetNextTurn(getGame(), "anonymous");
		// } catch (RuntimeException e) {
		// LOGGER.warn("ARG", e);
		// }
		// }, 1, 1, TimeUnit.SECONDS);

	}

	@Override
	protected Class<LiveCell> getAnimal() {
		return LiveCell.class;
	}

	@Override
	protected IExpansionCycleRule<LiveCell> getGame() {
		return game;
	}

	@Override
	protected IGameRenderer getRenderer() {
		return new GameOfLifeRenderer();
	}

	@GetMapping("/")
	public String index() {
		return "Game of Life";
	}

	@Override
	protected RectangleOccupation<LiveCell> initialOccupation(SquareMap squareMap) {
		RectangleOccupation<LiveCell> empty = RectangleOccupation.empty(squareMap);

		empty.setValue(new TwoDimensionPosition(5, 5), LiveCell.LIVE);
		empty.setValue(new TwoDimensionPosition(5, 6), LiveCell.LIVE);
		empty.setValue(new TwoDimensionPosition(5, 7), LiveCell.LIVE);

		return empty;
	}

}