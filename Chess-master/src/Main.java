import GameBoard.*;
import ViewControl.ViewControl;
import ViewControl.BoardGame;

public class Main {

    public static void main(String[] args) {
        BoardGame chess = new ChessBoard();
        new ViewControl(chess);
    }
}
