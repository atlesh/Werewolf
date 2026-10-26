package no.atlesh.werewolf.model.choice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import no.atlesh.werewolf.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VoteTest {
  private Player a;
  private Player b;
  private Player c;
  private Player d;
  private List<Player> all;

  @BeforeEach
  void setUp() {
    a = new Player("A", "a", "a");
    b = new Player("B", "b", "b");
    c = new Player("C", "c", "c");
    d = new Player("D", "d", "d");
    all = List.of(a, b, c, d);
  }

  @Test
  void waitsUntilEveryoneHasVoted() {
    Vote vote = new Vote(all, all, new Random(1));

    assertNull(vote.submit(a, b));
    assertNull(vote.submit(b, c));
    assertNull(vote.submit(c, b));
  }

  @Test
  void clearMajorityWins() {
    Vote vote = new Vote(all, all, new Random(1));
    vote.submit(a, b);
    vote.submit(b, c);
    vote.submit(c, b);

    assertEquals(b, vote.submit(d, b));
  }

  @Test
  void votingAgainChangesYourVote() {
    Vote vote = new Vote(List.of(a, b), all, new Random(1));
    vote.submit(a, c);
    vote.submit(a, d);

    assertEquals(d, vote.submit(b, d));
  }

  @Test
  void tieStartsRevoteBetweenTheTiedPlayers() {
    Vote vote = new Vote(all, all, new Random(1));
    vote.submit(a, b);
    vote.submit(b, a);
    vote.submit(c, b);

    assertNull(vote.submit(d, a));
    assertEquals(2, vote.getCandidates().size());
    assertTrue(vote.getCandidates().containsAll(List.of(a, b)));
    assertThrows(IllegalArgumentException.class, () -> vote.submit(a, c));
  }

  @Test
  void secondTieRevotesAmongTheOthers() {
    Vote vote = new Vote(all, all, new Random(1));
    tieBetweenAAndB(vote);
    tieBetweenAAndB(vote);

    assertEquals(2, vote.getCandidates().size());
    assertTrue(vote.getCandidates().containsAll(List.of(c, d)));
  }

  @Test
  void thirdTiePicksRandomlyFromEveryone() {
    Vote vote = new Vote(all, all, new Random(1));
    tieBetweenAAndB(vote);
    tieBetweenAAndB(vote);
    vote.submit(a, c);
    vote.submit(b, d);
    vote.submit(c, d);

    Player victim = vote.submit(d, c);

    assertTrue(all.contains(victim));
  }

  @Test
  void onlyVotersCanVote() {
    Vote vote = new Vote(List.of(a, b), all, new Random(1));

    assertThrows(IllegalArgumentException.class, () -> vote.submit(c, a));
  }

  @Test
  void targetMustBeACandidate() {
    Vote vote = new Vote(all, List.of(c, d), new Random(1));

    assertThrows(IllegalArgumentException.class, () -> vote.submit(a, b));
  }

  private void tieBetweenAAndB(Vote vote) {
    vote.submit(a, b);
    vote.submit(b, a);
    vote.submit(c, b);
    assertNull(vote.submit(d, a));
  }
}
