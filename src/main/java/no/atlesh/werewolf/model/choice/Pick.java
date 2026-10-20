package no.atlesh.werewolf.model.choice;

import java.util.List;
import no.atlesh.werewolf.model.Player;

public record Pick(Player picker, List<Player> pool) {
  public Player submit(Player player, Player target) {
    if (!player.equals(picker)) {
      throw new IllegalArgumentException("You can't pick.");
    }
    if (!pool.contains(target)) {
      throw new IllegalArgumentException("Target is not in the pool.");
    }
    return target;
  }
}
