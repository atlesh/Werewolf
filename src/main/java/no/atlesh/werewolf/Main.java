package no.atlesh.werewolf;

import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;
import no.atlesh.werewolf.model.Game;
import no.atlesh.werewolf.model.Player;

public class Main {
  public static void main(String[] args) {
    Javalin app = Javalin.create().start(7070);
    app.get("/", ctx -> ctx.result("Werewolf"));
    List<Game> games = new ArrayList<>();

    Player admin = new Player("Atle", "888");
    Player player1 = new Player("Bob", "862");
    Game game = admin.createGame(games, "test");
    player1.joinGame(games, "test");

    app.post("/games/{code}/start", ctx -> {
      game.startGame();
      ctx.status(200);
    });
  }
}
