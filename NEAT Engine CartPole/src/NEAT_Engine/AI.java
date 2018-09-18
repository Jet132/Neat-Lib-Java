package NEAT_Engine;

import java.util.ArrayList;
import java.util.Random;

public class AI {

	Random random = new Random();

	public ArrayList<node> nodes;
	ArrayList<node> orderedNodes;
	public ArrayList<connection> cons;
	float[] outputs;
	public int layers;
	int nextNode;
	node biasNode;
	float[] fitness;
	float average_fitness;
	int ID;

	float randomWeight() {
		return random.nextFloat() * 2 - 1;
	}

	connection[] getConnections() {
		return cons.toArray(new connection[0]);
	}

	connection[] cloneConnections() {
		connection[] temp = new connection[cons.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = cons.get(i).clone();
		}

		return cons.toArray(new connection[0]);
	}

	float getFitness() {
		return average_fitness;
	}

	float getOutput(int output) {
		return outputs[output];
	}

	int getID() {
		return ID;
	}

	public AI(int[] layout, boolean bias) {
		nodes = new ArrayList<node>();
		cons = new ArrayList<connection>();

		for (int i = 0; i < layout[0]; i++) {
			nodes.add(new node(nextNode, i, 0, null));
			nextNode++;
		}

		if (bias) {
			biasNode = new node(nextNode, -1, 0, null);
			nodes.add(biasNode);
			nextNode++;
		} else {
			biasNode = null;
		}

		for (int i = 0; i < layout[layout.length - 1]; i++) {
			nodes.add(new node(nextNode, i, layout.length - 1, null));
			nextNode++;
		}
		layers = layout.length;
		outputs = new float[layout[layout.length - 1]];
		orderNodes();
		fitness = new float[NEAT_Engine.maxGstep];
	}

	public AI(ArrayList<node> nodes, ArrayList<connection> cons, int nextNode, int layers, node biasNode) {
		fitness = new float[NEAT_Engine.maxGstep];
		this.nodes = new ArrayList<node>();
		for (int i = 0; i < nodes.size(); i++) {
			this.nodes.add(nodes.get(i).clone());
		}
		this.cons = new ArrayList<connection>();
		for (int i = 0; i < cons.size(); i++) {
			this.cons.add(new connection(findNode(cons.get(i).getFromNode().getID()),
					findNode(cons.get(i).getToNode().getID()), cons.get(i).getWeight(), cons.get(i).getInoNum(),
					cons.get(i).isEnabled()));
		}
		this.nextNode = nextNode;
		this.layers = layers;
		this.biasNode = biasNode;
		this.outputs = new float[NEAT_Engine.outputs];
		orderNodes();
	}

	public AI() {
		fitness = new float[NEAT_Engine.maxGstep];
		this.outputs = new float[NEAT_Engine.outputs];
		cons = new ArrayList<connection>();
		nodes = new ArrayList<node>();
		orderNodes();
	}
	
	void calculateAverageFitness() {
		float sum = 0;
		for(float i : fitness) {
			sum += i;
		}
		average_fitness = sum/NEAT_Engine.maxGstep;
	}

	void setID(int ID) {
		this.ID = ID;
	}

	public int[] createLayout() {
		int[] layout = new int[layers];
		for (int i = 0; i < nodes.size(); i++) {
			layout[nodes.get(i).getLayer()]++;
		}
		return layout;
	}

	public node findNode(int ID) {
		for (node node : nodes) {
			if (node.getID() == ID) {
				return node;
			}
		}
		System.out.println("cloudn't find node with ID:" + ID);
		return null;
	}

	void setNodes(ArrayList<node> nodes) {
		this.nodes.clear();
		for (node n : nodes) {
			this.nodes.add(n.clone());
		}
		nextNode = nodes.size();
	}

	void setInput(int Input, float value) {
		nodes.get(Input).setSum(value);
	}

	void setFitness(float fitness) {
		this.fitness[NEAT_Engine.generationstep] = fitness;
	}

	void run() {
		if (biasNode != null) {
			biasNode.setSum(1);
		}

		orderNodes();

		for (int i = 0; i < orderedNodes.size(); i++) {
			orderedNodes.get(i).exchange();
		}

		for (int i = 0; i < outputs.length; i++) {
			outputs[i] = nodes.get(NEAT_Engine.inputs + i).getSum();
		}

		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setSum(0);
		}
	}

	void orderNodes() {
		connectNodes();
		orderedNodes = new ArrayList<node>();

		for (int i = 0; i < layers; i++) {
			for (int j = 0; j < nodes.size(); j++) {
				if (nodes.get(j).getLayer() == i) {
					orderedNodes.add(nodes.get(j));
				}
			}
		}
	}

	void connectNodes() {

		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).clearOutputCons();
		}

		for (int i = 0; i < cons.size(); i++) {
			cons.get(i).fromNode.outCons.add(cons.get(i));
		}
	}

	boolean isConnected(node node1, node node2) {
		if (node1.getLayer() == node2.getLayer()) {
			return false;
		}

		for (int i = 0; i < cons.size(); i++) {
			if (cons.get(i).isConnected(node1) && cons.get(i).isConnected(node2)) {
				return true;
			}
		}

		return false;
	}

	boolean fullyConnected() {
		int[] layout = createLayout();

		int conSize = 0;
		for (int i = 0; i < layout.length - 1; i++) {
			int temp = 0;
			for (int j = i + 1; j < layers; j++) {
				temp += layout[j];
			}
			conSize += layout[i] * temp;
		}
		return (conSize == cons.size());
	}

	int getActConsSize() {
		int count = 0;
		for (int i = 0; i < cons.size(); i++) {
			if (cons.get(i).isEnabled()) {
				count++;
			}
		}
		return count;
	}

	int getInoNum(ArrayList<connectionHistory> InoHis, node fromNode, node toNode) {
		boolean isNew = true;
		int connectionInnovationNumber = NEAT_Engine.nextConnectionNo;
		for (int i = 0; i < InoHis.size(); i++) {
			if (InoHis.get(i).matches(cons.toArray(new connection[0]), fromNode, fromNode)) {
				isNew = false;
				connectionInnovationNumber = InoHis.get(i).getInoNum();
				break;
			}
		}

		if (isNew) {
			int[] inoNums = new int[cons.size()];
			for (int i = 0; i < cons.size(); i++) {
				inoNums[i] = cons.get(i).getInoNum();
			}

			NEAT_Engine.InoHis
					.add(new connectionHistory(fromNode.getID(), toNode.getID(), connectionInnovationNumber, inoNums));
			NEAT_Engine.nextConnectionNo++;
		}
		return connectionInnovationNumber;
	}

	int matchingGene(connection[] cons2, int InoNum) {
		for (int i = 0; i < cons2.length; i++) {
			if (cons2[i].getInoNum() == InoNum) {
				return i;
			}
		}
		return -1;
	}

	void addNode(ArrayList<connectionHistory> InoHis) {
		if (cons.size() == 0) {
			addConnection(InoHis);
		}

		int ranCon = random.nextInt(cons.size());

		/*
		 * if (biasNode != -1) { while (cons.get(ranCon).fromNode.getID() ==
		 * nodes.get(biasNode).getID()) { ranCon = random.nextInt(cons.size()); int[] a
		 * = null; a[0] = 0; } }
		 */

		cons.get(ranCon).setEnabled(false);

		int newNode = nodes.size();
		nodes.add(new node(nextNode));
		nextNode++;

		cons.add(new connection(cons.get(ranCon).fromNode, nodes.get(newNode), randomWeight(),
				getInoNum(InoHis, cons.get(ranCon).fromNode, nodes.get(newNode))));
		cons.add(new connection(nodes.get(newNode), cons.get(ranCon).toNode, randomWeight(),
				getInoNum(InoHis, nodes.get(newNode), cons.get(ranCon).toNode)));

		nodes.get(newNode).setLayer(cons.get(ranCon).fromNode.getLayer() + 1);

		if (nodes.get(newNode).getLayer() == cons.get(ranCon).toNode.getLayer()) {
			for (int i = 0; i < nodes.size() - 1; i++) {
				if (nodes.get(i).getLayer() >= nodes.get(newNode).getLayer()) {
					nodes.get(i).layer++;
				}
			}
			layers++;
		}
		connectNodes();
	}

	void addConnection(ArrayList<connectionHistory> InoHis) {
		if (fullyConnected()) {
			// System.out.println("NN already fully connected");
			return;
		}

		int RNode1 = random.nextInt(nodes.size());
		int RNode2 = random.nextInt(nodes.size());
		while (nodes.get(RNode1).getLayer() == nodes.get(RNode2).getLayer()
				|| isConnected(nodes.get(RNode1), nodes.get(RNode2))) {
			RNode1 = random.nextInt(nodes.size());
			RNode2 = random.nextInt(nodes.size());
		}

		if (nodes.get(RNode1).getLayer() > nodes.get(RNode2).getLayer()) {
			int temp = RNode1;
			RNode1 = RNode2;
			RNode2 = temp;
		}

		int conInoNum = getInoNum(InoHis, nodes.get(RNode1), nodes.get(RNode2));

		cons.add(new connection(nodes.get(RNode1), nodes.get(RNode2), randomWeight(), conInoNum));
	}

	void mutate(ArrayList<connectionHistory> InoHis) {
		if (cons.size() == 0) {
			addConnection(InoHis);
		}

		if (random.nextInt(100) <= NEAT_Engine.WMR) {
			for (int i = 0; i < cons.size(); i++) {
				if (random.nextInt(100) <= 90) {
					cons.get(i).setWeight(cons.get(i).getWeight()+(random.nextFloat()/2-0.25f));
				}else {
					cons.get(i).setWeight(randomWeight());
				}
			}
		}

		if (random.nextInt(100) <= NEAT_Engine.CAR) {
			addConnection(InoHis);
		}

		if (random.nextInt(100) <= NEAT_Engine.NAR) {
			addNode(InoHis);
		}
	}

	void printNN() {
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println("Node ID:" + nodes.get(i).getID() + " layer:" + nodes.get(i).getLayer());
		}
		for (int i = 0; i < cons.size(); i++) {
			System.out.print("Con index:" + i + " fromNode:" + cons.get(i).getFromNode().getID() + " toNode:"
					+ cons.get(i).getToNode().getID());
			if (!cons.get(i).isEnabled()) {
				System.out.println(" disabled");
			} else {
				System.out.println();
			}
		}
	}

	AI crossover(AI p2) {
		AI child = new AI();
		child.layers = layers;
		child.nextNode = nextNode;
		child.biasNode = biasNode;

		ArrayList<connection> childCons = new ArrayList<connection>();
		boolean[] isEnabled = new boolean[cons.size()];

		for (int i = 0; i < cons.size(); i++) {
			boolean enabled = true;

			int p2Gen = matchingGene(p2.getConnections(), cons.get(i).getInoNum());

			if (p2Gen != -1) {
				if (!cons.get(i).isEnabled() || !p2.getConnections()[i].isEnabled()) {
					if (random.nextInt(100) < NEAT_Engine.DWPR) {
						enabled = false;
					}
				}

				if (random.nextInt() < NEAT_Engine.DPG) {
					childCons.add(cons.get(i));
				} else {
					childCons.add(p2.getConnections()[p2Gen]);
				}
			} else {
				childCons.add(cons.get(i));
				enabled = cons.get(i).isEnabled();
			}

			isEnabled[i] = enabled;
		}

		child.setNodes(nodes);

		for (int i = 0; i < childCons.size(); i++) {
			child.cons.add(new connection(child.findNode(childCons.get(i).fromNode.getID()),
					child.findNode(childCons.get(i).toNode.getID()), childCons.get(i).getWeight(),
					childCons.get(i).getInoNum(), isEnabled[i]));
		}

		child.connectNodes();
		return child;
	}

	protected AI clone() {
		return new AI(nodes, cons, nextNode, layers, biasNode);
	}

}
