package ViewControl;

import GameBoard.BoardGame;
import GamePieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewControl extends JFrame implements ActionListener {
    /**
     Class that displays an underlying BoardGame and allows a player (players) to interact with it
     */

    private final int size; // CHESS SPECIFIC, easily fixed, won't be a problem at presentation
    private final BoardGame game; // the underlying game
    private final BoardTile[][] board;
    private final JLabel mess = new JLabel();
    private boolean moveStarted = false; // current player has selected a piece to move in chess
    private BoardTile tileToMoveFrom; // tile player has selected to move from
    private BoardTile tileToMoveTo; // ^... selected to move to

    public ViewControl(BoardGame gm, int size) {
        // Setup User Interface
        super();
        this.size = size;
        Color[] tileColors = gm.getTileColors();
        Color activeTilesColor = gm.getActiveTilesColor();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(720, 500);
        this.setLayout(new BorderLayout());
        mess.setSize(720, 100);
        mess.setText(gm.getMessage());
        mess.setHorizontalAlignment(JLabel.CENTER);
        mess.setVerticalAlignment(JLabel.CENTER);
        JPanel containsBoard = new JPanel();
        containsBoard.setSize(720, 400);
        containsBoard.setLayout(new GridLayout(size, size));

        // Setup board
        board = new BoardTile[size][size];
        game = gm;

        // Setup playing field
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                try {
                    board[i][j] = new BoardTile(i, j);
                    board[i][j].setPiece(new GamePiece(game.getStatus(i, j))); // every piece is a GamePiece
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    if (!(game.getStatus(i, j).equals("-"))) System.err.println("Something is wrong in the model");
                }
                board[i][j].addActionListener(this);
                board[i][j].setBorderPainted(false);
                board[i][j].setOpaque(true);
                board[i][j].setBackground((i + j) % 2 == 0 ? tileColors[0]: tileColors[1], activeTilesColor);
                containsBoard.add(board[i][j]); // adds button/tile to JPanel so that...
            }
        }

        // Add playing field to User Interface
        this.add(containsBoard, BorderLayout.CENTER); //...JPanel can be added to JFrame
        this.add(mess, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    /**
     * Fires whenever a tile on the board is pressed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Get info about button clicked
        BoardTile clickedTile = (BoardTile) e.getSource();
        int[] position = clickedTile.getPosition();
        int clicked_i = position[0]; int clicked_j = position[1];

        moveStarted = !game.getActivePiece().equals("");
        String turn = game.getTurn();
        boolean moveOk = game.move(clicked_i, clicked_j); // this has to be done every time a user clicks on the board to update messagebox

        if (!moveStarted) { // player hasn't marked piece to move yet
            if (clickedTile.hasPiece() && clickedTile.getPiece().getCol().equals(turn)) {
                tileToMoveFrom = clickedTile;
                boolean[][] validMoves = game.getValidMoves();
                tileToMoveFrom.setToActiveColor();
                colorValidMoves(validMoves);
            }
        } else if (clickedTile.getPiece() != null && clickedTile.getPiece().getCol().equals(turn)){ // player presses another of his/her pieces
            resetBoardColors();
            if (clickedTile == tileToMoveFrom) { // click on a tile/piece that you just marked, deselection
                tileToMoveFrom = null;
            } else { // this is a remark
                tileToMoveFrom = clickedTile;
                boolean[][] validMoves = game.getValidMoves();
                tileToMoveFrom.setToActiveColor();
                colorValidMoves(validMoves);
            }
        } else { // player wants to move a piece
            if (moveOk) {
                resetBoardColors();
                board[clicked_i][clicked_j].setPiece(tileToMoveFrom.getPiece());
                tileToMoveFrom.removePiece();
            }
        }

        checkForUpdates();
        this.mess.setText(game.getMessage());
    }

    /**
     * Used to check if any tile of the board needs to be updated. In chess this is for promotions of pawns.
     */
    private void checkForUpdates(){
        // get indices to update
        int[][] toUpdate = game.getTilesToUpdate();
        if (toUpdate[0] == null) {
            return;
        }
        for (int[] ints : toUpdate) {
            int i_index = ints[0];
            int j_index = ints[1];
            board[i_index][j_index].setPiece(new GamePiece(game.getStatus(i_index, j_index)));
        }
    }

    /**
     * Used to reset the colors that showed possible moves
     */
    private void resetBoardColors() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BoardTile currentTile = board[i][j];
                currentTile.resetToUsualColor(); // remove colors from "non-move" tiles
            }
        }
    }

    /**
     * Highlights possible moves for a clicked piece/tile.
     */
    private void colorValidMoves(boolean[][] validMoves) {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if (validMoves[i][j]){
                    board[i][j].setToActiveColor();
                }
            }
        }
    }
}