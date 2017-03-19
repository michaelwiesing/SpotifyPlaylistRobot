package net.wiesing.spotifyplaylistrobot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.wiesing.spotifyplaylistrobot.model.Constants;
import net.wiesing.spotifyplaylistrobot.model.TrackDownload;
import net.wiesing.spotifyplaylistrobot.util.LogUtil;
import net.wiesing.spotifyplaylistrobot.util.PropUtil;

/**
 * The main application.
 * @author Michael Wiesing
 */
public class App {

	/**
	 * The main function.
	 * 
	 * @param args
	 *            No args are supported
	 */
	public static void main(String[] args) {

		PropUtil pu = new PropUtil();

		pu.readProperty();
		LogUtil lu = LogUtil.getLogHelper(pu);

		lu.log(Level.INFO, "=== " + Constants.appName + " ===");

		if (configValuesComplete(pu)) {

			SpotifyApi sa = new SpotifyApi(pu, lu);

			if (pu.getProperty("accessToken") == null || pu.getProperty("accessToken").length() == 0
					|| pu.getProperty("refreshToken") == null || pu.getProperty("refreshToken").length() == 0) {
				lu.log(Level.INFO, "New token is necessary.");
				sa.loginAutorizationGrant();
			} else {
				lu.log(Level.INFO, "Use given token.");
				sa.loginRefreshGrant();
			}

			if (sa.validLogin()) {

				ArrayList<TrackDownload> tracks1LiveList = EinsliveWebparser.download1LivePlanBPlaylist(lu);
				ArrayList<String> tracksSpotifyList = sa.downloadSpotifyPlaylistTrackIds(pu.getProperty("playlist"));

				ArrayList<TrackDownload> newTracks = new ArrayList<TrackDownload>();
				for (TrackDownload track : tracks1LiveList) {
					lu.log(Level.INFO, "Try to find: " + track);
					track.uri = sa.searchTrack(track);
					if (track.uri == null)
						lu.log(Level.INFO, "Missing track: " + track);
					else {
						lu.log(Level.INFO, "Found track: " + track);
						if (!tracksSpotifyList.contains(track.uri))
							newTracks.add(track);
					}
				}

				if (newTracks.size() > 0) {

					List<String> idsNewTracks = new ArrayList<String>();
					for (TrackDownload track : newTracks) {
						lu.log(Level.INFO, "Try to add the following track: " + track.toString());
						idsNewTracks.add(track.uri);
					}
					sa.addTrackToPlaylist(idsNewTracks);

				} else {
					lu.log(Level.INFO, "No new tracks.");
				}

				pu.writeProperty();

			} else {
				lu.log(Level.SEVERE, "Login was not sucessfull!");
			}
		} else {
			lu.log(Level.SEVERE, "Not all necesarry config values are set!");
		}

		lu.log(Level.INFO, "==============================");
		lu.closeLogger();

	}

	/**
	 * Check if all necessary config values are set.
	 * 
	 * @param pu
	 *            Util for properties
	 * @return True if all config values are set
	 */
	private static boolean configValuesComplete(PropUtil pu) {
		return !(pu.getProperty("clientId").isEmpty() || pu.getProperty("clientSecret").isEmpty()
				|| pu.getProperty("username").isEmpty() || pu.getProperty("playlist").isEmpty());
	}

}
