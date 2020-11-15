package Server.DataPackage;

import Server.Utils.ResponseCode;
import Server.Utils.StatusCode;

/**
 * @author jinaxCai
 */
public class Packages {
    public static final char SPLITTER = ' ';

    public static DataPackage ConnectionPackage(){
        return new DataPackage(ResponseCode.CONNECTION, StatusCode.SUCCESS,"");
    }

    public static DataPackage GameStartPackage(int playerCount){
        return new DataPackage(ResponseCode.GAME_START, StatusCode.SUCCESS,playerCount + "");
    }

    public static DataPackage SuccessPutPackage(int playerCount,int[][][] board,int nextPos,int winInfo){
        StringBuilder data = new StringBuilder();
        data.append(playerCount);
        for(int i = 0;i < 9;i++)
            for(int j = 0;j < 3;j++)
                for(int k = 0;k < 3;k++){
                    if(board[i][j][k] == 0)
                        data.append(board[i][j][k]);
                }
        data.append(nextPos);
        data.append(winInfo);
        return new DataPackage(ResponseCode.PUT, StatusCode.SUCCESS,data.toString());
    }

    public static DataPackage FailPutPackage(){
        return new DataPackage(ResponseCode.PUT, StatusCode.FAIL,"");
    }

    public static DataPackage TimoutPackage(int playerCount){
        return new DataPackage(ResponseCode.TIMEOUT, StatusCode.FAIL,playerCount + "");
    }
}
