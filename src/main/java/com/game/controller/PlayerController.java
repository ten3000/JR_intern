package com.game.controller;

import com.game.dto.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("rest/players")
    public List<Player> getPlayers(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                   @RequestParam(value = "order", required = false) PlayerOrder order,
                                   @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                   @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<Player> players = playerService.findAll(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        List<Player> sortedPlayers = playerService.sortPlayers(players, order);
        return playerService.getPage(sortedPlayers, pageNumber, pageSize);
    }

    @GetMapping("/rest/players/count")
    public Integer countPlayers(@RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "race", required = false) Race race,
                                @RequestParam(value = "profession", required = false) Profession profession,
                                @RequestParam(value = "after", required = false) Long after,
                                @RequestParam(value = "before", required = false) Long before,
                                @RequestParam(value = "banned", required = false) Boolean banned,
                                @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerService.findAll(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    @GetMapping("/rest/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id) {
        if (id == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Player player = playerService.findOne(id);
        if (player == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("rest/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        playerService.save(player);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("rest/players/{id}")
    public Player updatePlayer(@RequestBody PlayerDTO playerDTO, @PathVariable(value = "id") Long id) {
        playerService.validateId(id);
        return playerService.update(playerDTO, id);
    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") Long id) {
        return playerService.delete(id);
    }
}
