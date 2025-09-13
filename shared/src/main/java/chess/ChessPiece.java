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
            MoveInDirection(moves, myPosition, 1, 1, board, myColor); // up-right
            MoveInDirection(moves, myPosition, -1,1, board, myColor); // down-right
            MoveInDirection(moves, myPosition, -1, -1, board, myColor); // down-left
            MoveInDirection(moves, myPosition, 1,-1, board, myColor); // up-left
        }
        if (piece.getPieceType() == PieceType.ROOK){
            MoveInDirection(moves, myPosition,1, 0, board, myColor); // up
            MoveInDirection(moves, myPosition, -1, 0, board, myColor); // down
            MoveInDirection(moves, myPosition, 0, 1, board, myColor); // right
            MoveInDirection(moves, myPosition, 0, -1, board, myColor); // left
        }
        if (piece.getPieceType() == PieceType.KNIGHT){
            MoveSinglePos(moves, myPosition, 2, 1, board, myColor);
            MoveSinglePos(moves, myPosition, -2, 1, board, myColor);
            MoveSinglePos(moves, myPosition, 2, -1, board, myColor);
            MoveSinglePos(moves, myPosition, -2, -1, board, myColor);
            MoveSinglePos(moves, myPosition, 1, 2, board, myColor);
            MoveSinglePos(moves, myPosition, -1, 2, board, myColor);
            MoveSinglePos(moves, myPosition, 1, -2, board, myColor);
            MoveSinglePos(moves, myPosition, -1, -2, board, myColor);
        }
        if (piece.getPieceType() == PieceType.KING) {
            MoveSinglePos(moves, myPosition, 1, -1, board, myColor);
            MoveSinglePos(moves, myPosition, 1, 0, board, myColor);
            MoveSinglePos(moves, myPosition, 1, 1, board, myColor);
            MoveSinglePos(moves, myPosition, 0, -1, board, myColor);
            MoveSinglePos(moves, myPosition, 0,1, board, myColor);
            MoveSinglePos(moves, myPosition, -1, -1, board, myColor);
            MoveSinglePos(moves, myPosition, -1, 0, board, myColor);
            MoveSinglePos(moves, myPosition, -1, 1, board, myColor);
        }
        if (piece.getPieceType() == PieceType.QUEEN) {
            // Diagonals
            MoveInDirection(moves, myPosition, 1, 1, board, myColor); // up-right
            MoveInDirection(moves, myPosition, -1,1, board, myColor); // down-right
            MoveInDirection(moves, myPosition, -1, -1, board, myColor); // down-left
            MoveInDirection(moves, myPosition, 1,-1, board, myColor); // up-left
            // Straights
            MoveInDirection(moves, myPosition,1, 0, board, myColor); // up
            MoveInDirection(moves, myPosition, -1, 0, board, myColor); // down
            MoveInDirection(moves, myPosition, 0, 1, board, myColor); // right
            MoveInDirection(moves, myPosition, 0, -1, board, myColor); // left
        }
        if (piece.getPieceType() == PieceType.PAWN){
            int colDir;
            if (myColor == ChessGame.TeamColor.WHITE){
                colDir = 1; // move up
                if (myPosition.getRow() == 2){

                }
            }
        }
        return moves;
    }
    public void pawnMoves(List<ChessMove> moves, ChessPosition startPos
    // Check if position is valid on the board
    public boolean checkValidPosition(ChessPosition currentPosition){
        // Check if move on board.
        return currentPosition.getRow() <= 8 &&
                currentPosition.getColumn() <= 8 &&
                currentPosition.getRow() >= 1 &&
                currentPosition.getColumn() >= 1;
    }
    //move in a direction.
    private void MoveInDirection(List<ChessMove> moves, ChessPosition startPos, int rowDir, int colDir, ChessBoard board, ChessGame.TeamColor myColor){
        int row = startPos.getRow() +rowDir;
        int col = startPos.getColumn() + colDir;

        //valid position & not blocked.
        while (checkValidPosition(new ChessPosition(row,col))){
            ChessPosition testPosition = new ChessPosition(row, col);
            ChessPiece pieceAt = board.getPiece((testPosition));

            // Check if route is blocked.
            if (pieceAt == null) { // no piece blocking route
                moves.add(new ChessMove(startPos, testPosition, null));
            } else { // if piece is opposite color add pos else break
                if (checkCanCapture(pieceAt, board, myColor)) {
                    moves.add(new ChessMove(startPos, testPosition, null));
                }
                break;
            }

        row += rowDir;
        col += colDir;
        }
    }
    private void MoveSinglePos (List<ChessMove> moves, ChessPosition startPos, int rowDir, int colDir, ChessBoard board, ChessGame.TeamColor myColor){
        ChessPosition testPosition = new ChessPosition(startPos.getRow()+rowDir, startPos.getColumn()+colDir);

        if (checkValidPosition(testPosition)){
            ChessPiece pieceAt = board.getPiece(testPosition);
            if (pieceAt == null) {
                moves.add(new ChessMove(startPos, testPosition, null));
            } else {
                if(checkCanCapture(pieceAt, board, myColor)){
                    moves.add(new ChessMove(startPos, testPosition, null));
                }
            }
        }
    }
    private boolean checkCanCapture(ChessPiece testPiece, ChessBoard board, ChessGame.TeamColor myColor){
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
