package no.atlesh.werewolf;

import io.javalin.Javalin;
import java.util.Map;
import java.util.UUID;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.service.GameRegistry;

public class Main {

  public static void main(String[] args) {
    GameRegistry registry = new GameRegistry();
    Javalin app = Javalin.create().start(8080);
    app.exception(IllegalArgumentException.class,
        (e, ctx) -> ctx.status(400).result(e.getMessage()));
    app.exception(IllegalStateException.class,
        (e, ctx) -> ctx.status(400).result(e.getMessage()));

    app.post("/games", ctx -> {
      String token = UUID.randomUUID().toString();
      String name = ctx.formParam("name");
      if (name == null) throw new IllegalArgumentException("No name given");
      Player admin = new Player(name, token);
      Game game = registry.createGame(admin);
      ctx.json(Map.of("code", game.getGameCode(), "token", token));
    });

    app.post("/games/{code}/join", ctx -> {
      Game game = registry.findGame(ctx.pathParam("code"));
      String token = UUID.randomUUID().toString();
      String name = ctx.formParam("name");
      if (name == null) throw new IllegalArgumentException("No name given");
      game.addPlayer(new Player(name, token));
      ctx.json(Map.of("token", token));
    });

    app.post("/games/{code}/start", ctx -> {
      Game game = registry.findGame(ctx.pathParam("code"));
      Player player = game.findPlayer(ctx.header("token"));
      game.startGame(player);
    });
  }
}
