package eu.solven.territory;

import java.util.function.Supplier;

import lombok.Data;

@Data
public class GameContext<A extends IAnimal> {
	private final Class<A> animal;
	private final ITerritoryMap map;
	private final IGameRenderer renderer;

	private final Supplier<IPlayerOccupation<A>> supplierOccupation;

	public IPlayerOccupation<A> getOccupation() {
		return supplierOccupation.get();
	}

}
