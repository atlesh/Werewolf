package no.atlesh.werewolf.model;

import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.role.Role;

public class Player {
  private final String  name;
  private Role gameRole;
  private boolean alive;
  private final String token;
  private final String id;

  public Player(String name, String id, String token) {
    this.name = name;
    this.id = id;
    this.token = token;
    this.alive = true;
    this.gameRole = null;
  }

  // Getters

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public Role getGameRole() {
    return gameRole;
  }

  public boolean getAlive() {
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

  public void removePlayerGame(Player player, Game game) {
    if (game == null) throw new IllegalArgumentException("Game cannot be null");

    game.removePlayer(this, player);
  }

  public void addRoleGame(Role role,  Game game) {
    if (game == null) throw new IllegalArgumentException("Game cannot be null");

    game.addRole(this, role);
  }

  public void removeRoleGame(Role role, Game game) {
    if (game == null) throw new IllegalArgumentException("Game cannot be null");

    game.removeRole(this, role);
  }
}
