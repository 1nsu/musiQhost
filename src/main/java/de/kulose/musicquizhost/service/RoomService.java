package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.kulose.musicquizhost.util.Util.getRandomRoomName;

@Slf4j
@Service
public class RoomService {
    @Autowired
    SpotifyService spotifyService;
    @Autowired
    ValidationService validationService;

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
                .orElseThrow(() -> new IllegalArgumentException("Room does not exist."));
    }

    public Player getPlayer(String roomId, String playerId) {
        Room room = getRoom(roomId);
        return room.getPlayers().stream().filter(p -> p.getName().equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player does not exist."));
    }

    public Round getRound(String roomId, int roundIndex) {
        Room room = getRoom(roomId);
        return room.getRounds().stream().filter(r -> r.getIndex() == roundIndex).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Round does not exist."));
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

    public void submitAnswers(String id, String playerId, List<String> answers, int roundIndex) {
        Room room = getRoom(id);

        Round round = room.getRounds().get(roundIndex);
        Optional<Guess> guesses = round.getGuesses().stream().filter(guess -> guess.getPlayerId().equals(playerId)).findAny();

        if (guesses.isPresent()) {
            throw new IllegalArgumentException("Player already guessed in round " + roundIndex);
        } else {
            round.getGuesses().add(Guess.builder().playerId(playerId).guesses(answers).build());
        }

        validationService.updatePoints(id, playerId, roundIndex);
    }

    public Room startRoom(String id) {
        Room room = getRoom(id);
        List<SongData> songs = spotifyService.getSongs(room.getSettings().getRounds());
        room.setRounds(new ArrayList<>());

        for (int i = 0; i < songs.size(); i++) {
            Round round = Round.builder()
                    .song(songs.get(i))
                    .index(i)
                    .build();
            room.getRounds().add(round);
        }

        room.setStatus(Status.ACTIVE);
        return room;
    }

    public void readyPlayer(String id, String playerId) {
        Player player = getPlayer(id, playerId);

        if (!player.isReady()) {
            player.setReady(true);
        } else {
            throw new IllegalArgumentException("Player already ready.");
        }
    }
}
