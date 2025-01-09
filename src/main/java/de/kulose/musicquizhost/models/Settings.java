package de.kulose.musicquizhost.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    private Mode mode;
    private int maxPlayers;
    private boolean isPublic;
    @Size(min = 3, max = 20, message = "Rounds must be between 3 and 20.")
    private int rounds;
}
