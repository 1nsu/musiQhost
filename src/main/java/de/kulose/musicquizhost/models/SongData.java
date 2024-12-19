package de.kulose.musicquizhost.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SongData {
    String title, album, id;
    List<String> artists;
    int releaseYear;
}
