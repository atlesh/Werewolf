package no.atlesh.werewolf.model;

public enum Role {
  VILLAGER("Villager", 0, Side.GOOD),
  TANNER("Tanner", 0, Side.DEPRESSED),
  WEREWOLF("Werewolf", 1, Side.BAD),
  SEER("Seer", 2, Side.GOOD);

  private final String name;
  private final int nightOrder;
  private final Side side;

  Role(String name, int nightOrder, Side side) {
    this.name = name;
    this.nightOrder = nightOrder;
    this.side = side;
  }

  public String getName() {
    return name;
  }

  public int getNightOrder() {
    return nightOrder;
  }

  public Side getSide() {
    return side;
  }
}
