package no.atlesh.werewolf.model;

import java.util.List;
import java.util.Objects;
import javax.management.relation.Role;

public class Player {
  private final String  name;
  private Role gameRole;
  private boolean alive;
  private final String browserID;

  public Player(String name, String browserID) {
    this.name = name;
    this.browserID = browserID;
    this.alive = true;
    this.gameRole = null;
  }

  // Getters

  public String getName() {
    return name;
  }

  public String getBrowserID() {
    return browserID;
  }

  public Role getGameRole() {
    return gameRole;
  }

  public Boolean getAlive() {
    return alive;
  }

  // Setters

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public void setGameRole(Role gameRole) {
    this.gameRole = gameRole;
  }

  // Methods

  public Game createGame(List<Game> games, String gameCode) {
    if (gameCode == null) throw new IllegalArgumentException("gameCode cannot be null");

    for (Game game : games) {
      if (Objects.equals(game.getGameCode(), gameCode)) {
        throw new IllegalArgumentException("gameCode already exists");
      }
    }

    Game game = new Game("test", this);
    games.add(game);
    return game;
  }

  public void joinGame(List<Game> games, String gameCode) {
    for  (Game game : games) {
      if(game.getPhase() == Phase.LOBBY && game.getGameCode().equals(gameCode)) {
        game.addPlayer(this);
      }
    }
  }

  public void removePlayerGame(Player player, Game game) {
    if (game == null) {throw new IllegalArgumentException("Game cannot be null");}

    if (this != game.getAdmin()) {
      throw new IllegalArgumentException("Only admin can remove players.");}

    if (game.getPhase() == Phase.LOBBY) {
      if (game.getPlayers().contains(player)) {
        game.removePlayer(player);
      }
    }
  }

  public void addRoleGame(Role role,  Game game) {
    if (game == null) {throw new IllegalArgumentException("Game cannot be null");}

    if (this != game.getAdmin()) {
      throw new IllegalArgumentException("Only admin can add roles.");}

    if (game.getPhase() == Phase.LOBBY) {
      game.addRole(role);
    }
  }

  public void removeRoleGame(Role role, Game game) {
    if (game == null) {throw new IllegalArgumentException("Game cannot be null");}

    if (this != game.getAdmin()) {
      throw new IllegalArgumentException("Only admin can remove roles.");}

    if (game.getPhase() == Phase.LOBBY) {
      boolean removed = false;
      for (Role r : game.getDeck()) {
        if (r.equals(role) && !removed) {
          game.removeRole(r);
          removed = true;
        }
      }
    }
  }

  public Player vote(List<Player> players) {
    return players.getFirst(); // Until I know how to let the player pick from the list.
  }


  // Need a method to update +1 in timesPerRole.
}
