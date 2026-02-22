package evaluation;

import core.Game;
import evaluation.listeners.IGameListener;
import evaluation.metrics.Event;
import games.uno.UnoGameState;
import games.uno.actions.PlayCard;
import games.uno.cards.UnoCard;

/**
 * Listener that counts how many times player 0 (MCTS in POC) plays a Skip card per game.
 * Reset before each game; read skipCount after each game.
 */
public class POCSkipCountListener implements IGameListener {

    private Game game;
    private int skipCount = 0;

    @Override
    public void onEvent(Event event) {
        if (event.type != Event.GameEvent.ACTION_TAKEN || event.state == null || event.action == null) {
            return;
        }
        if (!(event.state instanceof UnoGameState) || !(event.action instanceof PlayCard)) {
            return;
        }
        if (event.playerID != 0) {
            return;
        }
        UnoGameState ugs = (UnoGameState) event.state;
        if (ugs.getCurrentCard() != null && ugs.getCurrentCard().type == UnoCard.UnoCardType.Skip) {
            skipCount++;
        }
    }

    @Override
    public void report() {
        // No aggregate report needed for POC
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void reset() {
        skipCount = 0;
    }

    public int getSkipCount() {
        return skipCount;
    }
}
