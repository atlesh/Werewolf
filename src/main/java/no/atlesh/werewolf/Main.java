package no.atlesh.werewolf;

import io.javalin.Javalin;
import no.atlesh.werewolf.controller.GameController;
import no.atlesh.werewolf.service.GameRegistry;

public class Main {

  public static void main(String[] args) {
    GameRegistry registry = new GameRegistry();
    GameController controller = new GameController(registry);
    Javalin app = Javalin.create().start(8080);

    app.exception(IllegalArgumentException.class,
        (e, ctx) -> ctx.status(400).result(e.getMessage()));
    app.exception(IllegalStateException.class,
        (e, ctx) -> ctx.status(400).result(e.getMessage()));

    controller.registerRoutes(app);
  }
}
