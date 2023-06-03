package eu.solven.territory.snake;

import eu.solven.territory.snake.ISnakeMarkers.IsApple;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Apple extends SnakeOrApple implements IsApple {

}
