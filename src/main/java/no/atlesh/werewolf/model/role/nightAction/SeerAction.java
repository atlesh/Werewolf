package no.atlesh.werewolf.model.role.nightAction;

import java.util.List;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.choice.Pick;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Role;
import no.atlesh.werewolf.model.role.nightAction.witch.WitchPotion;

public class SeerAction implements NightAction {
  @Override
  public void start(Game game) {
    Player seer = null;
    for (Player player : game.alivePlayers()) {
      if (player.getGameRole() == Role.SEER) {
        seer = player;
      }
    }

    if (seer == null) {
      game.runNight();
      return;
    }

    List<Player> candidates = game.alivePlayers().stream()
        .filter(p -> p.getGameRole() != Role.SEER)
        .filter(player -> !(game.getSeen().contains(player)))
        .toList();

    if (candidates.isEmpty()) {
      game.runNight();
      return;
    }

    game.setCurrentPick(new Pick(seer, candidates));
  }

  @Override
  public Role resolve(Game game, Player actor, Player target,  WitchPotion potion) {
    if  (game.getCurrentPick() == null) throw new IllegalStateException("No pick right now.");

    Player chosen = game.getCurrentPick().submit(actor, target);

    game.addSeen(chosen);

    game.setCurrentPick(null);

    game.runNight();

    return chosen.getGameRole();
  }
}