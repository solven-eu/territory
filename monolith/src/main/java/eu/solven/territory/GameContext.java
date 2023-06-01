package eu.solven.territory;

import java.util.function.Supplier;

import lombok.Data;

@Data
public class GameContext {
	private final ITerritoryMap map;
	private final Supplier<IPlayerOccupation> supplierOccupation;

	public IPlayerOccupation getOccupation() {
		return supplierOccupation.get();
	}

}
