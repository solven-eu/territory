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
package eu.solven.territory.snake.strategies.dummy;

import eu.solven.territory.ICellPosition;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.snake.SnakeCell;
import eu.solven.territory.snake.SnakeTurnContext;
import eu.solven.territory.snake.v0_only_snake.IDirectionPicker;

/**
 * A very dummy {@link IDirectionPicker}. We prefer moving forward. If not possible, we turn left, else right. No
 * random.
 * 
 * @author Benoit Lacelle
 *
 */
public class LeftElseRight_NeverEatSnake extends LeftElseRight_MayEatSnake {

	@Override
	public int pickDirection(ITerritoryMap map,
			SnakeTurnContext context,
			ICellPosition position,
			SnakeCell currentHead) {
		int newDirection = IDirectionPicker.NO_DIRECTION;

		// Search for a direction not eating a snake
		for (int optDirection : directionCandidates(currentHead)) {
			if (canBeNextHead_noSnake(map, context, position, optDirection)) {
				newDirection = optDirection;

				break;
			}
		}

		return newDirection;
	}

}
