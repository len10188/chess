package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = pieceColor;

        List<ChessMove> moves = new ArrayList<>();

        if (piece.getPieceType() == PieceType.BISHOP){
            // Add all possible diagonals to move list
            moveInDirection(moves, myPosition, 1, 1, board, myColor); // up-right
            moveInDirection(moves, myPosition, -1,1, board, myColor); // down-right
            moveInDirection(moves, myPosition, -1, -1, board, myColor); // down-left
            moveInDirection(moves, myPosition, 1,-1, board, myColor); // up-left
        }
        if (piece.getPieceType() == PieceType.ROOK){
            moveInDirection(moves, myPosition,1, 0, board, myColor); // up
            moveInDirection(moves, myPosition, -1, 0, board, myColor); // down
            moveInDirection(moves, myPosition, 0, 1, board, myColor); // right
            moveInDirection(moves, myPosition, 0, -1, board, myColor); // left
        }
        if (piece.getPieceType() == PieceType.KNIGHT){
            moveSinglePos(moves, myPosition, 2, 1, board, myColor);
            moveSinglePos(moves, myPosition, -2, 1, board, myColor);
            moveSinglePos(moves, myPosition, 2, -1, board, myColor);
            moveSinglePos(moves, myPosition, -2, -1, board, myColor);
            moveSinglePos(moves, myPosition, 1, 2, board, myColor);
            moveSinglePos(moves, myPosition, -1, 2, board, myColor);
            moveSinglePos(moves, myPosition, 1, -2, board, myColor);
            moveSinglePos(moves, myPosition, -1, -2, board, myColor);
        }
        if (piece.getPieceType() == PieceType.KING) {
            moveSinglePos(moves, myPosition, 1, -1, board, myColor);
            moveSinglePos(moves, myPosition, 1, 0, board, myColor);
            moveSinglePos(moves, myPosition, 1, 1, board, myColor);
            moveSinglePos(moves, myPosition, 0, -1, board, myColor);
            moveSinglePos(moves, myPosition, 0,1, board, myColor);
            moveSinglePos(moves, myPosition, -1, -1, board, myColor);
            moveSinglePos(moves, myPosition, -1, 0, board, myColor);
            moveSinglePos(moves, myPosition, -1, 1, board, myColor);
        }
        if (piece.getPieceType() == PieceType.QUEEN) {
            // Diagonal
            moveInDirection(moves, myPosition, 1, 1, board, myColor); // up-right
            moveInDirection(moves, myPosition, -1,1, board, myColor); // down-right
            moveInDirection(moves, myPosition, -1, -1, board, myColor); // down-left
            moveInDirection(moves, myPosition, 1,-1, board, myColor); // up-left
            // Straight
            moveInDirection(moves, myPosition,1, 0, board, myColor); // up
            moveInDirection(moves, myPosition, -1, 0, board, myColor); // down
            moveInDirection(moves, myPosition, 0, 1, board, myColor); // right
            moveInDirection(moves, myPosition, 0, -1, board, myColor); // left
        }
        if (piece.getPieceType() == PieceType.PAWN){
            int rowDir;

            // ROW DIRECTION based on color
            if (myColor == ChessGame.TeamColor.WHITE) {
                rowDir = 1; // white piece moves down board.
            }else {
                rowDir = -1; // black piece moves up board.
            }
            // FIRST MOVE
            if (myColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2){ // start position
                pawnFistMove(moves, myPosition, rowDir, board);
                pawnCaptureMove(moves, myPosition, board, myColor);
            }
            else if (myColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7){ // start position
                pawnFistMove(moves, myPosition, rowDir, board);
                pawnCaptureMove(moves, myPosition, board, myColor);
            } else{
                pawnMoveSingleForward(moves, myPosition, board, rowDir);
                pawnCaptureMove(moves, myPosition, board, myColor);
            }
        }
        return moves;
    }
    private void addPawnMoves(List<ChessMove> moves, ChessPosition startPos, ChessPosition testPosition){
        if (testPosition.getRow() == 1 || testPosition.getRow() == 8) {
            moves.add(new ChessMove(startPos, testPosition, PieceType.QUEEN));
            moves.add(new ChessMove(startPos, testPosition, PieceType.BISHOP));
            moves.add(new ChessMove(startPos, testPosition, PieceType.ROOK));
            moves.add(new ChessMove(startPos, testPosition, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(startPos, testPosition, null));
        }
    }

    private void pawnMoveSingleForward(List<ChessMove> moves, ChessPosition startPos, ChessBoard board, int rowDir){
        ChessPosition testPosition= new ChessPosition(startPos.getRow() + rowDir, startPos.getColumn());

        if(checkValidPosition(testPosition)){
            ChessPiece pieceAt = board.getPiece(testPosition);
            if (pieceAt == null) {
                addPawnMoves(moves, startPos, testPosition);
            }
        }
    }

    private void pawnCaptureMove(List<ChessMove> moves, ChessPosition startPos, ChessBoard board,  ChessGame.TeamColor myColor) {
        int rowDir;

        if (myColor == ChessGame.TeamColor.WHITE) {
            rowDir = 1; // white piece moves down board.
        } else {
            rowDir = -1; // black piece moves up board.
        }

        ChessPosition leftDiag = new ChessPosition(startPos.getRow() + rowDir, startPos.getColumn() - 1);
        if (checkValidPosition(leftDiag)) {
            addPawnCaptureMove(moves, startPos, leftDiag, board, myColor);
        }
        ChessPosition rightDiag = new ChessPosition(startPos.getRow() + rowDir, startPos.getColumn() + 1);
        if (checkValidPosition(rightDiag)) {
            addPawnCaptureMove(moves, startPos, rightDiag, board, myColor);
        }
    }

    private void addPawnCaptureMove(List<ChessMove> moves, ChessPosition startPos, ChessPosition testPosition ,
                                    ChessBoard board,  ChessGame.TeamColor myColor){
        ChessPiece pieceAt = board.getPiece(testPosition);
        if (pieceAt != null && pieceAt.getTeamColor() != myColor){
            addPawnMoves(moves, startPos, testPosition);
        }
    }

    private void pawnFistMove(List<ChessMove> moves, ChessPosition startPosition, int rowDir,
                              ChessBoard board){
        ChessPosition oneForward = new ChessPosition(startPosition.getRow()+ rowDir, startPosition.getColumn());
        ChessPosition twoForward = new ChessPosition(startPosition.getRow() + (rowDir*2), startPosition.getColumn());
        ChessPiece pieceOneForward = board.getPiece(oneForward);
        ChessPiece pieceTwoForward = board.getPiece(twoForward);

        if (pieceOneForward == null){
            moves.add (new ChessMove(startPosition, oneForward, null));
            if (pieceTwoForward == null){
                moves.add (new ChessMove(startPosition, twoForward, null));
            }
        }
    }

    // Check if position is valid on the board
    public boolean checkValidPosition(ChessPosition currentPosition){
        // Check if move on board.
        return currentPosition.getRow() <= 8 &&
                currentPosition.getColumn() <= 8 &&
                currentPosition.getRow() >= 1 &&
                currentPosition.getColumn() >= 1;
    }
    //move in a direction.
    private void moveInDirection(List<ChessMove> moves, ChessPosition startPos,
                                 int rowDir, int colDir, ChessBoard board, ChessGame.TeamColor myColor){
        int row = startPos.getRow() +rowDir;
        int col = startPos.getColumn() + colDir;

        //valid position & not blocked.
        while (checkValidPosition(new ChessPosition(row,col))){
            ChessPosition testPosition = new ChessPosition(row, col);
            ChessPiece pieceAt = board.getPiece(testPosition);

            // Check if route is blocked.
            if (pieceAt == null) { // no piece blocking route
                moves.add(new ChessMove(startPos, testPosition, null));
            } else { // if piece is opposite color add pos else break
                if (checkCanCapture(pieceAt, myColor)) {
                    moves.add(new ChessMove(startPos, testPosition, null));
                }
                break;
            }

        row += rowDir;
        col += colDir;
        }
    }
    private void moveSinglePos (List<ChessMove> moves, ChessPosition startPos, int rowDir, int colDir,
                                ChessBoard board, ChessGame.TeamColor myColor){
        ChessPosition testPosition = new ChessPosition(startPos.getRow()+rowDir, startPos.getColumn()+colDir);

        if (checkValidPosition(testPosition)){
            ChessPiece pieceAt = board.getPiece(testPosition);
            if (pieceAt == null) {
                moves.add(new ChessMove(startPos, testPosition, null));
            } else {
                if(checkCanCapture(pieceAt, myColor)){
                    moves.add(new ChessMove(startPos, testPosition, null));
                }
            }
        }
    }
    private boolean checkCanCapture(ChessPiece testPiece, ChessGame.TeamColor myColor){
        return testPiece.getTeamColor() != myColor; // returns true if can capture, false if same team.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
