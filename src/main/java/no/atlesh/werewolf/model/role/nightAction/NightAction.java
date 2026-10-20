package no.atlesh.werewolf.model.role.nightAction;

import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Role;
import no.atlesh.werewolf.model.role.nightAction.witch.WitchPotion;

public interface NightAction {
  void start(Game game);
  Role resolve(Game game, Player actor, Player target, WitchPotion potion);
}
