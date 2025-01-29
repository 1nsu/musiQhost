package de.kulose.musicquizhost.service;

import de.kulose.musicquizhost.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(fixedDelay = 60_000)
    public void roomCleanup() {
        synchronized (rooms) {
            List<String> roomIds = rooms.stream().map(Room::getId).toList();
            for (String roomId : roomIds) {
                getRoom(roomId);
            }
        }
        log.info("Cleaning up rooms.");
    }

    public List<Room> getRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(Status.OPEN) && room.getSettings().isPublic())
                .collect(Collectors.toList());
    }

    public Room getRoom(String id) {
        Room room = rooms.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room does not exist."));

        if (room.getStatus() == Status.ACTIVE) {
            Round round = room.currentRound();

            if (room.getSettings().getMode() == Mode.FASTEST_STOPS && !round.getGuesses().isEmpty()) {
                round.setRemainingTime(0);
            } else {
                long remainingTime = (room.getSettings().getMaxRoundTime() * 1000L) - (System.currentTimeMillis() - round.getStartTime());
                round.setRemainingTime(remainingTime < 0 ? 0 : remainingTime);
            }
        }

        if (room.getStatus() == Status.OPEN && System.currentTimeMillis() - room.getRoomCreationTime() > 600_000) {
            room.getPlayers().forEach(player -> leaveRoom(room.getId(), player.getName()));
        }

        if (isTimedOut(room)) {
            kickInactivePlayers(room);
        }

        if (isRoomReadyForNextRound(room)) {
            advanceRound(room);
        }

        return room;
    }

    private boolean isRoomReadyForNextRound(Room room) {
        return room.getPlayers().stream().allMatch(Player::isReady)
                && room.getStatus() == Status.ACTIVE
                && room.currentRound().getGuesses().size() == room.getPlayers().size();
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
            room.setPlayers(new HashSet<>(Set.of(room.getHost())));
            room.setStatus(Status.OPEN);
            room.setRoomCreationTime(System.currentTimeMillis());
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

            if (joiningRoom.getSettings().getMaxPlayers() <= joiningRoom.getPlayers().size()) {
                throw new IllegalArgumentException("Room is full.");
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
        Room room = getRoom(id);

        if (room.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("Room has not started yet");
        }

        Round round = room.currentRound();

        Optional<Guess> guesses = round.getGuesses().stream()
                .filter(guess -> guess.getPlayerId().equals(playerId))
                .findAny();

        if (guesses.isPresent()) {
            throw new IllegalArgumentException("Player already guessed in round.");
        } else {
            round.getGuesses().add(new Guess(playerId, answers));
        }

        if (room.currentRound().getGuesses().size() == room.getPlayers().size()) {
            waitForNextRound(room);
        } else if (isTimedOut(room)) {
            kickInactivePlayers(room);
        }
    }

    public Room startRoom(String id) {
        Room room = getRoom(id);

        if (room.getStatus() == Status.ACTIVE) {
            throw new IllegalArgumentException("Room already active");
        }

        if (!room.getPlayers().stream().allMatch(Player::isReady)) {
            throw new IllegalArgumentException("Not all players are ready.");
        }

        List<SongData> songs = spotifyService.getSongs(room.getSettings().getRounds());
        room.setRounds(new ArrayList<>());

        for (int i = 1; i <= songs.size(); i++) {
            Round round = Round.builder()
                    .song(songs.get(i-1))
                    .index(i)
                    .remainingTime(-1)
                    .guesses(new ArrayList<>())
                    .build();
            room.getRounds().add(round);
        }

        room.setStatus(Status.ACTIVE);
        room.setCurrentRoundNumber(1);
        room.currentRound()
                .setStartTime(System.currentTimeMillis());
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

    private void waitForNextRound(Room room) {
        room.getPlayers().forEach(player -> player.setReady(false));
    }

    private void advanceRound(Room room) {
        room.setCurrentRoundNumber(room.getSettings().getRounds() > room.getCurrentRoundNumber() ? room.getCurrentRoundNumber() + 1 : 0);

        if (room.getCurrentRoundNumber() == 0) {
            room.setStatus(Status.CLOSED);
        } else {
            room.currentRound().setStartTime(System.currentTimeMillis());
        }
    }

    private boolean isTimedOut(Room room) {
        return room.currentRound() != null
                && System.currentTimeMillis() - room.currentRound().getStartTime() > (room.getSettings().getMaxRoundTime() * 1000L + 30_000);
    }

    private void kickInactivePlayers(Room room) {
        List<String> activePlayers = room.currentRound().getGuesses().stream().map(Guess::getPlayerId).toList();
        List<Player> inactivePlayers = room.getPlayers().stream().filter(player -> !activePlayers.contains(player.getName())).toList();

        if (inactivePlayers.isEmpty()) {
            return;
        }

        inactivePlayers.forEach(player -> room.getRounds().forEach(round -> round.removePlayer(player)));
        room.getPlayers().removeIf(inactivePlayers::contains);

        if (room.getPlayers().isEmpty()) {
            room.setStatus(Status.CLOSED);
        }
    }

    public void leaveRoom(String id, String playerId) {
        Room room = getRoom(id);
        room.getPlayers().removeIf(player -> player.getName().equals(playerId));

        if (room.getStatus() == Status.ACTIVE) {
            room.getRounds().forEach(round -> round.removePlayer(playerId));
        }

        if (room.getPlayers().isEmpty()) {
            room.setStatus(Status.CLOSED);
        }
    }
}
