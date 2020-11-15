package Chess.Utils;

import Chess.Game;
import Chess.Player;

/**
 * @author jinaxCai
 */
public class DefaultGame implements Game {

//    private Player player1;
//    private Player player2;
    private int currentPlayer;// 1 or 2
    private int nextPos;// 0 - 8
    private int[][][] board; // 0 not put,1 p1 put,2 p2 put

    public DefaultGame() {
//        this.player1 = player1;
//        this.player2 = player2;
        board = new int[9][3][3];
    }

    public int[][][] getBoard() {
        return board;
    }

    public void showBoard() {
        //TODO
    }

    public String getBoardString() {
        //TODO
        return null;
    }

    public int getNextPos() {
        return nextPos;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean put(int board,int x, int y, int player) {
        if(board != nextPos){
            return false;
        }
        if((player + 1) != currentPlayer){
            return false;
        }
        if(!isEmpty(board,x,y)){
            return false;
        }
        this.board[board][x][y] = player;
        changePlayer();
        nextPos = y * 3 + x;
        return true;
    }

    private void changePlayer(){
        currentPlayer = 3 - currentPlayer;
    }

    public boolean isEmpty(int boardCount,int x, int y) {
        return board[boardCount][x][y] == 0;
    }

    public int checkWin() {
        for(int i = 0;i < 9;i++){
            int subBoardWin = checkSubBoardWin(i);
            if(subBoardWin != 0){
                return subBoardWin;
            }
        }
        return isFull() ? 3 : 0;
    }

    private boolean isFull(){
        for(int i = 0;i < 9;i++)
            for(int j = 0;j < 3;j++)
                for(int k = 0;k < 3;k++){
                    if(board[i][j][k] == 0)
                        return false;
                }
        return true;
    }

    private int checkSubBoardWin(int i){
        int[][] subBoard = board[i];
        //line
        if(subBoard[0][0] == 1 && subBoard[0][1] == 1 && subBoard[0][2] == 1){
            return 1;
        }
        if(subBoard[1][0] == 1 && subBoard[1][1] == 1 && subBoard[1][2] == 1){
            return 1;
        }
        if(subBoard[2][0] == 1 && subBoard[2][1] == 1 && subBoard[2][2] == 1){
            return 1;
        }
        //column
        if(subBoard[0][0] == 1 && subBoard[1][0] == 1 && subBoard[2][0] == 1){
            return 1;
        }
        if(subBoard[0][1] == 1 && subBoard[1][1] == 1 && subBoard[2][1] == 1){
            return 1;
        }
        if(subBoard[0][2] == 1 && subBoard[1][2] == 1 && subBoard[2][2] == 1){
            return 1;
        }
        //斜
        if(subBoard[0][0] == 1 && subBoard[1][1] == 1 && subBoard[2][2] == 1){
            return 1;
        }

        if(subBoard[2][0] == 1 && subBoard[1][1] == 1 && subBoard[0][2] == 1){
            return 1;
        }

        if(subBoard[0][0] == 2 && subBoard[0][1] == 2 && subBoard[0][2] == 2){
            return 2;
        }
        if(subBoard[1][0] == 2 && subBoard[1][1] == 2 && subBoard[1][2] == 2){
            return 2;
        }
        if(subBoard[2][0] == 2 && subBoard[2][1] == 2 && subBoard[2][2] == 2){
            return 2;
        }

        //column
        if(subBoard[0][0] == 2 && subBoard[1][0] == 2 && subBoard[2][0] == 2){
            return 2;
        }
        if(subBoard[0][1] == 2 && subBoard[1][1] == 2 && subBoard[2][1] == 2){
            return 2;
        }
        if(subBoard[0][2] == 2 && subBoard[1][2] == 2 && subBoard[2][2] == 2){
            return 2;
        }
        //斜
        if(subBoard[0][0] == 2 && subBoard[1][1] == 2 && subBoard[2][2] == 2){
            return 2;
        }

        if(subBoard[2][0] == 2 && subBoard[1][1] == 2 && subBoard[0][2] == 2){
            return 2;
        }

        return 0;

    }
}
