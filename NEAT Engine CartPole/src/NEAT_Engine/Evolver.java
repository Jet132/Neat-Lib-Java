package NEAT_Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javax.swing.JFrame;

import UI.NN;

public class Evolver {

	int nextSpecies = 0;
	int staleness = 0;
	float best_fitness = 0;
	static Random random = new Random();

	float getFitnessSum() {
		float sum = 0;
		for (Species s : NEAT_Engine.Species) {
			sum += s.getAvarageFitness();
		}
		return sum;
	}

	static Species selectSpecies() {
		float sum = 0;
		for (int i = 0; i < NEAT_Engine.Species.size(); i++) {
			sum += NEAT_Engine.Species.get(i).getBestFitness();
		}
		float rand = (float) (random.nextDouble() * sum);
		float runningSum = 0;

		for (int i = 0; i < NEAT_Engine.Species.size(); i++) {
			runningSum += NEAT_Engine.Species.get(i).getBestFitness();
			if (runningSum > rand) {
				return NEAT_Engine.Species.get(i);
			}
		}

		return NEAT_Engine.Species.get(0);
	}

	void evolve() {
		speciate();
		sortSpecies();
		NEAT_Engine.updateUI();
		cullSpecies();
		killStaleSpecies();
		killBadSpecies();

		if (NEAT_Engine.Species.size() == 0) {
			zeroSpeciesEvolve();
			System.err.println("No species left. Whole pop will be taken as one species.");
			return;
		}

		if (NEAT_Engine.getAIScorePerScoreList(0) > best_fitness) {
			best_fitness = NEAT_Engine.getAIScorePerScoreList(0);
			staleness = 0;
		} else {
			if (staleness > 20) {
				for (int i = 2; i < NEAT_Engine.Species.size();) {
					NEAT_Engine.Species.remove(i);
				}
				staleness = 0;
			}
			staleness++;
		}

		System.out.println("last generation:" + NEAT_Engine.getGeneration() + " Species:" + NEAT_Engine.Species.size()
				+ "  best Fitness:" + NEAT_Engine.getAIScorePerScoreList(0) + " staleness:" + staleness
				+ " best Fitness of all time:" + best_fitness);

		float fitnessSum = getFitnessSum();
		ArrayList<AI> children = new ArrayList<AI>();

		for (int i = 0; i < NEAT_Engine.Species.size(); i++) {
			if (NEAT_Engine.Species.get(i).AIs.size() > 5) {
				children.add(NEAT_Engine.Species.get(i).AIs.get(0).clone());
			}

			int NoOfChildren = (int) (NEAT_Engine.Species.get(i).getAvarageFitness() / fitnessSum
					* NEAT_Engine.AIs.length);
			for (int j = 0; j < NoOfChildren; j++) {
				children.add(NEAT_Engine.Species.get(i).makeChild(NEAT_Engine.InoHis, (random.nextInt(100) < 0.1)));
			}
		}

		while (children.size() < NEAT_Engine.AIs.length) {
			children.add(NEAT_Engine.Species.get(0).makeChild(NEAT_Engine.InoHis, false));
		}

		for (int i = children.size() - 1; i >= NEAT_Engine.AIs.length; i--) {
			children.remove(i);
		}

		NEAT_Engine.AIs = children.toArray(new AI[0]);
		for (int i = 0; i < NEAT_Engine.AIs.length; i++) {
			NEAT_Engine.AIs[i].orderNodes();
		}

	}

	void speciate() {
		for (Species s : NEAT_Engine.Species) {
			s.clearList();
		}

		for (int i = 0; i < NEAT_Engine.AIs.length; i++) {
			boolean speciesFound = false;
			for (Species s : NEAT_Engine.Species) {
				if (s.sameSpecies(NEAT_Engine.AIs[i])) {
					s.addAI(NEAT_Engine.AIs[i]);
					speciesFound = true;
					break;
				}
			}
			if (!speciesFound) {
				NEAT_Engine.Species.add(new Species(NEAT_Engine.AIs[i], nextSpecies));
				nextSpecies++;
			}
		}

		//int temp = 0;
		for (int i = 0; i < NEAT_Engine.Species.size(); i++) {
			System.out.println(
					i + ": " + NEAT_Engine.Species.get(i).AIs.size() + " " + NEAT_Engine.Species.get(i).staleness);
			//temp += NEAT_Engine.Species.get(i).AIs.size();
		}
		//System.out.println("total AIs:"+temp);
	}

	void sortSpecies() {
		for (Species s : NEAT_Engine.Species) {
			s.sortAIs();
		}

		Collections.sort(NEAT_Engine.Species, new Comparator<Species>() {

			@Override
			public int compare(Species o1, Species o2) {
				if (o1.getBestFitness() > o2.getBestFitness()) {
					return -1;
				}
				if (o1.getBestFitness() < o2.getBestFitness()) {
					return 1;
				}
				return 0;
			}
		});
	}

	void cullSpecies() {
		for (Species s : NEAT_Engine.Species) {
			s.cull();
			s.setAvarage();
			s.shareFitness();
		}
	}

	void killStaleSpecies() {
		// need to change
		for (int i = 0; i < NEAT_Engine.Species.size();) {
			if (NEAT_Engine.Species.get(i).getStaleness() > 15) {
				NEAT_Engine.Species.remove(i);
			} else {
				i++;
			}
		}
	}

	void killBadSpecies() {
		float Sum = getFitnessSum();

		for (int i = 1; i < NEAT_Engine.Species.size();) {
			if (NEAT_Engine.Species.get(i).getAvarageFitness() / Sum * NEAT_Engine.AIs.length < 1) {
				NEAT_Engine.Species.remove(i);
			} else {
				i++;
			}
		}
	}

	void zeroSpeciesEvolve() {
		ArrayList<AI> childrens = new ArrayList<AI>();

		for (int i = 0; i < NEAT_Engine.AIs.length / 2; i++) {
			childrens.add(NEAT_Engine.AIs[i]);
		}
		for (int i = 0; i < NEAT_Engine.AIs.length / 2; i++) {
			AI child;
			AI p1 = selectAI();
			AI p2 = selectAI();
			if (p1.getFitness() > p2.getFitness()) {
				child = p1.crossover(p2);
			} else {
				child = p2.crossover(p1);
			}
			child.mutate(NEAT_Engine.InoHis);
			childrens.add(child);
		}
		NEAT_Engine.AIs = childrens.toArray(new AI[0]);
	}

	AI selectAI() {
		float sum = 0;
		for (int i = 0; i < NEAT_Engine.AIs.length / 2; i++) {
			sum += NEAT_Engine.AIs[i].getFitness();
		}
		float rand = (float) (random.nextDouble() * sum);
		float runningSum = 0;

		for (int i = 0; i < NEAT_Engine.AIs.length / 2; i++) {
			runningSum += NEAT_Engine.AIs[i].getFitness();
			if (runningSum > rand) {
				return NEAT_Engine.AIs[i];
			}
		}

		return NEAT_Engine.AIs[0];
	}
}
