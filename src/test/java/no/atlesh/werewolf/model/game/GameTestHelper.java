package no.atlesh.werewolf.model.game;

import java.util.List;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Role;

final class GameTestHelper {
  private GameTestHelper() {
  }

  /** Creates a game in the lobby with one player per role. The first player is the admin. */
  static Game newGame(Role... roles) {
    Player admin = new Player("P0", "id0");
    Game game = new Game("TEST", admin);
    for (int i = 1; i < roles.length; i++) {
      game.addPlayer(new Player("P" + i, "id" + i));
    }
    for (Role role : roles) {
      game.addRole(admin, role);
    }
    return game;
  }

  static Game startedGame(Role... roles) {
    Game game = newGame(roles);
    game.startGame();
    return game;
  }

  static Player withRole(Game game, Role role) {
    return allWithRole(game, role).getFirst();
  }

  static List<Player> allWithRole(Game game, Role role) {
    return game.getPlayers().stream()
        .filter(p -> p.getGameRole() == role)
        .toList();
  }

  /** Every living player votes for the same target in the day vote. */
  static void everyoneVotes(Game game, Player target) {
    for (Player voter : game.alivePlayers()) {
      game.submitVote(voter, target);
    }
  }
}
