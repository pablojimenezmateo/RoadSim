package searchAlgorithms;

import environment.Map;
import environment.Path;

public interface Algorithm {

	public Path getPath(Map map, String origin, String destination);
}
