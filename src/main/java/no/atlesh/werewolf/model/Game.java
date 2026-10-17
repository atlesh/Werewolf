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
  private int round;
  private List<Player> winners;
  private List<Player> losers;

  public Game(String gameCode, Player admin) {
    this.gameCode = gameCode;
    this.admin = admin;
    this.phase = Phase.LOBBY;
    this.round = 1;
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

  public List<Player> getWinners() {
    return winners;
  }

  public List<Player> getLosers() {
    return losers;
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

    return voted.get(new Random().nextInt(voted.size()));
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
      if (p.getGameRole().getSide() == Side.BAD) {
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

  public void dealRoles() {
    if (players.size() < 5)
      throw  new IllegalStateException("Cannot start with less than 5 players.");
    if (players.size() != deck.size())
      throw new IllegalStateException("Need same amount of players as size of deck.");

    int bad = 0;
    int good = 0;
    for (Role r : deck) {
      if (r.getSide() == Side.BAD) {
        bad++;
      } else good++;
    }

    if (bad == 0 || good == 0 || bad >= good)
      throw new IllegalStateException("Deck rules not followed.");


    List<Role> roles = new ArrayList<>(deck);
    Collections.shuffle(roles);
    for (int i = 0; i  < roles.size(); i++) {
      players.get(i).setGameRole(roles.get(i));
    }
  }

  public void startGame() {
    if (phase != Phase.LOBBY) {
      throw  new IllegalStateException("Game has already started.");
    }
    runGame();
  }

  public void endGame(Side winSide) {
    winners = players.stream()
        .filter(p -> p.getGameRole().getSide() == winSide)
        .toList();
    losers = players.stream()
        .filter(p -> p.getGameRole().getSide() != winSide)
        .toList();
    phase = Phase.ENDED;
  }

  private void runGame() {
    dealRoles();
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

  private void runNight() {
    for  (Role r : orderNight()) {
      if (phase == Phase.ENDED) break;
      switch (r) {
        case WEREWOLF -> runWerewolves();
        case SEER -> runSeer();
      }
    }
  }

  private void runDay() {
    phase = Phase.VOTING;
  }

  private void runVote() {
    List<Player> voters = this.players.stream()
        .filter(Player::getAlive)
        .toList();

    Player victim = voting(voters, voters);
    if (victim != null) {
      victim.setAlive(false);
      Side winSide = checkEnded();
      if (victim.getGameRole().getSide() == Side.DEPRESSED) {
        endGame(Side.DEPRESSED);
      } else if (winSide != null) {
        endGame(winSide);
      } else {
        round++;
        phase = Phase.NIGHT;
      }
    }
  }

  private void runWerewolves() {
    List<Player> werewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() == Role.WEREWOLF)
        .toList();
    List<Player> notWerewolves = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() != Role.WEREWOLF)
        .toList();

    Player victim = voting(werewolves, notWerewolves);
    if (victim != null) victim.setAlive(false);

    Side winSide = checkEnded();
    if (winSide != null) {
      endGame(winSide);
    } else {
      phase = Phase.DAY;
    }
  }

  private void runSeer() {
    Player seer = null;
    for (Player p : players) {
      if (p.getGameRole() == Role.SEER && p.getAlive()) {
        seer = p;
      }
    }
    if (seer == null) {
      return;
    }

    List<Player> candidates = players.stream()
        .filter(Player::getAlive)
        .filter(p -> p.getGameRole() != Role.SEER)
        .toList();

    String seen = seer.vote(candidates).getGameRole().getName();

  }

  public void addPlayer(Player player) {
    if (phase == Phase.LOBBY && !players.contains(player)) players.add(player);
  }

  public void removePlayer(Player player1, Player player2) {
    if (player1 != admin)
      throw new IllegalArgumentException("Only admin can remove players.");

    if (player2.equals(admin))
      throw new IllegalArgumentException("Admin cannot remove themself from game.");

    if (phase == Phase.LOBBY) {
      players.remove(player2);
    } else throw new IllegalArgumentException("Not in Lobby");
  }

  public void addRole(Player player, Role role) {
    if (player != admin) throw new IllegalArgumentException("Only admin can add roles.");

    if (phase == Phase.LOBBY) {
      deck.add(role);
    } else throw new IllegalArgumentException("Not in Lobby");
  }

  public void removeRole(Player player, Role role) {
    if (player != admin) throw new IllegalArgumentException("Only admin can remove roles.");

    if (phase == Phase.LOBBY) {
      boolean removed = false;
      for (Role r : deck) {
        if (r.equals(role) && !removed) {
          deck.remove(r);
          removed = true;
        }
      }
    } else throw new IllegalArgumentException("Not in Lobby");
  }

}
