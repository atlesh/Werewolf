package no.atlesh.werewolf.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;
import java.util.UUID;
import no.atlesh.werewolf.dto.GameState;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.role.Role;
import no.atlesh.werewolf.model.role.nightAction.witch.WitchPotion;
import no.atlesh.werewolf.service.GameRegistry;

public class GameController {
  private final GameRegistry registry;

  public GameController(GameRegistry registry) {
    this.registry = registry;
  }

  public void registerRoutes(Javalin app) {
    app.post("/games", ctx -> {
      String id = UUID.randomUUID().toString();
      String token = UUID.randomUUID().toString();
      Game game = registry.createGame(new Player(name(ctx), id, token));
      ctx.json(Map.of("code", game.getGameCode(), "id", id, "token", token));
    });

    app.post("/games/{code}/join", ctx -> {
      String id = UUID.randomUUID().toString();
      String token = UUID.randomUUID().toString();
      game(ctx).addPlayer(new Player(name(ctx), id, token));
      ctx.json(Map.of("id", id, "token", token));
    });

    app.post("/games/{code}/kick", ctx -> {
      Game game = game(ctx);
      game.removePlayer(me(ctx, game), target(ctx, game));
    });

    app.post("/games/{code}/start", ctx -> {
      Game game = game(ctx);
      game.startGame(me(ctx, game));
    });

    app.post("/games/{code}/roles/add", ctx -> {
      Game game = game(ctx);
      game.addRole(me(ctx, game), role(ctx));
    });

    app.post("/games/{code}/roles/remove", ctx -> {
      Game game = game(ctx);
      game.removeRole(me(ctx, game), role(ctx));
    });

    app.post("/games/{code}/vote", ctx -> {
      Game game = game(ctx);
      game.submitVote(me(ctx, game), target(ctx, game));
    });

    app.post("/games/{code}/night-action", ctx -> {
      Game game = game(ctx);
      String id = ctx.formParam("id");
      Player target = id == null ? null : game.findPlayerByID(id);
      WitchPotion witchPotion = null;
      String potion = ctx.formParam("potion");
      if (potion != null) {
        witchPotion = WitchPotion.valueOf(potion.toUpperCase());
      }
      game.submitNightAction(
          me(ctx, game), target, witchPotion);
    });

    app.post("/games/{code}/day-action", ctx -> {
      Game game = game(ctx);
      game.submitDayAction(me(ctx, game), target(ctx, game));
    });

    app.get("/games/{code}/state", ctx -> {
      Game game = game(ctx);
      synchronized (game) {
        ctx.json(GameState.of(game, me(ctx, game)));
      }
    });
  }

  private Game game(Context ctx) {
    return registry.findGame(ctx.pathParam("code"));
  }

  private Player me(Context ctx, Game game) {
    return game.findPlayer(ctx.header("token"));
  }

  private Player target(Context ctx, Game game) {
    return game.findPlayerByID(ctx.formParam("id"));
  }

  private String name(Context ctx) {
    String name = ctx.formParam("name");
    if (name == null) throw new IllegalArgumentException("No name given");
    if (name.isBlank()) throw new IllegalArgumentException("Name can't be blank");
    return name;
  }

  private Role role(Context ctx) {
    String role = ctx.formParam("role");
    if (role == null) throw new IllegalArgumentException("No role given");
    if (role.isBlank()) throw new IllegalArgumentException("Role can't be blank");
    return Role.valueOf(role.toUpperCase());
  }
}
