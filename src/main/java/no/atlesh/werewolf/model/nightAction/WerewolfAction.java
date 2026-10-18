package no.atlesh.werewolf.model.nightAction;

import java.util.List;
import no.atlesh.werewolf.model.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.Role;
import no.atlesh.werewolf.model.Side;
import no.atlesh.werewolf.model.Vote;

public class WerewolfAction implements NightAction {
  @Override
  public void start(Game game) {
    List<Player> werewolves = game.alivePlayers().stream()
        .filter(p -> p.getGameRole() == Role.WEREWOLF)
        .toList();
    List<Player> notWerewolves = game.alivePlayers().stream()
        .filter(p -> p.getGameRole() != Role.WEREWOLF)
        .toList();

    game.setCurrentVote(new Vote(werewolves, notWerewolves, game.getRandom()));
  }

  @Override
  public Role resolve(Game game, Player actor, Player target) {
    if (game.getCurrentVote() == null) throw new IllegalStateException("No vote right now.");

    Player victim = game.getCurrentVote().submit(actor, target);
    if (victim == null) return null;

    game.setCurrentVote(null);

    victim.setAlive(false);

    Side winSide = game.checkEnded();
    if (winSide != null) game.endGame(winSide);
    else game.runNight();

    return null;
  }
}
