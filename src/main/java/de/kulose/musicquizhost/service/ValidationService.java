package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.models.Room;
import de.kulose.musicquizhost.models.Round;
import de.kulose.musicquizhost.models.SongData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {
    @Autowired
    RoomService roomService;

    public void updatePoints(String roomId, String playerId, int roundIndex) {
        Round round = roomService.getRound(roomId, roundIndex);
    }
}
