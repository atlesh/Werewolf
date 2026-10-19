package no.atlesh.werewolf.model.nightAction.witch;

import java.util.ArrayList;
import java.util.List;
import no.atlesh.werewolf.model.Game;
import no.atlesh.werewolf.model.Pick;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.Role;
import no.atlesh.werewolf.model.nightAction.NightAction;

public class WitchAction implements NightAction {

  @Override
  public void start(Game game) {
    Player witch = null;
    for (Player player : game.alivePlayers()) {
      if (player.getGameRole() == Role.WITCH) {
        witch = player;
      }
    }

    if (witch == null || game.getRound() == 1 || game.isWitchKillUsed() && game.isWitchSaveUsed()) {
      game.runNight();
      return;
    }

    List<Player> candidates = new ArrayList<>(game.alivePlayers().stream()
        .filter(p -> p.getGameRole() != Role.WITCH)
        .toList());
    if (game.getNightVictim() != null) candidates.add(game.getNightVictim());

    game.setCurrentPick(new Pick(witch, candidates));
  }


  @Override
  public Role resolve(Game game, Player actor, Player target, WitchPotion potion) {
    if (game.getCurrentPick() == null) throw new IllegalStateException("Not picking now.");

    if (potion == null) {
      game.setCurrentPick(null);
      game.runNight();
      return null;
    }

    if (potion == WitchPotion.KILL && game.isWitchKillUsed())
      throw new IllegalArgumentException("Potion already used.");
    if (potion == WitchPotion.SAVE && game.isWitchSaveUsed())
      throw new IllegalArgumentException("Potion already used.");

    Player chosen = game.getCurrentPick().submit(actor, target);
    if (potion == WitchPotion.KILL) {
      chosen.setAlive(false);
      game.useWitchKill();
    } else {
      chosen.setAlive(true);
      game.useWitchSave();
    }

    game.setCurrentPick(null);
    game.runNight();
    return null;
  }
}
