package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void save(Player player) {
        player.setExperience(player.getExperience());
        playerRepository.save(player);
    }

    @Transactional
    public Player update(Player player, Player updatedPlayer) throws IllegalArgumentException {
        if (updatedPlayer.getName() != null) player.setName(updatedPlayer.getName());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getTitle() != null) player.setTitle(updatedPlayer.getTitle());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getRace() != null) player.setRace(updatedPlayer.getRace());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getProfession() != null) player.setProfession(updatedPlayer.getProfession());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getBirthday() != null) player.setBirthday(updatedPlayer.getBirthday());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getBanned() != null) player.setBanned(updatedPlayer.getBanned());
        else throw new IllegalArgumentException();
        if (updatedPlayer.getExperience() != null) player.setExperience(updatedPlayer.getExperience());
        else throw new IllegalArgumentException();
        playerRepository.save(player);
        return player;
    }

    @Transactional
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    public boolean isPlayerValid(Player player) {
        return (player != null) && (player.getName() != null) && (player.getTitle() != null) && (!player.getName().isEmpty()) && (player.getName().length() > 0) &&
                (!player.getTitle().isEmpty()) && (player.getName().length() < 13) && (player.getTitle().length() > 0) && (player.getTitle().length() < 31) &&
                (player.getExperience() <= 10000000) && (player.getExperience() >= 0) && (player.getBirthday() != null) &&
                (isProdDateValid(player.getBirthday()));
    }

    private boolean isProdDateValid(Date prodDate) {
        final Date startProd = getDateForYear(2000);
        final Date endProd = getDateForYear(3000);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }
}
