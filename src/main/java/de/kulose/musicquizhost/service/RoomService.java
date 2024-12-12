package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.models.Player;
import de.kulose.musicquizhost.models.Room;
import de.kulose.musicquizhost.models.Status;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.kulose.musicquizhost.util.Util.getRandomRoomName;

@Service
public class RoomService {
    private final List<Room> rooms = Collections.synchronizedList(new ArrayList<>());

    public List<Room> getRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(Status.OPEN) && room.getSettings().isPublic())
                .collect(Collectors.toList());
    }

    public Room getRoom(String id) {
        return rooms.stream()
                .filter(room -> room.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public Room updateRoom(String id, Room updatedRoom) {
        synchronized (rooms) {
            rooms.stream()
                    .filter(room -> room.getId().equals(id))
                    .findFirst()
                    .ifPresentOrElse(
                            room -> {
                                room.setStatus(updatedRoom.getStatus());
                                room.setHost(updatedRoom.getHost());
                                room.setPlayers(updatedRoom.getPlayers());
                                room.setSettings(updatedRoom.getSettings());
                                room.setRounds(updatedRoom.getRounds());
                            },
                            () -> {
                                throw new NoSuchElementException(String.format("Room with id=%s not available.", id));
                            }
                    );
        }

        return rooms.stream()
                .filter(room -> room.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public Room createRoom(Room room) {
        room.setId(getRandomRoomName());
        room.setPlayers(Set.of(room.getHost()));
        room.setStatus(Status.OPEN);
        rooms.add(room);
        return room;
    }

    public void closeRoom(String id, Player player) {
    }

    public Room joinRoom(String id, Player player) {
        return null;
    }

    public void submitAnswers(String id, String playerId, List<String> answers) {
    }
}
