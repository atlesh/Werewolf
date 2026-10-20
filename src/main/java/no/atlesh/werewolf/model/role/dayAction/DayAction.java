package no.atlesh.werewolf.model.role.dayAction;

import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.game.Game;

public interface DayAction {
  void start(Game game, Player player);
  void resolve(Game game, Player actor, Player target);
}
