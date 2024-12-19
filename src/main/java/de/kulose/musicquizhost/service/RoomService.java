package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.kulose.musicquizhost.util.Util.getRandomRoomName;

@Service
public class RoomService {
    @Autowired
    SpotifyService spotifyService;

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

    public Room updateRoom(String id, Room room) {
        synchronized (rooms) {
            Room updatedRoom = getRoom(id);
            updatedRoom.setStatus(room.getStatus());
            updatedRoom.setHost(room.getHost());
            updatedRoom.setPlayers(room.getPlayers());
            updatedRoom.setSettings(room.getSettings());
            updatedRoom.setRounds(room.getRounds());
            return updatedRoom;
        }
    }

    public Room createRoom(Room room) {
        synchronized (rooms) {
            room.setId(getRandomRoomName());
            room.setPlayers(Set.of(room.getHost()));
            room.setStatus(Status.OPEN);
            rooms.add(room);
            return room;
        }
    }

    public void closeRoom(String id, String playerName) {
        synchronized (rooms) {
            Room closingRoom = getRoom(id);

            if (closingRoom.getStatus() == Status.CLOSED) {
                throw new IllegalArgumentException("Room is already closed.");
            }

            if (closingRoom.getHost().getName().equals(playerName)) {
                closingRoom.setStatus(Status.CLOSED);
            } else {
                throw new IllegalArgumentException("Only the host may close the room");
            }
        }
    }

    public Room joinRoom(String id, Player player) {
        synchronized (rooms) {
            Room joiningRoom = getRoom(id);

            if (joiningRoom.getPlayers().stream().anyMatch(p -> p == player)) {
                throw new IllegalArgumentException("Player " + player + " already in room");
            }

            if (joiningRoom.getStatus() == Status.OPEN) {
                joiningRoom.setPlayers(
                        Stream.concat(joiningRoom.getPlayers().stream(),
                                        Stream.of(player))
                                .collect(Collectors.toSet())
                );
            } else {
                throw new IllegalArgumentException("Only open rooms can be joined, room is " + joiningRoom.getStatus().getText());
            }
            return joiningRoom;
        }
    }

    public void submitAnswers(String id, String playerId, List<String> answers) {

    }

    public Room startRoom(String id) {
        Room room = getRoom(id);
        List<SongData> songs = spotifyService.getSongs(5);
        room.setStatus(Status.ACTIVE);
        return room;
    }
}
