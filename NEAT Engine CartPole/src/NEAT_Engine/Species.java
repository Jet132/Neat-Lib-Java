package NEAT_Engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Species {

	public ArrayList<AI> AIs;
	public int ID;
	connection[] rep;
	AI best;
	float avarage_fitness;
	float best_fitness;
	int staleness;

	Random random = new Random();

	float getBestFitness() {
		return best_fitness;
	}

	float getAvarageFitness() {
		return avarage_fitness;
	}

	int getStaleness() {
		return staleness;
	}

	public Species(AI AI, int ID) {
		this.rep = AI.cloneConnections();
		AIs = new ArrayList<AI>();
		AIs.add(AI);

		best_fitness = AI.getFitness();
		avarage_fitness = best_fitness;
		staleness = 0;
		this.ID = ID;
	}

	void clearList() {
		AIs.clear();
	}

	void addAI(AI AI) {
		if (AI.getFitness() > best_fitness) {
			best_fitness = AI.getFitness();
		}

		avarage_fitness = (avarage_fitness * AIs.size() + AI.getFitness()) / (AIs.size() + 1);

		AIs.add(AI);
	}

	void sortAIs() {
		Collections.sort(AIs, new Comparator<AI>() {

			@Override
			public int compare(AI o1, AI o2) {
				if (o1.getFitness() > o2.getFitness()) {
					return -1;
				}
				if (o1.getFitness() < o2.getFitness()) {
					return 1;
				}
				return 0;
			}
		});

		if (AIs.size() == 0) {
			staleness = 200;
			return;
		}

		if (AIs.get(0).getFitness() > best_fitness) {
			staleness = 0;
			best_fitness = AIs.get(0).getFitness();
			rep = AIs.get(0).cloneConnections();
		} else {
			staleness++;
		}

	}

	void cull() {
		if (AIs.size() > 2) {
			for (int i = AIs.size() / 2; i < AIs.size();) {
				AIs.remove(i);
			}
		}
	}

	void setAvarage() {
		float sum = 0;
		for (int i = 0; i < AIs.size(); i++) {
			sum += AIs.get(i).getFitness();
		}
		avarage_fitness = sum / AIs.size();
	}

	void shareFitness() {
		avarage_fitness /= AIs.size();
	}

	boolean sameSpecies(AI AI) {
		int largeGenomeNormaliser = AI.getConnections().length - 20;
		if (largeGenomeNormaliser < 1) {
			largeGenomeNormaliser = 1;
		}

		Object[] Diff = getNNDif(AI.getConnections(), rep);

		//
		return (NEAT_Engine.compatibilityThreshold > (NEAT_Engine.excessCoeff * ((int) Diff[0]) / largeGenomeNormaliser)
				+ (NEAT_Engine.weightDiffCoeff * ((float) Diff[1])));
	}

	AI makeChild(ArrayList<connectionHistory> inoHis, boolean interspecies) {
		AI child;
		if (random.nextInt(100) > NEAT_Engine.COR) {
			child = selectAI().clone();
		} else {
			AI p1 = selectAI();
			AI p2;
			if (interspecies) {
				p2 = Evolver.selectSpecies().selectAI();
			} else {
				p2 = selectAI();
			}

			if (p1.getFitness() > p2.getFitness()) {
				child = p1.crossover(p2);
			} else {
				child = p2.crossover(p1);
			}
		}
		child.mutate(inoHis);
		return child;
	}

	AI selectAI() {
		float sum = 0;
		for (int i = 0; i < AIs.size(); i++) {
			sum += AIs.get(i).getFitness();
		}
		float rand = (float) (random.nextDouble() * sum);
		float runningSum = 0;

		for (int i = 0; i < AIs.size(); i++) {
			runningSum += AIs.get(i).getFitness();
			if (runningSum > rand) {
				return AIs.get(i);
			}
		}

		return AIs.get(0);
	}

	Object[] getNNDif(connection[] con1, connection[] con2) {
		if (con1.length == 0 || con2.length == 0) {
			return new Object[] { (int) 0, (float) 0 };
		}

		int matching = 0;
		float totalDiff = 0;
		for (int i = 0; i < con1.length; i++) {
			for (int j = 0; j < con2.length; j++) {
				if (con1[i].getInoNum() == con2[j].getInoNum()) {
					matching++;
					totalDiff += Math.abs(con1[i].getWeight() - con2[j].getWeight());
					break;
				}
			}
		}

		if (matching == 0) {
			return new Object[] { (int) con1.length + con2.length - 2 * (matching), (float) 100 };
		}

		return new Object[] { (int) con1.length + con2.length - 2 * matching, (float) totalDiff / matching };
	}

}
