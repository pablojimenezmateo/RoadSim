package searchAlgorithms;

public class AlgorithmFactory {

	public Algorithm getAlgorithm() {

		return this.getAlgorithm(Method.SHORTEST);
	}

	public Algorithm getAlgorithm(Method method) {

		Algorithm ret = null;
		
		//I really dislike switch/case
		if (method.equals(Method.SHORTEST)) {
			
			return new ShortestPathAlgorithm();
			
		} else if (method.equals(Method.FASTEST)) {
			
			return new FastestPathAlgorithm();
			
		} else if (method.equals(Method.SMARTEST)) {
			
			return new SmartestPathAlgorithm();
		}
		
		return ret;
	}
}