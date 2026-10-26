package no.atlesh.werewolf.dto;

import java.util.List;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.choice.Pick;
import no.atlesh.werewolf.model.choice.Vote;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.game.Phase;
import no.atlesh.werewolf.model.role.Role;

public record GameState(
    String code,
    Phase phase,
    int round,
    boolean admin,
    String myId,
    Role myRole,
    boolean alive,
    List<PlayerView> players,
    List<Role> deck,
    List<String> targets,
    String nightVictim,
    List<String> winners
) {
  public static GameState of(Game game, Player me) {
    boolean admin = me == game.getAdmin();

    List<PlayerView> players = game.getPlayers().stream()
        .map(p -> new PlayerView(
            p.getId(),
            p.getName(),
            p.getAlive(),
            canSeeRole(game, me, p) ? p.getGameRole() : null))
        .toList();

    return new GameState(
        game.getGameCode(),
        game.getPhase(),
        game.getRound(),
        admin,
        me.getId(),
        me.getGameRole(),
        me.getAlive(),
        players,
        game.getDeck(),
        targets(game, me),
        nightVictim(game, me),
        winners(game)
    );
  }

  private static boolean canSeeRole(Game game, Player me, Player other) {
    Role myRole = me.getGameRole();
    return me == other
        || myRole == Role.WEREWOLF && other.getGameRole() == Role.WEREWOLF
        || game.getPhase() == Phase.ENDED
        || myRole == Role.SEER && game.getSeen().contains(other);
  }

  private static List<String> targets(Game game, Player me) {
    Pick pick = game.getCurrentPick();
    if (pick != null && pick.picker() == me) {
      return pick.pool().stream().map(Player::getId).toList();
    }

    Vote vote = game.getCurrentVote();
    if (vote != null && vote.getVoters().contains(me)) {
      return vote.getCandidates().stream().map(Player::getId).toList();
    }

    return List.of();
  }

  private static String nightVictim(Game game, Player me) {
    Pick pick = game.getCurrentPick();
    Player victim = game.getNightVictim();
    if (pick != null
        && pick.picker() == me
        && me.getGameRole() == Role.WITCH
        && victim != null)
      return victim.getId();
    return null;
  }

  private static List<String> winners(Game game) {
    if (game.getPhase() == Phase.ENDED) {
      return game.getWinners().stream().map(Player::getId).toList();
    }

    return List.of();
  }
}
