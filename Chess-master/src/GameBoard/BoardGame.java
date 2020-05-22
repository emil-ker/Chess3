package GameBoard;

import java.awt.*;

/**
 Interface representing a standard tilebased boardgame where X players take turns to move pieces on a board.
 */
public interface BoardGame {
    boolean move(int i, int j);
    boolean[][] getValidMoves();
    String getStatus(int i, int j);
    String getMessage();
    String getTurn();
    Color[] getTileColors();
    Color getActiveTilesColor();
    int[][] getTilesToUpdate();
    String getActivePiece();
}
