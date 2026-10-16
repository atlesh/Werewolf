package no.atlesh.werewolf.model;

import java.util.List;
import java.util.Objects;

public class Player {
  private String  name;
  private Role gameRole;
  private boolean alive;
  private String browserID;

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

  public boolean getAlive() {
    return alive;
  }

  // Setters

  public void setName(String name) {
    this.name = name;
  }

  public void setBrowserID(String browserID) {
    this.browserID = browserID;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public void setGameRole(Role gameRole) {
    this.gameRole = gameRole;
  }

  // Methods

  public Player vote(List<Player> players) {
    Player vote = players.getFirst();
    if (Objects.equals(vote.getName(), name)) {
      vote = players.get(1);
    }
    // Until I know how to let the player pick from the list.
    return vote;
  }


  // Needs a method to update +1 timesPerRole in players user.
}
