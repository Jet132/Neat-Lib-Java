package UI;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

import NEAT_Engine.NEAT_Engine;

public class Species extends JPanel {

	ArrayList<ArrayList<Double>> data;
	int maxData;

	JFreeChart Chart(Double[][] data) {
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset("", "", data);

		JFreeChart chart = ChartFactory.createStackedAreaChart("Species", // Title
				"", // x-axis Label
				"", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);
		return chart;
	}

	public Species(int x, int y, int width, int height, int maxData) {
		this.maxData = maxData + 1;
		setVisible(true);
		setBounds(x, y, width, height);

		data = new ArrayList<ArrayList<Double>>();

		ChartPanel cp = new ChartPanel(Chart(new Double[0][0])) {

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, height);
			}
		};

		add(cp);
	}

	public void updateGraph() {
		int temp = maxData;
		if (NEAT_Engine.getGeneration() <= maxData) {
			temp = NEAT_Engine.getGeneration();
		}

		for (int i = 0; i < NEAT_Engine.Species.size(); i++) {
			boolean found = false;
			for (int j = 0; j < data.size(); j++) {
				if (NEAT_Engine.Species.get(i).ID == data.get(j).get(0)) {
					data.get(j).add((double) NEAT_Engine.Species.get(i).AIs.size());
					found = true;
					// System.out.println(NEAT_Engine.Species.get(i).ID+" "+data.get(j).size()+"
					// "+NEAT_Engine.getGeneration());
					break;
				}
			}
			if (!found) {
				ArrayList<Double> subdata = new ArrayList<Double>();
				subdata.add((double) NEAT_Engine.Species.get(i).ID);

				for (int j = 0; j < temp - 1; j++) {
					subdata.add(0d);
				}
				subdata.add((double) NEAT_Engine.Species.get(i).AIs.size());
				data.add(subdata);
			}
		}
		if (NEAT_Engine.Species.size() == 0) {
			data.get(0).add((double) NEAT_Engine.AIs.length);
		}
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).size() <= temp) {
				data.get(i).add(0d);
			} else {
				if (data.get(i).get(temp) == 0) {
					data.get(i).add(0d);
				}
			}
		}

		for (int i = 0; i < data.size(); i++) {
			while (data.get(i).size() > maxData) {
				data.get(i).remove(1);
			}
		}

		((ChartPanel) getComponent(0)).setChart(Chart(toArray(data)));
	}

	Double[][] toArray(ArrayList<ArrayList<Double>> arrayList) {
		Double[][] array = new Double[arrayList.size()][];
		for (int i = 0; i < arrayList.size(); i++) {
			ArrayList<Double> row = (ArrayList<Double>) arrayList.get(i).clone();
			row.remove(0);
			array[i] = row.toArray(new Double[0]);
		}
		return array;
	}
}
