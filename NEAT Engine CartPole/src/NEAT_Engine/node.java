package NEAT_Engine;

import java.util.ArrayList;
import java.util.Arrays;

public class node {

	int ID;
	int layer;
	public int name;
	ArrayList<connection> outCons;
	float sum;

	private float sigmoid(float x) {
		return (float) (1 / (1 + Math.pow(Math.E, -4.9 * x)));
	}

	public int getLayer() {
		return layer;
	}

	public int getID() {
		return ID;
	}

	float getSum() {
		return sum;
	}

	public node(int ID, int name, int layer, connection[] outCon) {
		if (outCon == null) {
			this.outCons = new ArrayList<connection>();
		} else {
			this.outCons = new ArrayList<connection>(Arrays.asList(outCon));
		}
		this.ID = ID;
		this.layer = layer;
		this.name = name;
	}

	void exchange() {
		// System.out.println(sum);
		if (layer != 0) {
			sum = sigmoid(sum);
		}
		// System.out.println(outCons.size());
		for (int i = 0; i < outCons.size(); i++) {
			if (outCons.get(i).isEnabled()) {
				outCons.get(i).toNode.addSum(sum * outCons.get(i).getWeight());
			}
		}
	}

	public node(int ID) {
		this.ID = ID;
		this.outCons = new ArrayList<connection>();
	}

	void setSum(float value) {
		sum = value;
	}

	void addSum(float value) {
		sum += value;
	}

	void setLayer(int layer) {
		this.layer = layer;
	}

	void setOutCons(ArrayList<connection> outCons) {
		this.outCons = outCons;
	}

	void clearOutputCons() {
		outCons.clear();
	}

	void addOutputCon(connection con) {
		outCons.add(con);
	}

	protected node clone() {
		return new node(ID, name, layer, outCons.toArray(new connection[0]));
	}

}
