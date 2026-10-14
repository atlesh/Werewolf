package no.atlesh.werewolf.model;

public class Player {
  private String  name;
  private Role gameRole;
  private Boolean alive;
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

  public Boolean getAlive() {
    return alive;
  }

  // Setters

  public void setName(String name) {
    this.name = name;
  }

  public void setBrowserID(String browserID) {
    this.browserID = browserID;
  }

  public void setAlive(Boolean alive) {
    this.alive = alive;
  }

  public void setGameRole(Role gameRole) {
    this.gameRole = gameRole;
  }


  // Trenger en metode for å oppdatere +1 i timesPerRole i players user.
}
