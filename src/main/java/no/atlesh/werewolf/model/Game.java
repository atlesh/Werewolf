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
  private Phase phase;
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

  public List<Player> orderNight() {
    return players.stream()
        .filter(p -> p.getGameRole().getNightOrder() != 0)
        .sorted(Comparator.comparingInt(p -> p.getGameRole().getNightOrder()))
        .toList();
  }

  private void voting(List<Player> voters, List<Player> voted) {
    if (voters.isEmpty() || voted.isEmpty()) return;

    List<Player> candidates = voted;
    List<Player> discussed = new ArrayList<>();

    for (int attempt = 1; attempt <= 3; attempt++) {
      List<Player> top = mostVoted(voters, candidates);
      if (top.size() == 1) {
        top.getFirst().setAlive(false);
        return;
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

  public boolean checkEnded() {
    List<Player> alive = players.stream().filter(Player::getAlive).toList();

    int good = 0;
    int bad = 0;
    int depressed = 0;
    for  (Player p : alive) {
      if (p.getGameRole().getSide().equals(Side.BAD)) {
        bad++;
      }
      else if (p.getGameRole().getSide().equals(Side.GOOD)) {
        good++;
      }
      else {
        depressed++;
      }
    }

    if (depressed == 0) {
      return true;
    } else if (bad == good + depressed) {
      return true;
    } else if (bad == 0) {
      return true;
    } else {
      return false;
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
    for  (Player p : orderNight()) {
      if(!p.getAlive()) continue;
      switch (p.getGameRole()) {
        case WEREWOLF -> runWerewolves();
        case SEER -> runSeer(p);
      }
    }
  }

  public void runDay() {
    System.out.println("Dag");
    phase = Phase.VOTING;
  }

  public void runVote() {
    List<Player> voters = this.players.stream()
        .filter(Player::getAlive)
        .toList();

    voting(voters, voters);

    if (checkEnded()) {
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

    if (checkEnded()) {
      phase = Phase.ENDED;
    } else {
      phase = Phase.DAY;
    }
  }

  public void runSeer(Player player) {
    List<Player> candidates = players.stream()
        .filter(Player::getAlive)
        .toList();

    System.out.println(player.vote(candidates).getGameRole().getName());
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
