package de.kulose.musicquizhost.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Track {
    private String id;
    private String name;
    private String albumName;
    private int releaseDate;
    private List<String> artists;

    @JsonProperty("album")
    private void unpackNameFromNestedObject(Map<String, String> album) {
        albumName = album.get("name");
        releaseDate = Integer.parseInt(album.get("release_date").substring(0, 4));
    }

    @JsonProperty("artists")
    private void unpackNameFromNestedObject(List<Map<String, String>> artists) {
        this.artists = artists.stream().map(m -> m.get("name")).collect(Collectors.toList());
    }

}