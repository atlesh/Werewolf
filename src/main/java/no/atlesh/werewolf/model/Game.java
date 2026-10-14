package no.atlesh.werewolf.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Game {
  private final String gameCode;
  private final Player admin;
  private final List<Player> players;
  private final List<Role> deck;
  private final Phase phase;
  private final int round;
  private Map<Player, Player> votes;

  public Game(String gameCode, Player admin, Phase phase, int round) {
    this.gameCode = gameCode;
    this.admin = admin;
    this.phase = phase;
    this.round = round;
    this.players = new ArrayList<>();
    this.players.add(admin);
    this.deck = new ArrayList<>();
    this.votes = new HashMap<>();
  }

  public String getGameCode() {
    return gameCode;
  }

  public Player getAdmin() {
    return admin;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public List<Role> getDeck() {
    return deck;
  }

  public Phase getPhase() {
    return phase;
  }

  public int getRound() {
    return round;
  }

  public void runNight() {
    for  (Player p : orderNight()) {
      if(!p.getAlive()) continue;
      switch (p.getGameRole()) {
        case WEREWOLF -> runWerewolves();
        case SEER -> runSeer();
      }
    }
  }

  public List<Player> orderNight() {
    return players.stream()
        .filter(p -> p.getGameRole().getNightOrder() != 0)
        .sorted(Comparator.comparingInt(p -> p.getGameRole().getNightOrder()))
        .toList();
  }

  public void runWerewolves() {
    /*
    * All the werewolves need to be able to vote on who to kill.
    * If its a draw then they have to vote between the ones that they drew on.
    * If its a draw again, then they have to vote on the ones they haven't discussed.
    * If the same thing happens again then a random citizen gets killed.
    */
    List<Player> werewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() == Role.WEREWOLF)
        .toList();
    List<Player> notWerewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() != Role.WEREWOLF)
        .toList();

    if (werewolves.isEmpty() || notWerewolves.isEmpty()) return;

    List<Player> candidates = notWerewolves;
    List<Player> discussed = new ArrayList<>();

    for (int attempt = 1; attempt <= 3; attempt++) {
      List<Player> top = mostVoted(werewolves, candidates);
      if (top.size() == 1) {
        top.getFirst().setAlive(false);
        return;
      }
      discussed.addAll(top);
      if (attempt == 1) {
        candidates = top;
      } else {
        candidates = notWerewolves.stream()
            .filter(p -> !discussed.contains(p))
            .toList();
        if (candidates.isEmpty()) break;
      }
    }

    Player victim = notWerewolves.get(new Random().nextInt(notWerewolves.size()));
    victim.setAlive(false);
  }

  private List<Player> mostVoted(List<Player> voters, List<Player> candidates) {
    Map<Player, Integer> votes = new HashMap<>();
    for (Player voter : voters) {
      Player target = voter.vote(candidates);
      votes.merge(target, 1, Integer::sum);
    }
    int max = Collections.max(votes.values());
    return votes.entrySet().stream()
        .filter(e -> e.getValue() == max)
        .map(Map.Entry::getKey)
        .toList();
  }

  public void runSeer() {
    //
  }

  public void addPlayer(Player player) {}

  public void removePlayer(Player player) {}

  public void addRole(Role role) {}

  public void removeRole(Role role) {}

  /*public List<Player> getWinners() {
    return winners;
  }

  public List<Player> getLosers() {
    return losers;
  }*/
}
