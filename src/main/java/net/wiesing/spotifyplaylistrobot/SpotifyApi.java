package net.wiesing.spotifyplaylistrobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.AddTrackToPlaylistRequest;
import com.wrapper.spotify.methods.CurrentUserRequest;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.methods.TrackSearchRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.ClientCredentials;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTrack;
import com.wrapper.spotify.models.Track;
import com.wrapper.spotify.models.User;

import net.wiesing.spotifyplaylistrobot.model.Constants;
import net.wiesing.spotifyplaylistrobot.model.TrackDownload;
import net.wiesing.spotifyplaylistrobot.util.CompUtil;
import net.wiesing.spotifyplaylistrobot.util.LogUtil;
import net.wiesing.spotifyplaylistrobot.util.PropUtil;

/**
 * Implements the needed methods for the spotify api library. Description of the
 * used library: https://github.com/thelinmichael/spotify-web-api-java. Some
 * code copied from the examples of the site.
 * @author Michael Wiesing
 */
public class SpotifyApi {

	private PropUtil pu = null;
	private LogUtil lu = null;
	private Api api = null;

	public SpotifyApi(PropUtil pu, LogUtil lu) {
		this.pu = pu;
		this.lu = lu;
	}

	/**
	 * Login that uses the client credentials flow described under
	 * https://developer.spotify.com/web-api/authorization-guide/.
	 */
	public void loginClientFlow() {
		// Create an API instance. The default instance connects to
		// https://api.spotify.com/.
		api = Api.builder().clientId(pu.getProperty("clientId")).clientSecret(pu.getProperty("clientSecret")).build();

		/* Create a request object. */
		final ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();

		/*
		 * Use the request object to make the request, either asynchronously
		 * (getAsync) or synchronously (get)
		 */
		final SettableFuture<ClientCredentials> responseFuture = request.getAsync();

		/* Add callbacks to handle success and failure */
		Futures.addCallback(responseFuture, new FutureCallback<ClientCredentials>() {
			public void onSuccess(ClientCredentials clientCredentials) {
				/* The tokens were retrieved successfully! */
				lu.log(Level.INFO, "Successfully retrieved an access token. " + clientCredentials.getAccessToken());
				lu.log(Level.INFO, "The access token expires in " + clientCredentials.getExpiresIn() + " seconds.");

				/*
				 * Set access token on the Api object so that it's used going
				 * forward
				 */
				api.setAccessToken(clientCredentials.getAccessToken());

				/*
				 * Please note that this flow does not return a refresh token.
				 * That's only for the Authorization code flow
				 */
			}

			public void onFailure(Throwable throwable) {
				/*
				 * An error occurred while getting the access token. This is
				 * probably caused by the client id or client secret is invalid.
				 */
			}
		});
	}

	/**
	 * Login that uses the authorization code flow described under
	 * https://developer.spotify.com/web-api/authorization-guide/. Advantage of
	 * this login is, that it allows to fetch user data.
	 */
	public void loginAutorizationGrant() {
		// Create an API instance. The default instance connects to
		// https://api.spotify.com/.
		api = Api.builder().clientId(pu.getProperty("clientId")).clientSecret(pu.getProperty("clientSecret"))
				.redirectURI(pu.getProperty("redirectUri")).build();

		/*
		 * Set the necessary scopes that the application will need from the user
		 */
		final List<String> scopes = Arrays.asList("user-read-private", "user-read-email", "playlist-read-private",
				"playlist-modify-private");

		/* Set a state. This is used to prevent cross site request forgeries. */
		final String state = Constants.spotifyState;

		/*
		 * Continue by sending the user to the authorizeURL, which will look
		 * something like https://accounts.spotify.com:443/authorize?client_id=
		 * 5fe01282e44241328a84e7c5cc169165&response_type=code&redirect_uri=
		 * https://example.com/callback&scope=user-read-private%20user-read-
		 * email&state=some-state-of-my-choice
		 */
		String authorizeURL = api.createAuthorizeURL(scopes, state);

		/* Application details necessary to get an access token */
		final String code = cliTokenQuestion(authorizeURL);

		/*
		 * Make a token request. Asynchronous requests are made with the
		 * .getAsync method and synchronous requests are made with the .get
		 * method. This holds for all type of requests.
		 */
		final SettableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = api
				.authorizationCodeGrant(code).build().getAsync();

		/* Add callbacks to handle success and failure */
		Futures.addCallback(authorizationCodeCredentialsFuture, new FutureCallback<AuthorizationCodeCredentials>() {
			public void onSuccess(AuthorizationCodeCredentials authorizationCodeCredentials) {
				/* The tokens were retrieved successfully! */
				lu.log(Level.INFO,
						"Successfully retrieved an access token. " + authorizationCodeCredentials.getAccessToken());
				lu.log(Level.INFO,
						"The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds.");
				lu.log(Level.INFO, "Luckily, I can refresh it using this refresh token. "
						+ authorizationCodeCredentials.getRefreshToken());

				/*
				 * Set the access token and refresh token so that they are used
				 * whenever needed
				 */
				api.setAccessToken(authorizationCodeCredentials.getAccessToken());
				api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
				pu.setProperty("accessToken", authorizationCodeCredentials.getAccessToken());
				pu.setProperty("refreshToken", authorizationCodeCredentials.getRefreshToken());

			}

			public void onFailure(Throwable throwable) {
				/*
				 * Let's say that the client id is invalid, or the code has been
				 * used more than once, the request will fail. Why it fails is
				 * written in the throwable's message.
				 */

			}
		});
	}

	/**
	 * Refresh an access token.
	 */
	public void loginRefreshGrant() {
		api = Api.builder().accessToken(pu.getProperty("accessToken")).refreshToken(pu.getProperty("refreshToken"))
				.build();
		api.refreshAccessToken();
	}

	/**
	 * Check if it is a valid login by reading account informations.
	 * 
	 * @return True if request was successful
	 */
	public boolean validLogin() {

		final CurrentUserRequest request = api.getMe().build();

		try {
			final User user = request.get();
			lu.log(Level.INFO, "Display name: " + user.getDisplayName());
			lu.log(Level.INFO, "Email: " + user.getEmail());

			lu.log(Level.INFO, "This account is a " + user.getProduct() + " account.");
			return true;
		} catch (Exception e) {
			lu.log(Level.SEVERE, "Something went wrong!" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * Download the track ids contain in a spotify playlist.
	 * 
	 * @param playlist
	 *            Id of the playlist
	 * @return List of strings with the track ids at spotify
	 */
	public ArrayList<String> downloadSpotifyPlaylistTrackIds(String playlist) {
		ArrayList<String> ids = new ArrayList<String>();

		PlaylistTracksRequest request = api.getPlaylistTracks(pu.getProperty("username"), playlist).build();
		try {
			final Page<PlaylistTrack> page = request.get();
			final List<PlaylistTrack> playlistTracks = page.getItems();
			for (PlaylistTrack playlistTrack : playlistTracks) {
				ids.add(playlistTrack.getTrack().getUri());
			}
		} catch (Exception e) {
			lu.log(Level.SEVERE, "Something went wrong!" + e.getMessage(), e);
		}

		return ids;
	}

	/**
	 * Search for a track at spotify in the german market.
	 * 
	 * @param track
	 *            Track to find
	 * @return Id at spotify
	 */
	public String searchTrack(TrackDownload track) {
		TrackSearchRequest request = api.searchTracks(track.title).market("DE").limit(3).build();

		String bestMatchId = null;
		double bestMatchValue = 0;

		try {
			final Page<Track> trackSearchResult = request.get();

			for (Track result : trackSearchResult.getItems()) {

				double currentValue = CompUtil.similarity(result.getName(), track.title)
						+ CompUtil.similarity(result.getArtists().get(0).getName(), track.interpret);
				lu.log(Level.INFO, result.getArtists().get(0).getName() + " - " + result.getName() + " ("
						+ result.getPopularity() + ", " + result.getUri() + ", " + currentValue + ")");
				if (currentValue > bestMatchValue) {
					bestMatchValue = currentValue;
					bestMatchId = result.getUri();
				}
			}
			lu.log(Level.INFO, "I got " + trackSearchResult.getTotal() + " results.");
		} catch (Exception e) {
			lu.log(Level.SEVERE, "Something went wrong!" + e.getMessage(), e);
		}

		return bestMatchId;
	}

	/**
	 * Adds tracks to a playlist at spotify.
	 * 
	 * @param idsNewTracks
	 *            List of strings with the track ids at spotify
	 */
	public void addTrackToPlaylist(List<String> idsNewTracks) {
		final AddTrackToPlaylistRequest request = api
				.addTracksToPlaylist(pu.getProperty("username"), pu.getProperty("playlist"), idsNewTracks).position(0)
				.build();
		try {
			request.get();
		} catch (Exception e) {
			lu.log(Level.SEVERE, "Something went wrong!" + e.getMessage(), e);
		}
	}

	/**
	 * Asks interactive via command line interface for the redirect url to get
	 * the code.
	 * 
	 * @param authorizeURL
	 *            First url for the spotify website call
	 * @return The code string
	 */
	public String cliTokenQuestion(String authorizeURL) {
		lu.log(Level.INFO, "Copy the following URL in a browser and login with your spotify account:");
		lu.log(Level.INFO, authorizeURL);
		System.out.print("Enter complete redirect URL:\n");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		int beginIndex = input.indexOf("code=") + 5;
		int endIndex = input.substring(beginIndex).indexOf("&") + beginIndex;
		scanner.close();
		return input.substring(beginIndex, endIndex);
	}

}
