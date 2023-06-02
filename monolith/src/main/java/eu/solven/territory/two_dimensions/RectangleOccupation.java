package eu.solven.territory.two_dimensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.solven.territory.IAnimal;
import eu.solven.territory.ICellMarker;
import eu.solven.territory.ICellPosition;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IPlayerOccupation;
import eu.solven.territory.game_of_life.LiveCell;

/**
 * A basic Rectangle {@link IPlayerOccupation}.
 * 
 * `0` means empty.
 * 
 * @author Benoit Lacelle
 *
 */
public class RectangleOccupation<A extends IAnimal> implements IPlayerOccupation<A> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RectangleOccupation.class);

	final IIsRectangle map;
	// [0 -> width] holds the first/bottom row
	final List<A> raw;

	public RectangleOccupation(IIsRectangle map, List<A> raw) {
		this.map = map;
		this.raw = raw;
	}

	public static <A extends IAnimal> RectangleOccupation<A> empty(SquareMap map) {
		if (!map.isRectangleLike()) {
			throw new IllegalArgumentException("!rectangle");
		}

		SquareMap squareMap = (SquareMap) map;
		List<A> asArray = (List<A>) Arrays.asList(new Object[squareMap.getWidth() * squareMap.getHeight()]);

		return new RectangleOccupation<>(map, asArray);
	}

	@Override
	public IPlayerOccupation<A> mutableCopy() {
		return new RectangleOccupation<>(map, new ArrayList<>(raw));
	}

	@Override
	public IMapWindow<A> makeWindowBuffer(int radius) {
		return RectangleWindow.empty(radius, radius);
	}

	@Override
	public void forEachLiveCell(Class<? extends ICellMarker> marker,
			IMapWindow<A> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		if (windowBuffer instanceof RectangleWindow<A> rectangleWindow) {
			for (int x = 0; x < map.getWidth(); x++) {
				for (int y = 0; y < map.getHeight(); y++) {
					int oneDimensionalIndex = x + map.getWidth() * y;
					// May be null, if outOfWorld or dead
					A currentCell = raw.get(oneDimensionalIndex);
					if (currentCell != null && marker.isAssignableFrom(currentCell.getClass())) {
						TwoDimensionPosition position = new TwoDimensionPosition(x, y);
						fill(position, rectangleWindow);
						cellPositionConsumer.accept(position);
					}

				}
			}
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	@Override
	public void forEachDeadButNearLiveCell(Class<? extends ICellMarker> marker,
			int radius,
			IMapWindow<A> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer) {
		// assert windowBuffer.getRadius() >= radius

		Set<ICellPosition> live = new HashSet<>();
		Set<ICellPosition> nearLive = new HashSet<>();

		// We assume there is much less live than dead cells
		// Then, to find dead-near-live cells, we iterate around live cells to later iterate over this neighborhood,
		// excluding the live cells
		forEachLiveCell(marker, windowBuffer, cellPosition -> {
			live.add(cellPosition);

			windowBuffer.forEachCell(c -> nearLive.add(c.shift(cellPosition)));
		});

		if (windowBuffer instanceof RectangleWindow<A> rectangleWindow) {
			Sets.difference(nearLive, live).stream().map(TwoDimensionPosition.class::cast).forEach(deadButNearLive -> {
				fill(deadButNearLive, rectangleWindow);
				cellPositionConsumer.accept(deadButNearLive);
			});
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	private void fill(TwoDimensionPosition position, RectangleWindow<A> windowBuffer) {
		int x = position.getX();
		int y = position.getY();

		windowBuffer.forEachCoordinate((shiftX, shiftY) -> {
			int shiftedX = x + shiftX;
			int shiftedY = y + shiftY;

			if (shiftedX < 0 || shiftedX >= map.getWidth() || shiftedY < 0 || shiftedY >= map.getHeight()) {
				windowBuffer.setOffWorld(shiftX, shiftY);
			} else {
				windowBuffer.setValue(shiftX, shiftY, raw.get(shiftedX + map.getWidth() * shiftedY));
			}

		});

	}

	@Override
	public void setValue(ICellPosition position, A cellValue) {
		if (position instanceof IIsRectangle rectangle) {
			int x = rectangle.getWidth();
			int y = rectangle.getHeight();
			LOGGER.debug("We turn {} to {}", position, cellValue);

			int oneDimensionalIndex = x + map.getWidth() * y;
			this.raw.set(oneDimensionalIndex, cellValue);
		} else {
			throw new IllegalArgumentException("!rectangle");
		}
	}

	@Override
	public void setDead(ICellPosition position) {
		setValue(position, null);
	}

}
