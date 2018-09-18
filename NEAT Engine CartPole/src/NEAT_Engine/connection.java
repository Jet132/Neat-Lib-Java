package NEAT_Engine;

public class connection {
	node fromNode;
	node toNode;
	float weight;
	int inoNum;
	boolean enabled;

	public float getWeight() {
		return weight;
	}

	int getInoNum() {
		return inoNum;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public node getFromNode() {
		return fromNode;
	}
	
	public node getToNode() {
		return toNode;
	}

	public connection(node fromNode, node toNode, float weight, int inoNum) {
		enabled = true;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.weight = weight;
		this.inoNum = inoNum;
	}

	public connection(node fromNode, node toNode, float weight, int inoNum, boolean enabled) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.weight = weight;
		this.inoNum = inoNum;
		this.enabled = enabled;
	}

	void setWeight(float weight) {
		this.weight = weight;
	}

	boolean isConnected(node Node) {
		return (Node == fromNode || Node == toNode);
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected connection clone() {
		return new connection(fromNode, toNode, weight, inoNum, enabled);
	}
}
