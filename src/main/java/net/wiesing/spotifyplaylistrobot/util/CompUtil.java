package net.wiesing.spotifyplaylistrobot.util;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 * Provide the comparision of two strings in a simple way.
 * @author Michael Wiesing
 */
public class CompUtil {

	public static double similarity(String a, String b) {
		SimilarityStrategy strategy = new JaroWinklerStrategy();
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
		return service.score(a, b);
	}

}
