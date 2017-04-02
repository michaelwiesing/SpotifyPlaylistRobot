package net.wiesing.spotifyplaylistrobot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;

import net.wiesing.spotifyplaylistrobot.model.TrackDownload;
import net.wiesing.spotifyplaylistrobot.util.LogUtil;

/**
 * Logic to process the webpage parsing of the 1live plan b playlist.
 * @author Michael Wiesing
 */
public class EinsliveWebparser {

	/**
	 * Download the webpage and parses the playlist.
	 * 
	 * @param lu
	 *            Util for logging
	 * @return List of tracks
	 */
	public static ArrayList<TrackDownload> download1LivePlanBPlaylist(LogUtil lu) {
		ArrayList<TrackDownload> list = new ArrayList<TrackDownload>();
		try {
			URL url = new URL("http://www1.wdr.de/radio/1live/on-air/1live-channels/plan-b-channel-playlist-100.html");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			TrackDownload track = null;
			while ((line = in.readLine()) != null) {
				if (line.contains(
						"<thead><tr class=\"headlines\"><th class=\"entry\">Titel</th><th class=\"entry\">Interpret</th></tr></thead>")) {
					int listStart = line.indexOf("<tbody>") + 7;
					int listEnd = line.indexOf("</tbody>");
					String lineEntrys = line.substring(listStart, listEnd);

					int i = 0;
					while (lineEntrys.contains("<td class=\"entry\">")) {
						int entryStart = lineEntrys.indexOf("<td class=\"entry\">") + 18;
						int entryEnd = lineEntrys.substring(entryStart).indexOf("</td>") + entryStart;
						String lineEntry = lineEntrys.substring(entryStart, entryEnd);
						lineEntrys = lineEntrys.substring(entryEnd);
						if (i % 2 == 0) {
							track = new TrackDownload();
							track.title = lineEntry;
						} else {
							track.interpret = lineEntry;
							list.add(track);
						}
						i++;
					}
				}
			}
			in.close();
		} catch (Exception e) {
			lu.log(Level.SEVERE, "Something went wrong!", e);
		}
		return list;
	}

}
