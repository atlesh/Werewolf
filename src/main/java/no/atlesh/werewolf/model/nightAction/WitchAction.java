package no.atlesh.werewolf.model.nightAction;

import no.atlesh.werewolf.model.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.Role;

public class WitchAction implements NightAction {
  @Override
  public void start(Game game) {
    game.runNight();
  }

  @Override
  public Role resolve(Game game, Player actor, Player target) {
    return null;
  }
}
