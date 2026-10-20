package no.atlesh.werewolf.model.role.dayAction;

import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.choice.Pick;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.role.Role;

public class HunterAction implements DayAction {
  @Override
  public void start(Game game, Player hunter) {
    game.setCurrentPick(new Pick(hunter, game.alivePlayers()));
  }

  @Override
  public void resolve(Game game, Player actor, Player target) {
    Player chosen = game.getCurrentPick().submit(actor, target);
    chosen.setAlive(false);
    game.setCurrentPick(null);
    game.finishDay();
  }
}
