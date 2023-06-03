package eu.solven.territory;

import java.util.function.Supplier;

import lombok.Data;

@Data
public class GameContext<A extends IWorldCell> {
	private final Class<A> animal;
	private final ITerritoryMap map;
	private final IGameRenderer renderer;

	private final Supplier<IWorldOccupation<A>> supplierOccupation;

	public IWorldOccupation<A> getOccupation() {
		return supplierOccupation.get();
	}

}
