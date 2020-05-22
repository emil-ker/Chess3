import GameBoard.*;
import ViewControl.ViewControl;
import GameBoard.BoardGame;

public class Main {

    public static void main(String[] args) {
        BoardGame chess = new ChessBoard();
        new ViewControl(chess, 8);
    }
}
