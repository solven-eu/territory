/*
 * Copyright 2023 Benoit Lacelle - SOLVEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.mvc.AGameController;
import eu.solven.territory.snake.GameOfSnakeRenderer;
import eu.solven.territory.snake.ISnakeWorldItem;
import eu.solven.territory.snake.strategies.v1_cansmell.WholeSnake_SmellApplesLoseWeightWithTime;
import eu.solven.territory.snake.strategies.v2_multiplesnakes.MultipleSnakeInRectangleOccupation;
import eu.solven.territory.snake.strategies.v2_multiplesnakes.PositionnedSnake;
import eu.solven.territory.snake.v0_only_snake.GameOfSnake_Multiple;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

@RestController
@RequestMapping("/snake_multiple")
public class Snake_v2MultipleController extends AGameController<ISnakeWorldItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Snake_v2MultipleController.class);

	final SquareMap squareMap = new SquareMap(20);
	final IExpansionCycleRule<ISnakeWorldItem> game = new GameOfSnake_Multiple(squareMap);

	final Supplier<Random> randomSupplier = Suppliers.memoize(() -> new Random(0));

	public Snake_v2MultipleController(EventBus eventBus) {
		super(eventBus);

		swing("anonymous");

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
			try {
				updateAndGetNextTurn(getGame(), "anonymous");
			} catch (RuntimeException e) {
				LOGGER.warn("ARG", e);
			}
		}, 10, 10, TimeUnit.MILLISECONDS);

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
	protected IWorldOccupation<ISnakeWorldItem> initialOccupation(SquareMap squareMap) {
		PositionnedSnake firstSnake =
				new PositionnedSnake(WholeSnake_SmellApplesLoseWeightWithTime.babyCanSmell(randomSupplier),
						new TwoDimensionPosition(squareMap.getWidth() / 2, squareMap.getHeight() / 2));

		PositionnedSnake secondSnake =
				new PositionnedSnake(WholeSnake_SmellApplesLoseWeightWithTime.babyCanSmell(randomSupplier),
						new TwoDimensionPosition(1 + squareMap.getWidth() / 2, 1 + squareMap.getHeight() / 2));

		MultipleSnakeInRectangleOccupation withBaby =
				new MultipleSnakeInRectangleOccupation(squareMap, firstSnake, secondSnake);

		return withBaby;
	}

}