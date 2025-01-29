package de.kulose.musicquizhost.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    @NotNull
    private Mode mode;
    @NotNull
    private int maxPlayers;
    @NotNull
    private boolean isPublic;
    @NotNull
    @Max(value = 90, message = "Max round time must be 90 or less.")
    @Min(value = 10, message = "Min round time must be 10 or more.")
    private int maxRoundTime;
    @NotNull
    @Max(value = 21, message = "Rounds must be between 3 and 21.")
    @Min(value = 3, message = "Rounds must be between 3 and 21.")
    private int rounds;
}
