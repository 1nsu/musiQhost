package de.kulose.musicquizhost.mapper;

import de.kulose.musicquizhost.models.SongData;
import de.kulose.musicquizhost.models.spotify.SearchResult;
import de.kulose.musicquizhost.models.spotify.TrackContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SongMapper {
    public List<SongData> searchResultToSongData(SearchResult searchResult) {
        List<SongData> songs = new ArrayList<>();
        for (TrackContainer container : searchResult.getTracks().getItems()) {
            songs.add(
                    SongData.builder()
                    .id(container.getTrack().getId())
                    .album(container.getTrack().getAlbumName())
                    .title(container.getTrack().getName())
                    .releaseYear(container.getTrack().getReleaseDate())
                    .artists(container.getTrack().getArtists())
                    .build()
            );
        }
        return songs;
    }
}
