package eu.solven.territory.render;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import eu.solven.territory.GameContext;
import eu.solven.territory.two_dimensions.SquareMap;

public class ShowSwing {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShowSwing.class);

	public void main(EventBus eventBus, AtomicReference<GameContext> refGameContext) {
		JFrame f = new JFrame("Territory");

		// Container contentPane = f.getContentPane();
		// LayoutManager layout = new GridLayout(1, 1);

		// contentPane.setLayout(layout);

		if (refGameContext.get().getMap() instanceof SquareMap square) {
			f.setSize(500, 500);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			HeavyClientRenderer component = new HeavyClientRenderer(eventBus, refGameContext);

			component.setVisible(true);
			component.repaint();

			// layout.addLayoutComponent("grid", component);
			f.add(component);

			f.invalidate();
			f.repaint();
			// f.pack();
		} else {
			LOGGER.warn("!2D");
		}

		// AFTER adding components
		f.setVisible(true);
	}
}