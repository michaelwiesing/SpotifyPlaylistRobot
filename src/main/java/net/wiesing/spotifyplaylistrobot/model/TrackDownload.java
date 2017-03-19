package net.wiesing.spotifyplaylistrobot.model;

/**
 * Object that represents a single track.
 * @author Michael Wiesing
 */
public class TrackDownload {

	public String title;
	public String interpret;
	public String uri;

	public String toString() {
		if (uri == null)
			return interpret + " - " + title;
		else
			return interpret + " - " + title + " (" + uri + ")";

	}

}
