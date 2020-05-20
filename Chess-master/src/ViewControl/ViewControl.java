package ViewControl;

import ViewControl.BoardGame;
import ViewControl.BoardTile;
import GamePieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ViewControl extends JFrame implements ActionListener {
    /**
     Class that displays an underlying BoardGame and allows a player (players) to interact with it
     */

    private final int size = 8; // CHESS SPECIFIC
    private final BoardGame game; // the underlying game
    private final BoardTile[][] board;
    private final JLabel mess = new JLabel();
    private boolean moveStarted = false; // current player has selected a piece to move in chess
    private BoardTile tileToMoveFrom; // tile player has selected to move from
    private BoardTile tileToMoveTo; // ^... selected to move to

    public ViewControl(BoardGame gm) {
        // Setup User Interface
        super();
        Color[] tileColors;
        Color activeTilesColor;
        JPanel containsBoard;
        tileColors = gm.getTileColors();
        activeTilesColor = gm.getActiveTilesColor();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(720, 450);
        this.setLayout(new BorderLayout());
        mess.setSize(720, 20);
        mess.setText(gm.getMessage());
        containsBoard = new JPanel();
        containsBoard.setSize(720, 430);
        containsBoard.setLayout(new GridLayout(size, size));

        // Setup board
        board = new BoardTile[size][size];
        game = gm;

        // Setup playing field
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = new BoardTile(i, j);
                String tile = game.getStatus(i, j);
                try {
                    String color = tile.substring(0, 5); // MAYBE CHESS SPECIFIC
                    String piece = tile.substring(6);
                    if (piece.equals("pawn")) { // CHESS SPECIFIC
                        board[i][j].setPiece(new Pawn(color));
                    } else if (piece.equals("rook")) {
                        board[i][j].setPiece(new Rook(color));
                    } else if (piece.equals("knight")) {
                        board[i][j].setPiece(new Knight(color));
                    } else if (piece.equals("bishop")) {
                        board[i][j].setPiece(new Bishop(color));
                    } else if (piece.equals("queen")) {
                        board[i][j].setPiece(new Queen(color));
                    } else if (piece.equals("king")) {
                        board[i][j].setPiece(new King(color));
                    }
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

    @Override
    public void actionPerformed(ActionEvent e) {
        /**
         * Fires whenever a tile on the board is pressed.
         * */

        // Get info about button clicked
        BoardTile clickedTile = (BoardTile) e.getSource(); // Update buttons if button clicked is next to empty one, i.e. game.move(b.i, b.j) returns true???
        int[] position = clickedTile.getPosition();
        int clicked_i = position[0]; int clicked_j = position[1];

        String turn = game.getTurn();
        boolean moveOk = game.move(clicked_i, clicked_j); // this has to be done every time a user clicks on the board
                                                          // to update messagebox

        if(!moveStarted) { // player hasn't marked piece to move yet
            if (clickedTile.hasPiece() && clickedTile.getPiece().getCol().equals(turn)) {
                if(tileToMoveFrom != null && tileToMoveTo != null){ // special case for first move
                    tileToMoveFrom.resetToUsualColor(); // these resets the 2 tiles involved in previous move
                    tileToMoveTo.resetToUsualColor();  // --||--
                }
                clearBoard();
                moveStarted = true;
                tileToMoveFrom = clickedTile;
                tileToMoveFrom.setToActiveColor();
                boolean[][] validMoves = game.getValidMoves();
                colorValidMoves(validMoves);

            } else {
                System.out.println("PLEASE SELECT YOUR PIECE");
            }
        } else if (clickedTile.getPiece() != null && clickedTile.getPiece().getCol().equals(turn)){ // player presses another of his/her pieces
            clearBoard();
            if(clickedTile == tileToMoveFrom){ // click on a tile/piece that you just marked
                tileToMoveFrom.resetToUsualColor();
                moveStarted = false;
                tileToMoveFrom = null;
            }else { // this is a remark
                tileToMoveFrom.resetToUsualColor();
                tileToMoveFrom = clickedTile;
                tileToMoveFrom.setToActiveColor();
                moveStarted = true; // unnecessary but explicit
                boolean[][] validMoves = game.getValidMoves();
                colorValidMoves(validMoves);            }
        } else { // player wants to move a piece
            if(moveOk){
                clearBoard();
                tileToMoveTo = clickedTile;
                tileToMoveTo.setToActiveColor();
                tileToMoveTo.setPiece(tileToMoveFrom.getPiece());
                tileToMoveFrom.removePiece();
                moveStarted = false;
            }else{
                System.out.println("Fel drag");
            }
        }

        checkForUpdates();
        this.mess.setText(game.getMessage());
    }

    private void checkForUpdates(){
        /**
         * Used to check if any tile of the board needs to be updated. In chess this is for promotions of pawns.
         * */

        // get indices to update
        int[][] toUpdate = game.getTilesToUpdate();
        if (toUpdate[0] == null){
            return;
        }
        System.out.println(Arrays.toString(toUpdate));
        for(int i=0; i<toUpdate.length; i++){
            int i_index = toUpdate[i][0];
            int j_index = toUpdate[i][1];

            // for each index getPiece that we want to update to.
            String tile = game.getStatus(i_index, j_index);
            String color = tile.substring(0, 5);
            String piece = tile.substring(6);

            if (piece.equals("queen")) {
                board[i_index][j_index].setPiece(new Queen(color));
                //containsBoard.add(board[i][j]); // adds button/tile to JPanel so that...
            }
        }
    }

    private void clearBoard(){
        /**
         * Used to reset the colors that showed possible moves but were ultimately not involved in the users move
         */
        for(int a=0; a<8; a++){
            for(int b=0; b<8; b++){ //hårdkodat
                BoardTile currentTile = board[a][b];
                if (currentTile != tileToMoveFrom && currentTile!= tileToMoveTo){
                    currentTile.resetToUsualColor(); //remove colors from "non-move" tiles
                }
            }
        }
    }
    private void colorValidMoves(boolean[][] validMoves){
        /**
         * Highlights possible moves for a clicked piece/tile.
         * */

        for(int a=0; a<8; a++){
            for(int b=0; b<8; b++){//hårdkodat
                //System.out.println(Integer.toString(a) + Integer.toString(b));
                //System.out.println(validMoves[a][b]);
                if (validMoves[a][b]){
                    board[a][b].setToActiveColor();

                }
            }
        }
    }

}