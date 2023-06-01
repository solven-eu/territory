package eu.solven.territory.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import com.indvd00m.ascii.render.Region;
import com.indvd00m.ascii.render.Render;
import com.indvd00m.ascii.render.api.ICanvas;
import com.indvd00m.ascii.render.api.IContextBuilder;
import com.indvd00m.ascii.render.api.IRender;
import com.indvd00m.ascii.render.elements.Label;
import com.indvd00m.ascii.render.elements.Rectangle;
import com.indvd00m.ascii.render.elements.plot.Axis;
import com.indvd00m.ascii.render.elements.plot.AxisLabels;
import com.indvd00m.ascii.render.elements.plot.Plot;
import com.indvd00m.ascii.render.elements.plot.api.IPlotPoint;
import com.indvd00m.ascii.render.elements.plot.misc.PlotPoint;

import eu.solven.territory.GameContext;
import eu.solven.territory.GameOfLife;
import eu.solven.territory.IMapWindow;
import eu.solven.territory.IPlayerOccupation;
import eu.solven.territory.RectangleOccupation;
import eu.solven.territory.SquareMap;
import eu.solven.territory.TwoDimensionPosition;
import eu.solven.territory.render.ShowSwing;

@RestController
public class HelloController {

	protected Map<String, Integer> playerToId = new ConcurrentHashMap<>();
	protected Map<String, IPlayerOccupation> playerToOccupation = new ConcurrentHashMap<>();

	final String beforePre = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
			+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
			+ "    <head>\n"
			+ "        <title>Example</title>\n"
			+ "    </head>\n"
			+ "    <body>\n"
			+ "        <pre>";

	final String afterPre = "        </pre>\n" + "    </body>\n" + "</html>";

	final EventBus eventBus;

	public HelloController(EventBus eventBus) {
		this.eventBus = eventBus;

		swing("anonymous");

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
			updateAndGetNextTurn("anonymous");
		}, 1, 1, TimeUnit.SECONDS);
	}

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/swing")
	public String swing(@RequestParam(name = "playerName", defaultValue = "anonymous") String playerName) {
		// https://stackoverflow.com/questions/58040229/exception-in-thread-main-java-awt-headlessexception-in-spring-boot-java
		System.setProperty("java.awt.headless", "false");

		SquareMap squareMap = new SquareMap(20);

		GameContext gameContext = new GameContext(squareMap,
				() -> playerToOccupation.computeIfAbsent(playerName, player -> initialOccupation(squareMap)));
		new ShowSwing().main(eventBus, new AtomicReference<>(gameContext));
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/turn")
	public void nextTurn(@RequestParam(name = "playerName", defaultValue = "anonymous") String playerName) {
		updateAndGetNextTurn(playerName);
	}

	private IPlayerOccupation updateAndGetNextTurn(String playerName) {
		SquareMap squareMap = new SquareMap(20);

		GameOfLife gameOfLife = new GameOfLife();

		// int id = playerToId.computeIfAbsent(playerName, p -> playerToId.size());

		IPlayerOccupation singlePlayerOccupation =
				playerToOccupation.computeIfAbsent(playerName, player -> initialOccupation(squareMap));

		IPlayerOccupation playerNewSituation = gameOfLife.cycle(singlePlayerOccupation);
		playerToOccupation.put(playerName, playerNewSituation);

		eventBus.post(playerNewSituation);

		return playerNewSituation;
	}

	@GetMapping("/ascii")
	public String ascii(@RequestParam(name = "playerName", defaultValue = "anonymous") String playerName) {
		SquareMap squareMap = new SquareMap(20);
		IPlayerOccupation playerNewSituation = updateAndGetNextTurn(playerName);

		String s = generateAscii(squareMap, playerNewSituation);
		// System.out.println(s);

		s = s.replaceAll(" ", "&nbsp;");

		String pre = Stream.of(s.split("[\r\n]")).collect(Collectors.joining("<br/>"));

		// https://stackoverflow.com/questions/1702559/ascii-art-in-html
		return beforePre + pre + afterPre;
	}

	private RectangleOccupation initialOccupation(SquareMap squareMap) {
		RectangleOccupation empty = RectangleOccupation.empty(squareMap);

		empty.setValue(new TwoDimensionPosition(5, 5), 1);
		empty.setValue(new TwoDimensionPosition(5, 6), 1);
		empty.setValue(new TwoDimensionPosition(5, 7), 1);

		return empty;
	}

	private String generateAscii(SquareMap squareMap, IPlayerOccupation playerNewSituation) {
		List<IPlotPoint> points = new ArrayList<>();

		// Print each alive cell individually
		IMapWindow windowBuffer = playerNewSituation.makeWindowBuffer(1);
		playerNewSituation.forEachLiveCell(windowBuffer, cellPosition -> {

			if (cellPosition instanceof TwoDimensionPosition twoDimPosition) {
				IPlotPoint plotPoint = new PlotPoint(twoDimPosition.getX(), twoDimPosition.getY());
				points.add(plotPoint);
			} else {
				throw new IllegalStateException("!2D");
			}
		});

		// if (points.isEmpty())
		{
			// Generate fake coordinates
			points.add(new PlotPoint(0, 0));
			points.add(new PlotPoint(0, squareMap.getWidth()));
			points.add(new PlotPoint(squareMap.getHeight(), 0));
			points.add(new PlotPoint(squareMap.getHeight(), squareMap.getWidth()));
		}

		String s = generateAscii(points);
		return s;
	}

	private String generateAscii(List<IPlotPoint> points) {
		IRender render = new Render();
		IContextBuilder builder = render.newBuilder();
		builder.width(80).height(20);
		builder.element(new Rectangle(0, 0, 80, 20));
		builder.layer(new Region(1, 1, 78, 18));
		builder.element(new Axis(points, new Region(0, 0, 78, 18)));
		builder.element(new AxisLabels(points, new Region(0, 0, 78, 18)));
		builder.element(new Plot(points, new Region(0, 0, 78, 18)));

		builder.element(new Label("Score=" + points.size()));

		ICanvas canvas = render.render(builder.build());
		String s = canvas.getText();
		return s;
	}

}