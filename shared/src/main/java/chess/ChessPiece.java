package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        List<ChessMove> moves = new ArrayList<>();

        if (piece.getPieceType() == PieceType.BISHOP){

            // Add all possible diagonals to move list
            MoveInDirection(moves, myPosition, 1, 1);
            MoveInDirection(moves, myPosition, -1,1);
            MoveInDirection(moves, myPosition, -1, -1);
            MoveInDirection(moves, myPosition, 1,-1);
        }
        return moves;
    }
    // Check if position is valid on the board
    public boolean checkValidPosition(ChessPosition currentPosition){
        return currentPosition.getRow() <= 8 &&
                currentPosition.getColumn() <= 8 &&
                currentPosition.getRow() >= 1 &&
                currentPosition.getColumn() >= 1;
    }
    //move in a direction.
    private void MoveInDirection(List<ChessMove> moves, ChessPosition startPos, int rowDir, int colDir){
        int row = startPos.getRow() +rowDir;
        int col = startPos.getColumn() + colDir;

        while (checkValidPosition(new ChessPosition(row,col))){
            ChessPosition testPosition = new ChessPosition(row, col);
            moves.add(new ChessMove(startPos, testPosition, null));

            row += rowDir;
            col += colDir;
        }
    }
}
