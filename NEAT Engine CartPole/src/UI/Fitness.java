package UI;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import NEAT_Engine.NEAT_Engine;

public class Fitness extends JPanel {

	XYSeries[] data;
	int maxData;

	JFreeChart Chart(XYSeries[] data) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (XYSeries i : data) {
			dataset.addSeries(i);
		}
		JFreeChart chart = ChartFactory.createXYLineChart("Fitness", // Title
				"Gen", // x-axis Label
				"Fitness", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);
		return chart;
	}

	public Fitness(int x, int y,int width, int height, int maxData) {
		this.maxData = maxData + 1;
		setVisible(true);
		setBounds(x, y, width, height);

		data = new XYSeries[3];

		data[0] = new XYSeries("Best");
		data[1] = new XYSeries("Average");
		data[2] = new XYSeries("Worst");

		ChartPanel cp = new ChartPanel(Chart(data)) {

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, height);
			}
		};

		add(cp);
	}

	public void updateGraph() {
		data[0].add(NEAT_Engine.getGeneration(), NEAT_Engine.getAIScorePerScoreList(0));

		float temp = 0;
		for (int i = 0; i < NEAT_Engine.AIs.length; i++) {
			temp += NEAT_Engine.getAIScorePerScoreList(i);
		}
		data[1].add(NEAT_Engine.getGeneration(), temp / NEAT_Engine.AIs.length);

		data[2].add(NEAT_Engine.getGeneration(), NEAT_Engine.getAIScorePerScoreList(NEAT_Engine.AIs.length - 1));

		for (int i = 0; i < data.length; i++) {
			while (data[i].getItemCount() > maxData) {
				data[i].remove(0);
			}
		}

		((ChartPanel) getComponent(0)).setChart(Chart(data));
	}

}
