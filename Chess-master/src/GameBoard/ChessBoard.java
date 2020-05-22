package GameBoard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ChessBoard implements BoardGame {
    /**
     This is the underlying ChessBoard with the logic of the game in it. As such it handles initialisation of the board
     and all the following moves, checking for chess, checking for promotion etc.
     */

    private final String[][] board = new String[8][8]; // represents chess board
    private Color[] tileColors = {new Color(240, 240,240), Color.lightGray}; // to display for ViewControl
    private Color activeTilesColor = Color.yellow;
    private final String[] colors = {"black", "white"};
    private String activeColor = colors[1];
    private String dormantColor = colors[0];
    private String msg = activeColor.replace(activeColor.charAt(0), Character.toUpperCase(activeColor.charAt(0))) + "'s turn, choose a piece to move";
    private String activePiece = "";
    private int[] activePosition;
    private final int maxIndex = 7;
    private boolean chessFlag = false; // used to flag that a chessmove has been made for the "switchTurn()" function
    private final ArrayList<String> whitePiecesTaken = new ArrayList<>();
    private final ArrayList<String> blackPiecesTaken = new ArrayList<>();
    private int[] promotionTile; // has values if we at the end of turn need to promote pawn to queen
    private boolean[][] moveOk = new boolean[8][8]; // represents valid moves for activePiece (aka marked piece)

    public ChessBoard() {
        init_middle();
        init_pawns();
        for (int i = 0; i < 4; i++) { // this loop construct is used just to setup symmetrical pieces
            if (i == 0) {
                init_rooks(i);
            } else if (i == 1) {
                init_knights(i);
            } else if (i == 2) {
                init_bishops(i);
            } else {
                init_queens(i);
                init_kings(maxIndex-i);
            }
        }
    }

    private void init_rooks(int i) {
        board[0][i] = "black_rook";
        board[0][maxIndex - i] = "black_rook";
        board[7][i] = "white_rook";
        board[7][maxIndex - i] = "white_rook";
    }

    private void init_knights(int i) {
        board[0][i] = "black_knight";
        board[0][maxIndex - i] = "black_knight";
        board[7][i] = "white_knight";
        board[7][maxIndex - i] = "white_knight";
    }

    private void init_bishops(int i) {
        board[0][i] = "black_bishop";
        board[0][maxIndex - i] = "black_bishop";
        board[7][i] = "white_bishop";
        board[7][maxIndex - i] = "white_bishop";
    }

    private void init_queens(int i) {
        board[0][i] = "black_queen";
        board[7][i] = "white_queen";
    }

    private void init_kings(int i) {
        board[0][i] = "black_king";
        board[7][i] = "white_king";
    }

    private void init_pawns() {
        for (int i = 0; i < board[0].length; i++) {
            board[1][i] = "black_pawn";
            board[6][i] = "white_pawn";
        }
    }

    private void init_middle() {
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = "-";
            }
        }
    }

    /**
     * Promotes a pawn
     */
    private void promotePawn(int i, int j){
        board[i][j] = activeColor + "_queen";
        promotionTile = new int[] {i, j};
    }

    /**
     * Called after every valid move to check if the move involves a pawn that is to be promoted
     */
    private void checkForPromotion(int i, int j){
        if (activePiece.substring(6).equals("pawn") && i==0 && activeColor.equals("white")){
            promotePawn(i, j);
        } else if(activePiece.substring(6).equals("pawn") && i == 7 && activeColor.equals("black")){
            promotePawn(i, j);
        }
    }

    /**
     * Called after each valid move to check if dormantcolor is in a checked position.
     * */
    private void checkForCheck(int played_i, int played_j) {
        HashSet<int[]> threatPositions = new HashSet<>();
        int[] opposingKingsPosition = new int[]{-37,-37}; //just to initialize to something

        // loopa igenom hela brädet, för varje pjäs av rätt färg få möjliga drag och se om något av de går till den andres kung.
        String piece;
        String color;
        String pieceName;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pieceName = board[i][j];
                if (pieceName.equals("-")) {
                    continue; //ignore empty Tiles
                } else {
                    piece = pieceName.substring(6);
                    color = pieceName.substring(0, 5);
                }
                if (piece.equals("king") && color.equals(dormantColor)) {
                    opposingKingsPosition = new int[] {i, j}; // get opposing players king's position
                }

                // get all positions activeColor threatens after one successful move
                if (color.equals(activeColor)) {
                    activePiece = pieceName; // needed for calculateOkMoves() to work
                    calculateOkMoves(new int[]{i, j});

                    for (int inner_i = 0; inner_i < 8; inner_i++) {
                        for (int inner_j = 0; inner_j < 8; inner_j++) {
                            if (moveOk[inner_i][inner_j]) {
                                threatPositions.add(new int[] {inner_i, inner_j});
                            }
                        }
                    }
                }
            }
        }
        for (int[] threatPosition: threatPositions) {
            if (Arrays.equals(threatPosition, opposingKingsPosition)) { //check if dormant color is in checked position
                chessFlag = true;
                msg = dormantColor.replace(dormantColor.charAt(0), Character.toUpperCase(dormantColor.charAt(0)))
                        + " is Checked, try to escape!";
            }

        }
    }

    private void switch_turn() {
        activeColor = activeColor.equals(colors[1]) ? colors[0]: colors[1];
        dormantColor = dormantColor.equals(colors[0]) ? colors[1]: colors[0];
        if (chessFlag) {
            chessFlag = false; // if player moved into chess we don't want to do the msg change
            return;
        }
        msg = activeColor.replace(activeColor.charAt(0), Character.toUpperCase(activeColor.charAt(0))) + "'s turn, choose a piece to move";
    }

    private void moveToEmpty(int i, int j) {
        String tmp = board[i][j];
        board[i][j] = activePiece;
        board[activePosition[0]][activePosition[1]] = tmp;
        checkForPromotion(i, j);
        checkForCheck(i, j);
        activePiece = "";
        switch_turn();
    }

    private void moveToKill(int i, int j) {
        if (activeColor.equals("black")) whitePiecesTaken.add(board[i][j]);
        else blackPiecesTaken.add(board[i][j]);
        board[i][j] = activePiece;
        board[activePosition[0]][activePosition[1]] = "-";
        checkForPromotion(i, j);
        checkForCheck(i, j);
        activePiece = "";
        switch_turn();
    }

    private void calculateOkMoves(int[] activePosition){
        switch (activePiece.substring(6)) {
            case "pawn":
                moveOk = MoveOk.pawn(activePosition, activeColor, board);
                break;
            case "rook":
                moveOk = MoveOk.rook(activePosition, activeColor, board);
                break;
            case "knight":
                moveOk = MoveOk.knight(activePosition, activeColor, board);
                break;
            case "bishop":
                moveOk = MoveOk.bishop(activePosition, activeColor, board);
                break;
            case "queen":
                moveOk = MoveOk.queen(activePosition, activeColor, board);
                break;
            case "king":
                moveOk = MoveOk.king(activePosition, activeColor, board);
                break;
            default:
                throw new IllegalStateException("Unknown piece");
        }
    }

    @Override
    public boolean move(int i, int j) {
        // this chain handles the start of a move and deselection of piece
        if (activePiece.equals("")) { // player is to mark a piece for moving
            String tile = board[i][j];
            try {
                if (tile.substring(0, 5).equals(activeColor)) {
                    activePiece = tile;
                    activePosition = new int[] {i, j};
                    calculateOkMoves(activePosition);

                    msg = activeColor.replace(activeColor.charAt(0), Character.toUpperCase(activeColor.charAt(0)))
                            + "'s turn, pick a destination tile or pick another piece to move";
                } else throw new IndexOutOfBoundsException("Wrong color!");
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                msg = "Choose a " + activeColor + " piece";
            }
            return false; // move started
        } else if (i == activePosition[0] && j == activePosition[1]) { // player presses the same tile as already marked
            activePiece = "";
            msg = activeColor.replace(activeColor.charAt(0), Character.toUpperCase(activeColor.charAt(0))) + "'s turn, choose a piece to move";
            return false; // deselection, move not started
        }

        // this chain handles selection of tile to move to
        if (moveOk[i][j]) {
            if (board[i][j].equals("-")) { // destination empty
                moveToEmpty(i, j);
                return true;
            } else if (board[i][j].substring(0, 5).equals(dormantColor)) { // destination has enemy piece
                moveToKill(i, j);
                return true;
            }
        } else { // handles remarks and non-valid moves
            if (!board[i][j].equals("-") && board[i][j].substring(0, 5).equals(activeColor)) { // this is a remark, destination has a friendly piece
                activePosition = new int[] {i, j};
                activePiece = board[i][j];
                calculateOkMoves(activePosition);
            } else {
                msg = "Forbidden move, choose a correct destination tile";
            }
        }
        return false;
    }

    @Override
    public int[][] getTilesToUpdate(){
        int[] tmp = promotionTile;
        promotionTile = null;
        return new int[][] {tmp};
    }

    @Override
    public boolean[][] getValidMoves() {
        return moveOk;
    }

    @Override
    public Color[] getTileColors(){
        return tileColors;
    }

    @Override
    public Color getActiveTilesColor(){
        return activeTilesColor;
    }

    @Override
    public String getStatus(int i, int j) {
        return board[i][j];
    }

    @Override
    public String getMessage() {
        return this.msg;
    }

    @Override
    public String getTurn() {
        return activeColor;
    }

    @Override
    public String getActivePiece() {
        return activePiece;
    }
}