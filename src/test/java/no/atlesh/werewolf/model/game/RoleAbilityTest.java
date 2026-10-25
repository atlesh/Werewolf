package no.atlesh.werewolf.model.game;

import static no.atlesh.werewolf.model.game.GameTestHelper.allWithRole;
import static no.atlesh.werewolf.model.game.GameTestHelper.everyoneVotes;
import static no.atlesh.werewolf.model.game.GameTestHelper.startedGame;
import static no.atlesh.werewolf.model.game.GameTestHelper.withRole;
import static no.atlesh.werewolf.model.role.Role.HUNTER;
import static no.atlesh.werewolf.model.role.Role.SEER;
import static no.atlesh.werewolf.model.role.Role.VILLAGER;
import static no.atlesh.werewolf.model.role.Role.WEREWOLF;
import static no.atlesh.werewolf.model.role.Role.WITCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.nightAction.witch.WitchPotion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RoleAbilityTest {

  @Nested
  class Witch {
    private Game game;
    private Player wolf;
    private Player witch;
    private List<Player> villagers;

    /** Plays night 1 and day 1, then lets the wolf kill villager 2 on night 2. Witch's turn is open. */
    private void reachWitchTurnOnNightTwo() {
      game = startedGame(WEREWOLF, WITCH, VILLAGER, VILLAGER, VILLAGER);
      wolf = withRole(game, WEREWOLF);
      witch = withRole(game, WITCH);
      villagers = allWithRole(game, VILLAGER);

      game.submitNightAction(wolf, villagers.get(0), null);
      everyoneVotes(game, villagers.get(1));
      game.submitNightAction(wolf, villagers.get(2), null);
    }

    @Test
    void witchDoesNotActOnFirstNight() {
      Game game = startedGame(WEREWOLF, WITCH, VILLAGER, VILLAGER, VILLAGER);

      game.submitNightAction(withRole(game, WEREWOLF), withRole(game, VILLAGER), null);

      assertEquals(Phase.VOTING, game.getPhase());
    }

    @Test
    void witchGetsATurnOnSecondNight() {
      reachWitchTurnOnNightTwo();

      assertEquals(Phase.NIGHT, game.getPhase());
      assertNotNull(game.getCurrentPick());
      assertEquals(witch, game.getCurrentPick().picker());
    }

    @Test
    void savingTheVictimBringsThemBack() {
      reachWitchTurnOnNightTwo();
      Player victim = villagers.get(2);

      game.submitNightAction(witch, victim, WitchPotion.SAVE);

      assertTrue(victim.getAlive());
      assertTrue(game.isWitchSaveUsed());
      assertEquals(Phase.VOTING, game.getPhase());
    }

    @Test
    void savingSomeoneNotAttackedWastesThePotion() {
      reachWitchTurnOnNightTwo();

      game.submitNightAction(witch, wolf, WitchPotion.SAVE);

      assertTrue(game.isWitchSaveUsed());
      assertFalse(villagers.get(2).getAlive());
    }

    @Test
    void killingTheLastWerewolfWinsForTheVillage() {
      reachWitchTurnOnNightTwo();

      game.submitNightAction(witch, wolf, WitchPotion.KILL);

      assertFalse(wolf.getAlive());
      assertTrue(game.isWitchKillUsed());
      assertEquals(Phase.ENDED, game.getPhase());
      assertTrue(game.getWinners().contains(witch));
    }

    @Test
    void skippingLeavesTheVictimDead() {
      reachWitchTurnOnNightTwo();

      game.submitNightAction(witch, null, null);

      assertFalse(villagers.get(2).getAlive());
      assertFalse(game.isWitchSaveUsed());
      assertFalse(game.isWitchKillUsed());
      assertEquals(Phase.ENDED, game.getPhase(), "wolf and witch are 1 against 1");
      assertEquals(List.of(wolf), game.getWinners());
    }

    @Test
    void witchCannotTargetHerself() {
      reachWitchTurnOnNightTwo();

      assertThrows(IllegalArgumentException.class,
          () -> game.submitNightAction(witch, witch, WitchPotion.KILL));
    }

    @Test
    void onlyTheWitchCanUseThePotions() {
      reachWitchTurnOnNightTwo();

      assertThrows(IllegalArgumentException.class,
          () -> game.submitNightAction(wolf, villagers.get(2), WitchPotion.SAVE));
    }
  }

  @Nested
  class Hunter {
    private Game game;
    private Player wolf;
    private Player hunter;
    private Player villager;

    /** Plays night 1 (wolf kills a villager) and votes out the Hunter. Hunter's shot is open. */
    private void voteOutHunter() {
      game = startedGame(WEREWOLF, HUNTER, VILLAGER, VILLAGER, VILLAGER, SEER);
      wolf = withRole(game, WEREWOLF);
      hunter = withRole(game, HUNTER);
      List<Player> villagers = allWithRole(game, VILLAGER);
      villager = villagers.get(1);

      game.submitNightAction(wolf, villagers.get(0), null);
      game.submitNightAction(withRole(game, SEER), wolf, null);
      everyoneVotes(game, hunter);
    }

    @Test
    void gameWaitsForTheHuntersShot() {
      voteOutHunter();

      assertFalse(hunter.getAlive());
      assertEquals(Phase.VOTING, game.getPhase());
      assertEquals(hunter, game.getCurrentPick().picker());
    }

    @Test
    void shootingTheLastWerewolfWinsForTheVillage() {
      voteOutHunter();

      game.submitDayAction(hunter, wolf);

      assertFalse(wolf.getAlive());
      assertEquals(Phase.ENDED, game.getPhase());
      assertTrue(game.getWinners().contains(hunter));
    }

    @Test
    void shootingAVillagerStartsNextNight() {
      voteOutHunter();

      game.submitDayAction(hunter, villager);

      assertFalse(villager.getAlive());
      assertEquals(Phase.NIGHT, game.getPhase());
      assertEquals(2, game.getRound());
    }

    @Test
    void onlyTheHunterCanShoot() {
      voteOutHunter();

      assertThrows(IllegalArgumentException.class, () -> game.submitDayAction(villager, wolf));
    }

    @Test
    void hunterCannotShootDeadPlayers() {
      voteOutHunter();

      assertThrows(IllegalArgumentException.class, () -> game.submitDayAction(hunter, hunter));
    }

    @Test
    void hunterKilledAtNightDoesNotShoot() {
      Game game = startedGame(WEREWOLF, HUNTER, VILLAGER, VILLAGER, SEER);
      Player wolf = withRole(game, WEREWOLF);
      Player hunter = withRole(game, HUNTER);

      game.submitNightAction(wolf, hunter, null);
      game.submitNightAction(withRole(game, SEER), wolf, null);

      assertEquals(Phase.VOTING, game.getPhase());
      assertThrows(IllegalStateException.class, () -> game.submitDayAction(hunter, wolf));
    }
  }
}
