package no.atlesh.werewolf.model.role;

import no.atlesh.werewolf.model.role.dayAction.DayAction;
import no.atlesh.werewolf.model.role.dayAction.HunterAction;
import no.atlesh.werewolf.model.role.nightAction.NightAction;
import no.atlesh.werewolf.model.role.nightAction.SeerAction;
import no.atlesh.werewolf.model.role.nightAction.WerewolfAction;
import no.atlesh.werewolf.model.role.nightAction.witch.WitchAction;

public enum Role {
  VILLAGER("Villager", 0, Side.GOOD, null, null),
  HUNTER("Hunter", 0, Side.GOOD, null, new HunterAction()),
  TANNER("Tanner", 0, Side.DEPRESSED, null, null),
  WEREWOLF("Werewolf", 1, Side.BAD, new WerewolfAction(), null),
  SEER("Seer", 2, Side.GOOD, new SeerAction(), null),
  WITCH("Witch", 3, Side.GOOD, new WitchAction(), null);

  private final String name;
  private final int nightOrder;
  private final Side side;
  private final NightAction nightAction;
  private final DayAction dayAction;

  Role(String name, int nightOrder, Side side, NightAction action, DayAction dayAction) {
    this.name = name;
    this.nightOrder = nightOrder;
    this.side = side;
    this.nightAction = action;
    this.dayAction = dayAction;
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

  public DayAction getDayAction() {
    return dayAction;
  }
}
