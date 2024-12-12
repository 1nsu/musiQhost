package de.kulose.musicquizhost.controller;

import de.kulose.musicquizhost.models.Player;
import de.kulose.musicquizhost.models.Room;
import de.kulose.musicquizhost.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    RoomService roomService;

    @Operation(summary = "Get a list of rooms", description = "Returns a list of publicly available rooms that have the open status")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Room> getRooms() {
        return roomService.getRooms();
    }

    @Operation(summary = "Get a room by id", description = "Returns a room with the given id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Room getRoomById(@PathVariable("id") String id) {
        return roomService.getRoom(id);
    }

    @Operation(summary = "Updates a room", description = "Updates a room with given values")
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Room updateRoom(@PathVariable("id") String id, @RequestBody @Valid @NotNull(message = "Room must be provided") Room room) {
        return roomService.updateRoom(id, room);
    }

    @Operation(summary = "Creates a room", description = "Creates a new room with the given settings and player host")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Room createRoom(@RequestBody @NotNull(message = "Room must be provided") Room room) {
        return roomService.createRoom(room);
    }

    @Operation(summary = "Closes a room", description = "Closes a room by the player host")
    @DeleteMapping(path = "/{id}")
    public void closeRoom(@PathVariable("id") String id, @RequestBody @Valid @NotNull(message = "Player host must be provided") Player player) {
        roomService.closeRoom(id, player);
    }

    @Operation(summary = "Joins a room", description = "Joins a given player to an open room")
    @PutMapping(path = "/{id}")
    public Room joinRoom(@PathVariable("id") String id, @RequestBody @Valid @NotNull(message = "Player host must be provided") Player player) {
        return roomService.joinRoom(id, player);
    }

    @Operation(summary = "Submit answers", description = "Submits answers for a round for the given player")
    @PostMapping(path = "/{id}/{playerId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void submitAnswers(@PathVariable("id") String id, @PathVariable("playerId") String playerId, @RequestBody @Valid @NotNull(message = "Answers must be provided") @NotNull(message = "Answers must be provided, even if empty") List<String> answers) {
        roomService.submitAnswers(id, playerId, answers);
    }
}
