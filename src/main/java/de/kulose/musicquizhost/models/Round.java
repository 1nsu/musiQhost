package de.kulose.musicquizhost.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Round {
    private List<Guess> guesses;
    private SongData song;
    private int index;
    private long remainingTime;
    @JsonIgnore
    private long startTime;

    public void removePlayer(Player player) {
        guesses.removeIf(guess -> guess.getPlayerId().equals(player.getName()));
    }
}
