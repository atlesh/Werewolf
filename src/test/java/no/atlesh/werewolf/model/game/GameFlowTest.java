package no.atlesh.werewolf.model.game;

import static no.atlesh.werewolf.model.game.GameTestHelper.allWithRole;
import static no.atlesh.werewolf.model.game.GameTestHelper.everyoneVotes;
import static no.atlesh.werewolf.model.game.GameTestHelper.startedGame;
import static no.atlesh.werewolf.model.game.GameTestHelper.withRole;
import static no.atlesh.werewolf.model.role.Role.SEER;
import static no.atlesh.werewolf.model.role.Role.TANNER;
import static no.atlesh.werewolf.model.role.Role.VILLAGER;
import static no.atlesh.werewolf.model.role.Role.WEREWOLF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Side;
import org.junit.jupiter.api.Test;

class GameFlowTest {

  @Test
  void werewolfKillThenSeerThenDayVote() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player seer = withRole(game, SEER);
    Player villager = withRole(game, VILLAGER);

    game.submitNightAction(wolf, villager, null);

    assertFalse(villager.getAlive());
    assertEquals(villager, game.getNightVictim());
    assertEquals(Phase.NIGHT, game.getPhase(), "seer still has to act");

    game.submitNightAction(seer, wolf, null);

    assertEquals(Phase.VOTING, game.getPhase());
    assertEquals(4, game.alivePlayers().size());
  }

  @Test
  void seerLearnsTheRealRole() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player seer = withRole(game, SEER);
    game.submitNightAction(wolf, withRole(game, VILLAGER), null);

    assertEquals(WEREWOLF, game.submitNightAction(seer, wolf, null));
  }

  @Test
  void werewolfCannotKillAnotherWerewolfOrActForOthers() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player villager = withRole(game, VILLAGER);

    assertThrows(IllegalArgumentException.class, () -> game.submitNightAction(wolf, wolf, null));
    assertThrows(IllegalArgumentException.class,
        () -> game.submitNightAction(villager, villager, null));
  }

  @Test
  void votingIsOnlyAllowedDuringTheDay() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player villager = withRole(game, VILLAGER);

    assertThrows(IllegalStateException.class, () -> game.submitVote(villager, wolf));

    game.submitNightAction(wolf, villager, null);
    game.submitNightAction(withRole(game, SEER), wolf, null);

    assertThrows(IllegalStateException.class,
        () -> game.submitNightAction(wolf, withRole(game, SEER), null));
  }

  @Test
  void votingOutTheLastWerewolfMakesTheVillageWin() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    game.submitNightAction(wolf, withRole(game, VILLAGER), null);
    game.submitNightAction(withRole(game, SEER), wolf, null);

    everyoneVotes(game, wolf);

    assertEquals(Phase.ENDED, game.getPhase());
    assertFalse(wolf.getAlive());
    assertEquals(List.of(wolf), game.getLosers());
    assertTrue(game.getWinners().stream().allMatch(p -> p.getGameRole().getSide() == Side.GOOD));
  }

  @Test
  void survivingDayStartsNextNight() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    List<Player> villagers = allWithRole(game, VILLAGER);
    game.submitNightAction(wolf, villagers.get(0), null);
    game.submitNightAction(withRole(game, SEER), wolf, null);

    everyoneVotes(game, villagers.get(1));

    assertEquals(Phase.NIGHT, game.getPhase());
    assertEquals(2, game.getRound());
    assertNull(game.getNightVictim(), "night victim is reset each night");
  }

  @Test
  void werewolvesWinWhenTheyEqualTheVillage() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player seer = withRole(game, SEER);
    List<Player> villagers = allWithRole(game, VILLAGER);

    game.submitNightAction(wolf, villagers.get(0), null);
    game.submitNightAction(seer, wolf, null);
    everyoneVotes(game, villagers.get(1));

    game.submitNightAction(wolf, villagers.get(2), null);

    assertEquals(Phase.ENDED, game.getPhase(), "seer has no one left to check, so night ends");
    assertEquals(List.of(wolf), game.getWinners());
  }

  @Test
  void seerCannotCheckTheSamePlayerTwice() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player seer = withRole(game, SEER);
    List<Player> villagers = allWithRole(game, VILLAGER);

    game.submitNightAction(wolf, villagers.get(0), null);
    game.submitNightAction(seer, wolf, null);
    everyoneVotes(game, villagers.get(1));
    game.submitNightAction(wolf, villagers.get(2), null);

    assertFalse(game.getCurrentPick().pool().contains(wolf));
    assertThrows(IllegalArgumentException.class, () -> game.submitNightAction(seer, wolf, null));
  }

  @Test
  void deadSeerIsSkipped() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);

    game.submitNightAction(withRole(game, WEREWOLF), withRole(game, SEER), null);

    assertEquals(Phase.VOTING, game.getPhase());
  }

  @Test
  void votingOutTheTannerMakesOnlyTheTannerWin() {
    Game game = startedGame(WEREWOLF, TANNER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player tanner = withRole(game, TANNER);
    game.submitNightAction(wolf, withRole(game, VILLAGER), null);
    game.submitNightAction(withRole(game, SEER), wolf, null);

    everyoneVotes(game, tanner);

    assertEquals(Phase.ENDED, game.getPhase());
    assertEquals(List.of(tanner), game.getWinners());
    assertEquals(4, game.getLosers().size());
  }

  @Test
  void deadPlayersCannotVote() {
    Game game = startedGame(WEREWOLF, VILLAGER, VILLAGER, VILLAGER, SEER);
    Player wolf = withRole(game, WEREWOLF);
    Player victim = withRole(game, VILLAGER);
    game.submitNightAction(wolf, victim, null);
    game.submitNightAction(withRole(game, SEER), wolf, null);

    assertThrows(IllegalArgumentException.class, () -> game.submitVote(victim, wolf));
    assertThrows(IllegalArgumentException.class, () -> game.submitVote(wolf, victim));
  }
}
