package no.atlesh.werewolf.model.game;

import static no.atlesh.werewolf.model.game.GameTestHelper.newGame;
import static no.atlesh.werewolf.model.role.Role.SEER;
import static no.atlesh.werewolf.model.role.Role.VILLAGER;
import static no.atlesh.werewolf.model.role.Role.WEREWOLF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Role;
import org.junit.jupiter.api.Test;

class GameLobbyTest {

  @Test
  void newGameStartsInLobbyWithAdmin() {
    Player admin = new Player("Admin", "a");
    Game game = new Game("CODE", admin);

    assertEquals(Phase.LOBBY, game.getPhase());
    assertEquals(List.of(admin), game.getPlayers());
  }

  @Test
  void addingSamePlayerTwiceThrows() {
    Player admin = new Player("Admin", "a");
    Player bob = new Player("Bob", "b");
    Game game = new Game("CODE", admin);
    game.addPlayer(bob);

    assertThrows(IllegalArgumentException.class, () -> game.addPlayer(bob));
  }

  @Test
  void onlyAdminCanChangeDeck() {
    Player admin = new Player("Admin", "a");
    Player bob = new Player("Bob", "b");
    Game game = new Game("CODE", admin);
    game.addPlayer(bob);

    assertThrows(IllegalArgumentException.class, () -> game.addRole(bob, VILLAGER));
    assertThrows(IllegalArgumentException.class, () -> game.removeRole(bob, VILLAGER));
  }

  @Test
  void removeRoleRemovesOnlyOneCopy() {
    Game game = newGame(VILLAGER, VILLAGER);

    game.removeRole(game.getAdmin(), VILLAGER);

    assertEquals(List.of(VILLAGER), game.getDeck());
  }

  @Test
  void adminCannotBeRemoved() {
    Player admin = new Player("Admin", "a");
    Game game = new Game("CODE", admin);

    assertThrows(IllegalArgumentException.class, () -> game.removePlayer(admin, admin));
  }

  @Test
  void onlyAdminCanRemovePlayers() {
    Player admin = new Player("Admin", "a");
    Player bob = new Player("Bob", "b");
    Player eve = new Player("Eve", "e");
    Game game = new Game("CODE", admin);
    game.addPlayer(bob);
    game.addPlayer(eve);

    assertThrows(IllegalArgumentException.class, () -> game.removePlayer(bob, eve));

    game.removePlayer(admin, eve);
    assertEquals(List.of(admin, bob), game.getPlayers());
  }

  @Test
  void returnedListsCannotChangeTheGame() {
    Game game = newGame(VILLAGER);

    assertThrows(UnsupportedOperationException.class,
        () -> game.getPlayers().add(new Player("Cheater", "c")));
    assertThrows(UnsupportedOperationException.class, () -> game.getDeck().clear());
  }

  @Test
  void cannotStartWithFewerThanFivePlayers() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER);

    assertThrows(IllegalStateException.class, game::startGame);
    assertEquals(Phase.LOBBY, game.getPhase());
  }

  @Test
  void cannotStartWhenDeckSizeDiffersFromPlayerCount() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    game.addRole(game.getAdmin(), VILLAGER);

    assertThrows(IllegalStateException.class, game::startGame);
  }

  @Test
  void cannotStartWithoutWerewolf() {
    Game game = newGame(VILLAGER, VILLAGER, VILLAGER, VILLAGER, SEER);

    assertThrows(IllegalStateException.class, game::startGame);
  }

  @Test
  void cannotStartWithTooManyWerewolves() {
    Game game = newGame(WEREWOLF, WEREWOLF, VILLAGER, VILLAGER, SEER);

    assertThrows(IllegalStateException.class, game::startGame);
  }

  @Test
  void startDealsEveryRoleInTheDeckOnce() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    List<Role> deckBefore = game.getDeck();

    game.startGame();

    List<Role> dealt = new ArrayList<>();
    for (Player p : game.getPlayers()) {
      assertNotNull(p.getGameRole());
      dealt.add(p.getGameRole());
    }
    List<Role> expected = new ArrayList<>(deckBefore);
    expected.sort(null);
    dealt.sort(null);
    assertEquals(expected, dealt);
    assertEquals(deckBefore, game.getDeck());
  }

  @Test
  void startTwiceThrows() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    game.startGame();

    assertThrows(IllegalStateException.class, game::startGame);
  }

  @Test
  void lobbyIsClosedAfterStart() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    game.startGame();
    Player admin = game.getAdmin();
    Player other = game.getPlayers().get(1);

    assertThrows(IllegalStateException.class, () -> game.addPlayer(new Player("Late", "l")));
    assertThrows(IllegalStateException.class, () -> game.removePlayer(admin, other));
    assertThrows(IllegalStateException.class, () -> game.addRole(admin, VILLAGER));
    assertThrows(IllegalStateException.class, () -> game.removeRole(admin, VILLAGER));
  }

  @Test
  void startGoesToNight() {
    Game game = newGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);

    game.startGame();

    assertEquals(Phase.NIGHT, game.getPhase());
    assertEquals(1, game.getRound());
    assertTrue(game.getCurrentVote() != null, "werewolf vote should be open");
  }
}
