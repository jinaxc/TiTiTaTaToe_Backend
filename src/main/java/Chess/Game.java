package Chess;

/**
 * @author jinaxCai
 */
public interface Game {
    void showBoard();
    String getBoardString();
    int getCurrentPlayer();
    int getNextPos();
    int[][][] getBoard();
    /**
     *
     * @param board the boardCount
     * @param x the x coordination of the board
     * @param y the y coordination of the board
     * @param player 0 refers to the first player, 1 refers to the second player
     * @return true if this operation succeeds
     */
    boolean put(int board,int x,int y,int player);
    boolean isEmpty(int boardCount,int x,int y);
    /**
     *
     * @return 0 game not end; 1 first player win, 2 second player win,3 drawGame
     */
    int checkWin();
}
