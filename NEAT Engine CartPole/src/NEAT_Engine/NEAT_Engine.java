package NEAT_Engine;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import UI.Fitness;
import UI.NN;

public class NEAT_Engine {

	public static AI[] AIs;
	public static ArrayList<Species> Species;
	static ArrayList<connectionHistory> InoHis;

	static float excessCoeff = 1;
	static float weightDiffCoeff = 0.5f;
	static float compatibilityThreshold = 3;

	static int COR = 25;// rate of just cloning when evolving
	static int DWPR = 75; // connection disabled when one parent is too rate
	static int DPG = 50;// distribution of parent genes by crossover
	static int WMR;// weight mutation rate
	static int NAR;// node add rate
	static int CAR;// connection add rate
	static int nextConnectionNo = 1000;
	static boolean bias;

	public static String[] inputNames;
	public static String[] outputNames;

	static int inputs;
	static int outputs;

	static Float[] temp_Fitness;

	static int maxGstep;
	static int generation;
	static int generationstep;
	Evolver evolver;
	static JFrame ui;

	public static int getGeneration() {
		return generation;
	}

	public static int getGenerationstep() {
		return generationstep;
	}

	public static float getAIScorePerScoreList(int Number) {
		if (temp_Fitness[Number] == null) {
			return 0;
		} else {
			return temp_Fitness[Number];
		}

	}

	public static void showAI(int AI) {
		JFrame temp = new JFrame();
		temp.getContentPane().add(new NN(0,0,800,800,AIs[AI]));
		temp.setVisible(true);
		temp.pack();
	}

	public void setup(int[] layout, boolean bias, String[] inputNames, String[] outputNames, int AINumber,
			int GenerationSteps, int weightMutationrate, int nodeAddrate, int connectionAddrate) {
		this.bias = bias;
		maxGstep = GenerationSteps;
		WMR = weightMutationrate;
		NAR = nodeAddrate;
		CAR = connectionAddrate;

		InoHis = new ArrayList<connectionHistory>();
		Species = new ArrayList<Species>();

		temp_Fitness = new Float[AINumber];

		inputs = layout[0];
		outputs = layout[layout.length - 1];

		if (inputNames == null) {
			this.inputNames = new String[layout[0]];
			for (int i = 0; i < layout[0]; i++) {
				this.inputNames[i] = "Input " + i;
			}
		} else {
			this.inputNames = inputNames;
			for (int i = 0; i < layout[0]; i++) {
				if (this.inputNames[i].isEmpty()) {
					this.inputNames[i] = "Input " + i;
				}
			}
		}
		if (outputNames == null) {
			this.outputNames = new String[layout[layout.length - 1]];
			for (int i = 0; i < layout[layout.length - 1]; i++) {
				this.outputNames[i] = "Output " + i;
			}
		} else {
			this.outputNames = outputNames;
			for (int i = 0; i < layout[layout.length - 1]; i++) {
				if (this.outputNames[i].isEmpty()) {
					this.outputNames[i] = "Output " + i;
				}
			}
		}

		AIs = new AI[AINumber];
		for (int i = 0; i < AIs.length; i++) {
			AIs[i] = new AI(layout, bias);
			AIs[i].mutate(InoHis);
			AIs[i].fullyConnected();
		}
		evolver = new Evolver();
		ui = new JFrame("NEAT Engine UI");
		ui.getContentPane().setLayout(null);
		ui.getContentPane().add(new NN(0,0,800,800,AIs[0]));
		ui.getContentPane().add(new UI.Species(800,0,400, 400,10));
		ui.getContentPane().add(new Fitness(800,400,400, 400,10));
		ui.setVisible(true);
		ui.getContentPane().setPreferredSize(new Dimension(1200, 800));
		ui.pack();
	}

	public void setSpeciesThreshold(float excessCoeff, float weightDiffCoeff, float compatibilityThreshold) {
		this.excessCoeff = excessCoeff;
		this.weightDiffCoeff = weightDiffCoeff;
		this.compatibilityThreshold = compatibilityThreshold;
	}

	public void setInput(int Input, float value, int AI) {
		AIs[AI].setInput(Input, value);
	}

	public void runNN(int AI) {
		AIs[AI].run();
	}

	public float getOutput(int output, int AI) {
		return AIs[AI].getOutput(output);
	}

	public void setFitness(float fitness, int AI) {
		AIs[AI].setFitness(fitness);
	}

	public void nextGenerationstep() {
		if (generationstep < maxGstep - 1) {
			generationstep++;
		} else {
			for (AI AI : AIs) {
				AI.calculateAverageFitness();
			}
			nextGeneration();
			generationstep = 0;
		}
	}

	void nextGeneration() {
		createFitnessList();
		evolver.evolve();
		generation++;
		//((UI.Species) ui.getContentPane().getComponent(1)).updateGraph();
		System.out.println("next Generation");
	}
	
	public static void updateUI() {
		((NN) ui.getContentPane().getComponent(0)).setAI(AIs[0]);
		((UI.Species) ui.getContentPane().getComponent(1)).updateGraph();
		((Fitness) ui.getContentPane().getComponent(2)).updateGraph();
	}
	
	Species findSpecies(int ID) {
		for(Species s: Species) {
			if(s.ID == ID) {
				return s;
			}
		}
		return null;
	}

	void createFitnessList() {
		for (int i = 0; i < AIs.length; i++) {
			temp_Fitness[i] = AIs[i].getFitness();
			AIs[i].setID(i);
		}
		Arrays.sort(AIs, new Comparator<AI>() {

			@Override
			public int compare(AI o1, AI o2) {
				if (temp_Fitness[o1.getID()] > temp_Fitness[o2.getID()]) {
					return -1;
				}
				if (temp_Fitness[o1.getID()] < temp_Fitness[o2.getID()]) {
					return 1;
				}
				return 0;
			}
		});

		Arrays.sort(temp_Fitness, Collections.reverseOrder());
	}

}
