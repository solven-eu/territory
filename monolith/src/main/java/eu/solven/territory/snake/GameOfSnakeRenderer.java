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
package eu.solven.territory.snake;

import java.awt.Color;

import eu.solven.territory.DeadCell;
import eu.solven.territory.IGameRenderer;
import eu.solven.territory.IWorldCell;
import eu.solven.territory.snake.ISnakeMarkers.IsApple;

public class GameOfSnakeRenderer implements IGameRenderer {
	@Override
	public Color getCellColor(IWorldCell iAnimal) {
		if (iAnimal instanceof SnakeCell snakeCell) {
			if (snakeCell.getWhole().isHatching()) {
				return Color.red;
			} else if (snakeCell.isHead()) {
				return Color.blue;
			} else {
				return Color.green;
			}
		} else if (iAnimal instanceof DeadCell) {
			return Color.cyan;
		} else if (iAnimal instanceof IsApple) {
			return Color.yellow;
		} else {
			return Color.black;
		}
	}
}
