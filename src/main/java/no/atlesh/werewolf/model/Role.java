package no.atlesh.werewolf.model;

import no.atlesh.werewolf.model.nightAction.NightAction;
import no.atlesh.werewolf.model.nightAction.SeerAction;
import no.atlesh.werewolf.model.nightAction.WerewolfAction;
import no.atlesh.werewolf.model.nightAction.WitchAction;

public enum Role {
  VILLAGER("Villager", 0, Side.GOOD, null),
  TANNER("Tanner", 0, Side.DEPRESSED, null),
  WEREWOLF("Werewolf", 1, Side.BAD, new WerewolfAction()),
  SEER("Seer", 2, Side.GOOD, new SeerAction()),
  WITCH("Witch", 3, Side.GOOD, new WitchAction());

  private final String name;
  private final int nightOrder;
  private final Side side;
  private final NightAction nightAction;

  Role(String name, int nightOrder, Side side, NightAction action) {
    this.name = name;
    this.nightOrder = nightOrder;
    this.side = side;
    this.nightAction = action;
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

  public NightAction getNightAction() {
    return nightAction;
  }
}
