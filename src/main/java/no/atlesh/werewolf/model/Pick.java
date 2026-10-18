package no.atlesh.werewolf.model;

import java.util.List;

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
