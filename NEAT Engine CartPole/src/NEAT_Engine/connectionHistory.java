package NEAT_Engine;

public class connectionHistory {

	int fromNode;
	int toNode;
	int innovationNumber;

	int[] innovationNumbers;

	int getInoNum() {
		return innovationNumber;
	}
	
	public connectionHistory(int fromNode,int toNode,int innovationNumber,int[] innovationNumbers) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.innovationNumber = innovationNumber;
		this.innovationNumbers = innovationNumbers;
	}

	boolean matches(connection[] cons, node fromNode, node toNode) {

		if (cons.length == innovationNumbers.length) {
			if (fromNode.getID() == this.fromNode && toNode.getID() == this.toNode) {
				for (int i = 0; i < cons.length; i++) {
					for (int j = 0; j < innovationNumbers.length; j++) {
						if (innovationNumbers[j] != cons[i].getInoNum()) {
							return false;
						}
					}
				}
				return true;
			}
		}

		return false;
	}
}
