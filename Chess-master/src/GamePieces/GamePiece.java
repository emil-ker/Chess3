package GamePieces;

import javax.swing.*;

public class GamePiece {

    private String col;
    private final Icon pieceIcon;

    // every piece is a GamePiece, pieceIcon differs depending on the piece passed from the board
    public GamePiece(String gameStatus) {
        col = gameStatus.substring(0, 5);
        this.pieceIcon = new ImageIcon("src/resources/" + gameStatus + ".png");
    }

    // only for subclasses not to throw errors, subclasses might as well be deleted
    public GamePiece(String color, ImageIcon imageIcon) {
        System.out.println(color);
        pieceIcon = imageIcon;
    }

    public String getCol() { return col; }

    public Icon getIcon() { return pieceIcon; }

}
