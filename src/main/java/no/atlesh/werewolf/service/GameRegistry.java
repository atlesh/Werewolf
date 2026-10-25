package no.atlesh.werewolf.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.game.Game;

public class GameRegistry {
  private final Map<String, Game> games = new ConcurrentHashMap<>();
  private final Random random = new Random();

  public Game createGame(Player admin) {
    String code;
    Game game;
    do {
      code = randomCode();
      game = new Game(code, admin);
    } while (games.putIfAbsent(code, game) != null);
    return game;
  }

  public Game findGame(String code) {
    Game game = games.get(code.toUpperCase());
    if (game == null) throw new IllegalArgumentException("No game with code: " + code);
    return game;
  }

  public void removeGame(String code) {
    if (games.remove(code.toUpperCase()) == null)
      throw new IllegalArgumentException("No game with code: " + code);
  }

  private String randomCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 5; i++) {
      sb.append((char)(random.nextInt(26) + 'A'));
    }
    return sb.toString();
  }
}
