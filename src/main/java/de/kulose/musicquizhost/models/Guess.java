package de.kulose.musicquizhost.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Guess {
    private Player player;
    private List<String> guesses;
}
