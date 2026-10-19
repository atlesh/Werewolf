package no.atlesh.werewolf.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import no.atlesh.werewolf.model.nightAction.witch.WitchPotion;

public class Game {
  private final String gameCode;
  private final Player admin;
  private final List<Player> players;
  private final List<Role> deck;
  private List<Role> nightQueue;
  private int nightStep;
  private Phase phase;
  private int round;
  private List<Player> winners;
  private List<Player> losers;
  private Vote currentVote;
  private Pick currentPick;
  private Player nightVictim;
  private final Random random;
  private boolean witchKillUsed;
  private boolean witchSaveUsed;

  public Game(String gameCode, Player admin) {
    this.gameCode = gameCode;
    this.admin = admin;
    this.random = new Random();
    this.phase = Phase.LOBBY;
    this.round = 1;
    this.players = new ArrayList<>();
    this.players.add(admin);
    this.deck = new ArrayList<>();
    this.nightQueue = new ArrayList<>();
    this.winners = new ArrayList<>();
    this.losers = new ArrayList<>();
    this.witchKillUsed = false;
    this.witchSaveUsed = false;
  }

  public String getGameCode() {
    return gameCode;
  }

  public Player getAdmin() {
    return admin;
  }

  public Random getRandom() {
    return random;
  }

  public Phase getPhase() {
    return phase;
  }

  public int getRound() {
    return round;
  }

  public List<Player> getPlayers() {
    return List.copyOf(players);
  }

  public List<Role> getDeck() {
    return List.copyOf(deck);
  }

  public List<Role> getNightQueue() {
    return List.copyOf(nightQueue);
  }

  public List<Player> getWinners() {
    return List.copyOf(winners);
  }

  public List<Player> getLosers() {
    return List.copyOf(losers);
  }

  public Player getNightVictim() {
    return nightVictim;
  }

  public boolean isWitchKillUsed() {
    return witchKillUsed;
  }

  public boolean isWitchSaveUsed() {
    return witchSaveUsed;
  }

  public Vote getCurrentVote() {
    return currentVote;
  }

  public Pick getCurrentPick() {
    return currentPick;
  }

  public void setCurrentVote(Vote currentVote) {
    this.currentVote = currentVote;
  }

  public void setCurrentPick(Pick currentPick) {
    this.currentPick = currentPick;
  }

  public void setNightVictim(Player nightVictim) {
    this.nightVictim = nightVictim;
  }

  public void useWitchKill() {
    this.witchKillUsed = true;
  }

  public void useWitchSave() {
    this.witchSaveUsed = true;
  }

  private List<Role> orderNight() {
    return players.stream()
            .filter(Player::getAlive)
            .map(Player::getGameRole)
            .filter(r -> r.getNightOrder() != 0)
            .distinct()
            .sorted(Comparator.comparingInt(Role::getNightOrder))
            .toList();
  }

  public List<Player> alivePlayers() {
    if (players.isEmpty()) throw new IllegalStateException("Game has no players");
    return players.stream().filter(Player::getAlive).toList();
  }

  public void submitVote(Player voter, Player target) {
    if (phase != Phase.VOTING) throw new IllegalStateException("Not voting.");
    if (currentVote == null) throw new IllegalStateException("No vote right now.");

    Player victim = currentVote.submit(voter, target);
    if (victim == null) return;

    currentVote = null;

    victim.setAlive(false);

    afterDayKill(victim);
  }

  private void afterDayKill(Player victim) {
    Side winSide = checkEnded();
    if (victim.getGameRole().getSide() == Side.DEPRESSED) {
      endGame(Side.DEPRESSED);
    } else if (winSide != null) {
      endGame(winSide);
    } else {
      round++;
      startNight();
    }
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

  private void dealRoles() {
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

    if (bad == 0 || good == 0 || good - bad < 3)
      throw new IllegalStateException("Deck rules not followed.");


    List<Role> roles = new ArrayList<>(deck);
    Collections.shuffle(roles, random);
    for (int i = 0; i  < roles.size(); i++) {
      players.get(i).setGameRole(roles.get(i));
    }
  }

  public Role submitNightAction(Player actor, Player target, WitchPotion witchPotion) {
    if (phase != Phase.NIGHT) throw new IllegalStateException("Not night.");

    return nightQueue.get(nightStep - 1)
        .getNightAction().resolve(this, actor, target, witchPotion);
  }

  public void addPlayer(Player player) {
    if (phase == Phase.LOBBY) {
      if (!players.contains(player))
        players.add(player);
      else throw new IllegalArgumentException("Player already in the game.");
    } else throw new IllegalStateException("Not in lobby.");
  }

  public void removePlayer(Player player1, Player player2) {
    if (player1 != admin)
      throw new IllegalArgumentException("Only admin can remove players.");

    if (player2.equals(admin))
      throw new IllegalArgumentException("Admin cannot remove themself from game.");

    if (phase == Phase.LOBBY) {
      players.remove(player2);
    } else throw new IllegalStateException("Not in Lobby");
  }

  public void addRole(Player player, Role role) {
    if (player != admin) throw new IllegalArgumentException("Only admin can add roles.");

    if (phase == Phase.LOBBY) {
      deck.add(role);
    } else throw new IllegalStateException("Not in Lobby");
  }

  public void removeRole(Player player, Role role) {
    if (player != admin) throw new IllegalArgumentException("Only admin can remove roles.");

    if (phase == Phase.LOBBY) {
      deck.remove(role);
    } else throw new IllegalStateException("Not in Lobby");
  }

  public void startGame() {
    if (phase != Phase.LOBBY) throw  new IllegalStateException("Game has already started.");
    dealRoles();
    startNight();
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

  private void startNight() {
    phase = Phase.NIGHT;
    nightQueue = orderNight();
    nightStep = 0;
    nightVictim = null;
    runNight();
  }

  public void runNight() {
    if (phase == Phase.ENDED) return;
    if (nightStep >= nightQueue.size()) {
      Side winSide = checkEnded();
      if (winSide != null) {
        endGame(winSide);
        return;
      }
      phase = Phase.VOTING;
      runVote();
      return;
    }
    Role nightRole = nightQueue.get(nightStep++);
    nightRole.getNightAction().start(this);
  }

  private void runVote() {
    currentVote = new Vote(alivePlayers(), alivePlayers(), random);
  }
}
