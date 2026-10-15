package no.atlesh.werewolf.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game {
  private final String gameCode;
  private final Player admin;
  private final List<Player> players;
  private final List<Role> deck;
  private Phase phase;
  private final int round;
  private List<Player> winners;
  private List<Player> losers;

  public Game(String gameCode, Player admin, Phase phase, int round) {
    this.gameCode = gameCode;
    this.admin = admin;
    this.phase = phase;
    this.round = round;
    this.players = new ArrayList<>();
    this.players.add(admin);
    this.deck = new ArrayList<>();
    this.winners = new ArrayList<>();
    this.losers = new ArrayList<>();
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

  public List<Role> orderNight() {
    return players.stream()
            .filter(Player::getAlive)
            .map(Player::getGameRole)
            .filter(r -> r.getNightOrder() != 0)
            .distinct()
            .sorted(Comparator.comparingInt(Role::getNightOrder))
            .toList();
  }

  private Player voting(List<Player> voters, List<Player> voted) {
    if (voters.isEmpty() || voted.isEmpty()) return null;

    List<Player> candidates = voted;
    List<Player> discussed = new ArrayList<>();

    for (int attempt = 1; attempt <= 3; attempt++) {
      List<Player> top = mostVoted(voters, candidates);
      if (top.size() == 1) {
        top.getFirst().setAlive(false);
        return top.getFirst();
      }
      discussed.addAll(top);
      if (attempt == 1) {
        candidates = top;
      } else {
        candidates = voted.stream()
            .filter(p -> !discussed.contains(p))
            .toList();
        if (candidates.isEmpty()) break;
      }
    }

    Player victim = voted.get(new Random().nextInt(voted.size()));
    victim.setAlive(false);
    return victim;
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

  public Side checkEnded() {
    List<Player> alive = players.stream().filter(Player::getAlive).toList();

    int good = 0;
    int bad = 0;
    for  (Player p : alive) {
      if (p.getGameRole().getSide().equals(Side.BAD)) {
        bad++;
      } else {
        good++;
      }
    }

    if (bad == 0) {
      return Side.GOOD;
    } else if (bad >= good) {
      return Side.BAD;
    } else {
      return null;
    }
  }

  public void runLobby() {
    // A player needs to be able to join the lobby with the right code
    // The admin needs to be able to remove a Player og Players can leave lobby
    // The admin needs to be able to pick roles for the deck and remove them
    // Click start and runGame()
  }

  public void runGame() {
    phase = Phase.NIGHT;
    boolean done  = false;
    while (!done) {
      switch (phase) {
        case NIGHT -> runNight();
        case DAY -> runDay();
        case VOTING -> runVote();
        case ENDED -> done = true;
      }
    }
  }

  public void runNight() {
    for  (Role r : orderNight()) {
      switch (r) {
        case WEREWOLF -> runWerewolves();
        case SEER -> runSeer();
      }
    }
  }

  public void runDay() {
    System.out.println("Daytime!");
    phase = Phase.VOTING;
  }

  public void runVote() {
    List<Player> voters = this.players.stream()
        .filter(Player::getAlive)
        .toList();

    Player victim = voting(voters, voters);

    assert victim != null;
    if (victim.getGameRole().getSide().equals(Side.DEPRESSED)) {
      winners = players.stream()
          .filter(p -> !p.getGameRole().getSide().equals(Side.DEPRESSED))
          .toList();
      losers = players.stream()
          .filter(p -> p.getGameRole().getSide().equals(Side.DEPRESSED))
          .toList();
      phase = Phase.ENDED;
    } else if (checkEnded().equals(Side.GOOD)) {
      winners = players.stream()
          .filter(p -> !p.getGameRole().getSide().equals(Side.GOOD))
          .toList();
      losers = players.stream()
          .filter(p -> p.getGameRole().getSide().equals(Side.BAD))
          .toList();
      phase = Phase.ENDED;
    } else if (checkEnded().equals(Side.BAD)) {
      winners = players.stream()
          .filter(p -> !p.getGameRole().getSide().equals(Side.BAD))
          .toList();
      losers = players.stream()
          .filter(p -> p.getGameRole().getSide().equals(Side.BAD))
          .toList();
      phase = Phase.ENDED;
    } else {
      phase = Phase.NIGHT;
    }
  }

  public void runWerewolves() {
    List<Player> werewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() == Role.WEREWOLF)
        .toList();
    List<Player> notWerewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() != Role.WEREWOLF)
        .toList();

    voting(werewolves, notWerewolves);

    if (checkEnded().equals(Side.BAD)) {
      winners = players.stream()
          .filter(p -> !p.getGameRole().getSide().equals(Side.BAD))
          .toList();
      losers = players.stream()
          .filter(p -> p.getGameRole().getSide().equals(Side.BAD))
          .toList();
      phase = Phase.ENDED;
    } else {
      phase = Phase.DAY;
    }
  }

  public void runSeer() {
    Player seer = null;
    for  (Player p : players) {
      if (p.getGameRole() == Role.SEER) {
        seer = p;
      }
    }

    List<Player> candidates = players.stream()
        .filter(Player::getAlive)
        .toList();

    assert seer != null;
    System.out.println(seer.vote(candidates).getGameRole().getName());
  }

  public void addPlayer(Player player) {
    players.add(player);
  }

  public void removePlayer(Player player) {
    players.remove(player);
  }

  public void addRole(Role role) {
    deck.add(role);
  }

  public void removeRole(Role role) {
    deck.remove(role);
  }

}
