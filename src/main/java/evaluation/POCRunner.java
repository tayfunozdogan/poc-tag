package evaluation;

import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import games.GameType;
import games.uno.UnoGameParameters;
import players.mcts.MCTSPlayer;
import players.simple.RandomPlayer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * POC runner: 500 games of UNO with modified rules (Extra Turn exploit).
 * MCTS (player 0) vs Random (player 1). Logs Skip usage by MCTS and win rate.
 */
public class POCRunner {

    private static final int N_GAMES = 500;
    private static final String CSV_PATH = "poc_results.csv";

    public static void main(String[] args) {
        UnoGameParameters params = new UnoGameParameters();
        Game game = GameType.Uno.createGameInstance(2, 0L, params);

        List<AbstractPlayer> players = Arrays.asList(
                new MCTSPlayer().copy(),
                new RandomPlayer().copy()
        );

        POCSkipCountListener skipListener = new POCSkipCountListener();
        game.addListener(skipListener);

        int mctsWins = 0;
        int totalSkips = 0;
        int maxSkipsInGame = 0;

        PrintWriter csv = null;
        try {
            csv = new PrintWriter(new FileWriter(CSV_PATH));
            csv.println("game,mctsWon,skipCount");
        } catch (IOException e) {
            System.err.println("Could not open " + CSV_PATH + ", logging to console only.");
        }

        System.out.println("POC: " + N_GAMES + " games, MCTS vs Random (UNO with Extra Turn exploit, nSkipCards=" + params.nSkipCards + ")");
        System.out.println("game,mctsWon,skipCount");

        for (int i = 0; i < N_GAMES; i++) {
            long seed = ThreadLocalRandom.current().nextLong();
            skipListener.reset();
            game.reset(players, seed);
            game.run();

            CoreConstants.GameResult[] results = game.getGameState().getPlayerResults();
            boolean mctsWon = results[0] == CoreConstants.GameResult.WIN_GAME;
            int skipCount = skipListener.getSkipCount();

            if (mctsWon) mctsWins++;
            totalSkips += skipCount;
            if (skipCount > maxSkipsInGame) maxSkipsInGame = skipCount;

            String line = (i + 1) + "," + mctsWon + "," + skipCount;
            if (i < 10 || (i + 1) % 100 == 0 || i == N_GAMES - 1) {
                System.out.println(line);
            }
            if (csv != null) {
                csv.println(line);
            }
        }

        if (csv != null) {
            csv.close();
        }

        double winRate = 100.0 * mctsWins / N_GAMES;
        double avgSkipsPerGame = (double) totalSkips / N_GAMES;
        System.out.println();
        System.out.println("--- Summary ---");
        System.out.println("MCTS win rate: " + String.format("%.1f", winRate) + "% (" + mctsWins + "/" + N_GAMES + ")");
        System.out.println("Skip cards played by MCTS per game (avg): " + String.format("%.2f", avgSkipsPerGame));
        System.out.println("Skip cards played by MCTS (total): " + totalSkips);
        System.out.println("Max Skip in a single game: " + maxSkipsInGame);
        System.out.println("Results written to: " + CSV_PATH);
    }
}
