# SpotifyPlaylistRobot
Inspired by this article https://www.heise.de/ct/ausgabe/2016-19-Google-Play-Music-und-Spotify-per-Skript-steuern-3305800.html (only in german), I coded an own little project that uses the spotify web api. Because I prefered java, it is build upon https://github.com/thelinmichael/spotify-web-api-java. The purpose of this application is to download the 1live (german radio station) plan b playlist and update a corresponding one on spotify automatically.

Hints:
- Its a maven project.
- For the first run you have to set the following values in SpotifyPlaylistRobot.cfg: clientId, clientSecret, playlist, username, redirectUri. The file must be located in the same directory as the jar.

More information is available at my blog under https://wiesing.net/index.php/2017/03/19/spotifyplaylistrobot/ (only in german).
