package no.atlesh.werewolf.model.nightAction;

import no.atlesh.werewolf.model.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.Role;

public interface NightAction {
  void start(Game game);
  Role resolve(Game game, Player actor, Player target);
}
