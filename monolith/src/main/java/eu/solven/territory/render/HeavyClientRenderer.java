package eu.solven.territory.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.solven.territory.DeadCell;
import eu.solven.territory.GameContext;
import eu.solven.territory.IWorldCell;
import eu.solven.territory.IGameRenderer;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IWorldOccupation;
import eu.solven.territory.ITerritoryMap;
import eu.solven.territory.two_dimensions.RectangleOccupation;
import eu.solven.territory.two_dimensions.SquareMap;
import eu.solven.territory.two_dimensions.TwoDimensionPosition;

public class HeavyClientRenderer<A extends IWorldCell> extends JComponent {
	private static final long serialVersionUID = -7682292545241939093L;

	final EventBus eventBus;

	final AtomicReference<GameContext<A>> refGameContext;

	public HeavyClientRenderer(EventBus eventBus, AtomicReference<GameContext<A>> refGameContext) {
		this.eventBus = eventBus;
		this.refGameContext = refGameContext;

		eventBus.register(this);
	}

	@Subscribe
	public void onEvent(Object event) {
		this.invalidate();
		this.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		GameContext<A> gameContext = refGameContext.get();
		IWorldOccupation<A> occupation = gameContext.getOccupation();

		// if (occupation instanceof RectangleOccupation<A> rectangle) {
		IGameRenderer renderer = refGameContext.get().getRenderer();

		BufferedImage image = new BufferedImage(((SquareMap) gameContext.getMap()).getWidth(),
				((SquareMap) gameContext.getMap()).getHeight(),
				BufferedImage.TYPE_INT_RGB);

		{
			IMapWindow<A> windowBuffer = occupation.makeWindowBuffer(1);
			occupation.forEachLiveCell(gameContext.getAnimal(), windowBuffer, cellPosition -> {

				if (cellPosition instanceof TwoDimensionPosition rectangleCell) {
					Color color = renderer.getCellColor(windowBuffer.getCenter());
					image.setRGB(rectangleCell.getX(), rectangleCell.getY(), color.getRGB());
				}
			});
		}

		{
			// Radius is 2 as we need to see further than the live cells
			IMapWindow<A> windowBuffer2 = occupation.makeWindowBuffer(2);
			occupation.forEachDeadButNearLiveCell(gameContext.getAnimal(), 1, windowBuffer2, cellPosition -> {
				assert null == windowBuffer2.getCenter();

				if (cellPosition instanceof TwoDimensionPosition rectangleCell) {
					if (isOutOfWorld(gameContext.getMap(), rectangleCell)) {
						// Out of the world
					} else {
						Color color = renderer.getCellColor(new DeadCell());
						image.setRGB(rectangleCell.getX(), rectangleCell.getY(), color.getRGB());
					}

				}
			});
		}

		Image scaled = image.getScaledInstance((int) g.getClip().getBounds().getWidth(),
				(int) g.getClip().getBounds().getHeight(),
				Image.SCALE_SMOOTH);

		g.drawImage(scaled, 0, 0, this);
		// }
	}

	private boolean isOutOfWorld(ITerritoryMap iTerritoryMap, TwoDimensionPosition cellPosition) {
		return iTerritoryMap.isOutOfWorld(cellPosition);
	}
}