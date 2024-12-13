package de.kulose.musicquizhost.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Slf4j
@AllArgsConstructor
@ConfigurationProperties(prefix = "spotify")
public class SpotifyConfig {
    @NotBlank
    final private String clientId, clientSecret, tokenUrl;
}
