package no.atlesh.werewolf.model.choice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import no.atlesh.werewolf.model.Player;
import org.junit.jupiter.api.Test;

class PickTest {
  private final Player picker = new Player("Picker", "p");
  private final Player target = new Player("Target", "t");
  private final Player other = new Player("Other", "o");

  @Test
  void returnsTheChosenTarget() {
    Pick pick = new Pick(picker, List.of(target));

    assertEquals(target, pick.submit(picker, target));
  }

  @Test
  void onlyThePickerCanPick() {
    Pick pick = new Pick(picker, List.of(target));

    assertThrows(IllegalArgumentException.class, () -> pick.submit(other, target));
  }

  @Test
  void targetMustBeInThePool() {
    Pick pick = new Pick(picker, List.of(target));

    assertThrows(IllegalArgumentException.class, () -> pick.submit(picker, other));
  }
}
