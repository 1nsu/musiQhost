package de.kulose.musicquizhost;

import de.kulose.musicquizhost.config.SpotifyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SpotifyConfig.class})
public class MusicQHostApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicQHostApplication.class, args);
    }

}
