package de.kulose.musicquizhost.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

    public void updatePoints(String roomId, String playerId, int roundIndex) {
        log.info("I need to calculate points");
    }
}
