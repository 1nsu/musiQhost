package de.kulose.musicquizhost.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Tracks {
    @JsonProperty("items")
    private List<TrackContainer> items;
}