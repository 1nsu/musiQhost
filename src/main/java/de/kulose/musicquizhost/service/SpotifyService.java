package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.config.SpotifyConfig;
import de.kulose.musicquizhost.mapper.SongMapper;
import de.kulose.musicquizhost.models.SongData;
import de.kulose.musicquizhost.models.spotify.Token;
import de.kulose.musicquizhost.models.spotify.SearchResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class SpotifyService {
    @Autowired
    SpotifyConfig config;
    @Autowired
    SongMapper songMapper;

    private final static String PLAYLIST_URL = "https://api.spotify.com/v1/playlists/%s?market=%s&fields=%s",
                         SEARCH_MODIFIER = "tracks.items(track(name, artists(name), id, album(id, name, release_date)))",
                         MARKET="DE";
    private final static long TIME_BUFFER = 60_000L;
    private final static List<String> PLAYLISTS = List.of("0HFjjN19YWx5Snx38K5I2v");

    private final Random random = new Random();
    private Token token;
    private long tokenFetchTime;
    private RestTemplate template = new RestTemplate();

    @PostConstruct
    private void initialize() {
        fetchToken();
    }

    public List<SongData> getSongs(int amount) {
        if (tokenIsExpired()) {
            fetchToken();
            log.warn("Token expired, re-fetching.");
        }

        template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<SearchResult> response = template.exchange(
                buildPlaylistUrl(PLAYLISTS.get(0)),
                HttpMethod.GET,
                requestEntity,
                SearchResult.class
        );

        SearchResult searchResult = response.getBody();
        List<SongData> allSongs = songMapper.searchResultToSongData(searchResult);
        List<SongData> chosenSongs = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            chosenSongs.add(allSongs.remove(random.nextInt(0, allSongs.size())));
        }

        return chosenSongs;
    }

    private void getToken() {
        template = new RestTemplate();
        token = template.postForObject(config.getTokenUrl(), createCredRequest(), Token.class);
    }

    private HttpEntity<MultiValueMap<String, String>> createCredRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","client_credentials");
        map.add("client_id", config.getClientId());
        map.add("client_secret", config.getClientSecret());

        return new HttpEntity<>(map, headers);
    }

    private void fetchToken() {
        getToken();
        tokenFetchTime = System.currentTimeMillis();
    }

    private boolean tokenIsExpired() {
        return (System.currentTimeMillis() - tokenFetchTime) > (token.getExpiresIn() * 1000L - TIME_BUFFER);
    }

    private String buildPlaylistUrl(String playlistId) {
        return String.format(PLAYLIST_URL, playlistId, MARKET, SEARCH_MODIFIER);
    }
}
