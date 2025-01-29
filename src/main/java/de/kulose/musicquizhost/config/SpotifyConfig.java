package de.kulose.musicquizhost.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@Slf4j
@EnableScheduling
@AllArgsConstructor
@ConfigurationProperties(prefix = "spotify")
public class SpotifyConfig {
    @NotBlank
    final private String clientId, clientSecret, tokenUrl;
}
