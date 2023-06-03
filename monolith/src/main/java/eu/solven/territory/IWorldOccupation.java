package eu.solven.territory;

import java.util.function.Consumer;

import eu.solven.territory.two_dimensions.TwoDimensionPosition;

/**
 * The occupation of a {@link ITerritoryMap} by a player. Due to {@link IFogOfWar}, these as per-player.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IWorldOccupation<W extends IWorldCell> {

	IWorldOccupation<W> mutableCopy();

	void forEachLiveCell(Class<? extends ICellMarker> marker,
			IMapWindow<W> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer);

	void forEachDeadButNearLiveCell(Class<? extends ICellMarker> marker,
			int gameOfLifeRadius,
			IMapWindow<W> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer);

	/**
	 * 
	 * @param radius
	 * @return the size of the window. 0 mean the window is empty. 1 means only current cell is visible.
	 */
	IMapWindow<W> makeWindowBuffer(int radius);

	void setValue(ICellPosition position, W cellValue);

	void setDead(ICellPosition position);
}
