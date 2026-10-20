package no.atlesh.werewolf;

import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;
import no.atlesh.werewolf.model.game.Game;
import no.atlesh.werewolf.model.Player;
import no.atlesh.werewolf.model.role.Role;

public class Main {
  public static void main(String[] args) {
    Javalin app = Javalin.create().start(7070);
    app.get("/", ctx -> ctx.result("Werewolf"));
    List<Game> games = new ArrayList<>();

    Player admin = new Player("Atle", "888");

    Player player1 = new Player("Bob", "801");
    Player player2 = new Player("Jack", "802");
    Player player3 = new Player("Daniel", "803");
    Player player4 = new Player("Danielson", "804");

    Game game = admin.createGame(games, "test");

    player1.joinGame(games, "test");
    player2.joinGame(games, "test");
    player3.joinGame(games, "test");
    player4.joinGame(games, "test");

    game.addRole(admin, Role.WEREWOLF);
    game.addRole(admin, Role.TANNER);
    game.addRole(admin, Role.VILLAGER);
    game.addRole(admin, Role.VILLAGER);
    game.addRole(admin, Role.SEER);

    app.post("/games/{code}/start", ctx -> {
      game.startGame();
      ctx.status(200);
    });
  }
}
