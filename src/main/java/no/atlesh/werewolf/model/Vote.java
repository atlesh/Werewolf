package no.atlesh.werewolf.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Vote {
  private final List<Player> voters;
  private final List<Player> pool;
  private List<Player> candidates;
  private final List<Player> discussed = new ArrayList<>();
  private final Map<Player, Player> votes =  new HashMap<>();
  private final Random random;
  private int attempt = 1;

  public Vote(List<Player> voters, List<Player> pool,  Random random) {
    this.voters = voters;
    this.pool = pool;
    this.candidates = pool;
    this.random = random;
  }

  public Player submit(Player voter, Player target) {
    if (!voters.contains(voter)) throw new IllegalArgumentException("You can't vote.");
    if (!candidates.contains(target)) throw new IllegalArgumentException("Not a candidate.");

    votes.put(voter, target);
    if (votes.size() < voters.size()) return null;

    Map<Player, Integer> voteCount = new HashMap<>();
    for (Player t : votes.values()) {
      voteCount.merge(t, 1, Integer::sum);
    }
    int max = Collections.max(voteCount.values());

    List<Player> mostVoted = voteCount.entrySet().stream()
        .filter(e -> e.getValue() == max)
        .map(Map.Entry::getKey)
        .toList();

    if (mostVoted.size() == 1) return mostVoted.getFirst();

    discussed.addAll(mostVoted);
    candidates = (attempt == 1)
        ? mostVoted
        : pool.stream().filter(p -> !discussed.contains(p)).toList();

    if (attempt == 3 || candidates.isEmpty()) {
      return pool.get(random.nextInt(pool.size()));
    }

    attempt++;
    votes.clear();
    return null;
  }

  public List<Player> getCandidates() {
    return candidates;
  }
}
