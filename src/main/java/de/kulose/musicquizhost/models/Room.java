package de.kulose.musicquizhost.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    public static final String NO_HOST_VALIDATION_MESSAGE = "Host cannot be null",
            NO_SETTINGS_VALIDATION_MESSAGE = "Settings cannot be null";

    String id;
    @NotNull(message = NO_HOST_VALIDATION_MESSAGE)
    private Player host;
    private Set<Player> players;
    private List<Round> rounds;
    private int currentRound;
    @NotNull(message = NO_SETTINGS_VALIDATION_MESSAGE)
    private Settings settings;
    private Status status;
    private int activeRound = 0;
    @JsonIgnore
    private long closeTime;
}
