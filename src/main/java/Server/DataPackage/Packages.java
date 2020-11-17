package Server.DataPackage;

import Server.Utils.ResponseCode;
import Server.Utils.StatusCode;

/**
 * @author jinaxCai
 */
public class Packages {
    public static final char SPLITTER = ' ';

    public static DataPackage InvalidRequestPackage(){
        return new DataPackage(ResponseCode.INVALID_REQUEST, StatusCode.FAIL,"");
    }

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
                    data.append(board[i][j][k]);
                }
        data.append(nextPos);
        data.append(winInfo);
        return new DataPackage(ResponseCode.PUT, StatusCode.SUCCESS,data.toString());
    }

    public static DataPackage FailPutPackage(String info){
        return new DataPackage(ResponseCode.PUT, StatusCode.FAIL,info);
    }

    public static DataPackage TimoutPackage(int playerCount){
        return new DataPackage(ResponseCode.TIMEOUT, StatusCode.FAIL,playerCount + "");
    }

    public static DataPackage ReceiveInvitePackage(String username){
        return new DataPackage(ResponseCode.RECEIVE_INVITE, StatusCode.SUCCESS,username);
    }

    public static DataPackage AnswerInvitePackage(boolean success,String username){
        return new DataPackage(ResponseCode.ANSWER_INVITE, success?1 : 0,username);
    }

    public static DataPackage AllUsersPackage(String users){
        return new DataPackage(ResponseCode.ALL_USERS, StatusCode.SUCCESS,users);
    }

    public static DataPackage ConfirmAnswerInvitePackage(boolean success,boolean isPositiveAnswer,String username){
        return new DataPackage(ResponseCode.CONFIRM_ANSWER_INVITE, success ? StatusCode.SUCCESS : StatusCode.FAIL,(isPositiveAnswer ? 1 : 0) + username);
    }

    public static DataPackage ApplyUsernamePackage(boolean success,String data){
        return new DataPackage(ResponseCode.APPLY_USER_NAME, success ? 1 : 0,data);
    }

    public static DataPackage GetUsernamePackage(boolean success,String data){
        return new DataPackage(ResponseCode.GET_USER_NAME, success ? 1 : 0,data);
    }
}
