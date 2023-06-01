package eu.solven.territory;

import java.util.function.Consumer;

/**
 * The occupation of a {@link ITerritoryMap} by a player. Due to {@link IFogOfWar}, these as per-player.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IPlayerOccupation<A extends IAnimal> {

	IPlayerOccupation<A> mutableCopy();

	void forEachLiveCell(IMapWindow<A> windowBuffer, Consumer<ICellPosition> cellPositionConsumer);

	void forEachDeadButNearLiveCell(int gameOfLifeRadius,
			IMapWindow<A> windowBuffer,
			Consumer<ICellPosition> cellPositionConsumer);

	/**
	 * 
	 * @param radius
	 * @return the size of the window. 0 mean the window is empty. 1 means only current cell is visible.
	 */
	IMapWindow<A> makeWindowBuffer(int radius);

	void setValue(ICellPosition position, A cellValue);
}
