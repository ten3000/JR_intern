package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.dto.PlayerDTO;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.InvalidIdException;
import com.game.exception.NoSuchPlayerException;
import com.game.exception.ValidationException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> findAll(String name,
                                String title,
                                Race race,
                                Profession profession,
                                Long after,
                                Long before,
                                Boolean banned,
                                Integer minExperience,
                                Integer maxExperience,
                                Integer minLevel,
                                Integer maxLevel) {
        Date afterDate = after == null ? null : new Date(after);
        Date beforeDate = before == null ? null : new Date(before);
        List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            list.add(player);
        });
        return list;
    }

    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case ID:
                        return player1.getId().compareTo(player2.getId());
                    case NAME:
                        return player1.getName().compareTo(player2.getName());
                    case LEVEL:
                        return player1.getLevel().compareTo(player2.getLevel());
                    case BIRTHDAY:
                        return player1.getBirthday().compareTo(player2.getBirthday());
                    case EXPERIENCE:
                        return player1.getExperience().compareTo(player2.getExperience());
                    default:
                        return 0;
                }
            });
        }
        return players;
    }

    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        Integer page = pageNumber == null ? 0 : pageNumber;
        Integer size = pageSize == null ? 3 : pageSize;
        int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    public Player findOne(Long id) {
        Optional<Player> foundPLayer = playerRepository.findById(id);
        return foundPLayer.orElse(null);
    }

    @Transactional
    public ResponseEntity<Player> save(Player player) {
        player.setExperience(player.getExperience());
        playerRepository.save(player);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public Player update(PlayerDTO playerDTO, Long id) {
        Player player = findOne(id);

        if (playerDTO.getName() != null && isNameLenValid(playerDTO)) {
            player.setName(playerDTO.getName());
        }
        if (playerDTO.getTitle() != null && isTitleLenValid(playerDTO)) {
            player.setTitle(playerDTO.getTitle());
        }
        if (playerDTO.getRace() != null) {
            player.setRace(playerDTO.getRace());
        }
        if (playerDTO.getProfession() != null) {
            player.setProfession(playerDTO.getProfession());
        }

        if (playerDTO.getBirthday() != null) {
            validateBirthday(playerDTO);
            player.setBirthday(new Date(playerDTO.getBirthday()));
        }

        if (playerDTO.getBanned() != null) {
            player.setBanned(playerDTO.getBanned());
        }

        if (playerDTO.getExperience() != null) {
            validateExperience(playerDTO);
            player.setExperience(playerDTO.getExperience());
        }
        return playerRepository.saveAndFlush(player);
    }

    @Transactional
    public ResponseEntity<Player> delete(Long id) {
        if (id == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (id < 0 || id > playerRepository.findAll().size()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Optional<Player> foundPLayer = playerRepository.findById(id);
        if (!foundPLayer.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        playerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public boolean isPlayerValid(Player player) {
        return (player != null) && (player.getName() != null) && (player.getTitle() != null) && (!player.getName().isEmpty()) && (player.getName().length() > 0) &&
                (!player.getTitle().isEmpty()) && (player.getName().length() < 13) && (player.getTitle().length() > 0) && (player.getTitle().length() < 31) &&
                (player.getExperience() <= 10000000) && (player.getExperience() >= 0) && (player.getBirthday() != null) && (player.getBirthday().getTime() > 0);
    }

    boolean isBirthdayValid(PlayerDTO playerDTO) {
        if (playerDTO.getBirthday() < 0) {
            return false;
        }
        Date date = new Date(playerDTO.getBirthday());
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();

        return date.getTime() >= 0 && (year >= 2000 && year <= 3000);
    }

    public void validateId(Long id) {
        if (id <= 0) {
            throw new InvalidIdException();
        } else if (id > playerRepository.count() || !playerRepository.findById(id).isPresent()) {
            throw new NoSuchPlayerException();
        }
    }

    boolean isNameLenValid(PlayerDTO playerDTO) {
        int nameLen = playerDTO.getName().trim().length();
        return nameLen > 0 && nameLen <= 12;
    }

    boolean isTitleLenValid(PlayerDTO playerDTO) {
        int titleLen = playerDTO.getTitle().trim().length();
        return titleLen > 0 && titleLen <= 30;
    }

    void validateBirthday(PlayerDTO playerDTO) {
        if (!isBirthdayValid(playerDTO)) {
            throw new ValidationException();
        }
    }

    void validateExperience(PlayerDTO playerDTO) {
        if (!isExperienceValid(playerDTO)) {
            throw new ValidationException();
        }
    }

    boolean isExperienceValid(PlayerDTO playerDTO) {
        return playerDTO.getExperience() >= 0 && playerDTO.getExperience() <= 10000000;
    }
}
