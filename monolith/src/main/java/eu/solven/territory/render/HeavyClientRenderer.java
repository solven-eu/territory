package eu.solven.territory.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.solven.territory.GameContext;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IPlayerOccupation;
import eu.solven.territory.RectangleOccupation;
import eu.solven.territory.SquareMap;
import eu.solven.territory.TwoDimensionPosition;

public class HeavyClientRenderer extends JComponent {
	private static final long serialVersionUID = -7682292545241939093L;

	final EventBus eventBus;

	final AtomicReference<GameContext> refGameContext;

	public HeavyClientRenderer(EventBus eventBus, AtomicReference<GameContext> refGameContext) {
		this.eventBus = eventBus;
		this.refGameContext = refGameContext;

		eventBus.register(this);
	}

	@Subscribe
	public void onEvent(Object event) {
		this.invalidate();
		this.repaint();
	}

	// @Override
	// public Dimension getPreferredSize() {
	// return size;
	// }

	@Override
	public void paintComponent(Graphics g) {
		GameContext gameContext = refGameContext.get();
		IPlayerOccupation occupation = gameContext.getOccupation();

		if (occupation instanceof RectangleOccupation rectangle) {

			BufferedImage image = new BufferedImage(((SquareMap) gameContext.getMap()).getWidth(),
					((SquareMap) gameContext.getMap()).getHeight(),
					BufferedImage.TYPE_INT_RGB);

			IMapWindow windowBuffer = occupation.makeWindowBuffer(1);
			rectangle.forEachLiveCell(windowBuffer, cellPosition -> {

				if (cellPosition instanceof TwoDimensionPosition rectangleCell) {
					// g2.setColor(Color.red);
					// g2.fillRect(rectangleCell.getX(), rectangleCell.getY(), 5, 5);
					image.setRGB(rectangleCell.getX(), rectangleCell.getY(), Color.red.getRGB());
				}
			});

			IMapWindow windowBuffer2 = occupation.makeWindowBuffer(2);
			rectangle.forEachDeadButNearLiveCell(1, windowBuffer2, cellPosition -> {

				if (cellPosition instanceof TwoDimensionPosition rectangleCell) {
					// g2.setColor(Color.green);
					// g2.fillRect(rectangleCell.getX(), rectangleCell.getY(), 5, 5);
					image.setRGB(rectangleCell.getX(), rectangleCell.getY(), Color.green.getRGB());
				}
			});

			Image scaled = image.getScaledInstance((int) g.getClip().getBounds().getWidth(),
					(int) g.getClip().getBounds().getHeight(),
					Image.SCALE_SMOOTH);

			g.drawImage(scaled, 0, 0, this);
		}

		// Graphics g2 = g.create();

	}
}