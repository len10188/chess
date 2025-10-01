package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard(); // make a board
        board.resetBoard(); // set up a new board
        currentTurn = TeamColor.WHITE; // game starts on White team
        }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn ;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team; // updates currentTurn
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return Collections.emptyList();
        }
        Collection<ChessMove> candidateMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : candidateMoves) {
            ChessBoard copyBoard = copyBoard(board);
            applyMove(copyBoard, move);

            // check if move puts
            ChessBoard originalBoard = board;
            board = copyBoard;
            boolean safe = !isInCheck(piece.getTeamColor());
            board = originalBoard;

            if (safe) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece((move.getStartPosition()));

        if(piece == null || piece.getTeamColor() != currentTurn){
            System.out.println("invalid move");
            throw new InvalidMoveException("Invalid Move");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (!legalMoves.contains(move)){
            throw new InvalidMoveException("Move is not legal");
        }

        applyMove(board, move);

        changeTurn();

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor, board);

        // scan board
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                // check every enemy piece
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> enemyMoves = piece.pieceMoves(board, position);
                    //check every enemy piece's potential moves
                    if(canAttackKing(enemyMoves, kingPosition)){
                        return true;
                    }
                }
            }
        }
        return false; // king is safe
    }

    private static boolean canAttackKing(Collection<ChessMove> enemyMoves, ChessPosition kingPosition) {
        for (ChessMove move : enemyMoves){
            if (move.getEndPosition().equals(kingPosition)){
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && checkPossibleMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && checkPossibleMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Find the position of the king
     *
     * @return king position
     */
    public ChessPosition findKing(ChessGame.TeamColor teamColor, ChessBoard board){
        // loop through every square until the king is found
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition testPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(testPosition);

                // check if the piece is king and on correct team
                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return testPosition; // return found king position
                }
            }
        }
        return null;
    }

    public ChessBoard copyBoard(ChessBoard ogBoard){
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = ogBoard.getPiece(position);

                if(piece != null){
                    ChessPiece newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    newBoard.addPiece(position, newPiece);
                }
            }
        }
        return newBoard;
    }

    public void applyMove(ChessBoard board, ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece.PieceType promotionType = move.getPromotionPiece();

        // get piece
        ChessPiece piece = board.getPiece(start);

        // remove piece from start square
        board.addPiece(start, null);

        // promotion piece
        if (promotionType != null){
            ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), promotionType);
            board.addPiece(end, promotedPiece);
        }else{
            board.addPiece(end, piece);
        }
    }

    public void changeTurn(){
        if(currentTurn == TeamColor.WHITE){
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    public boolean checkPossibleMoves(TeamColor teamColor){
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor){
                    if (!validMoves(position).isEmpty()){
                        return false; // there are NO moves
                    }
                }
            }
        }
        return true; // there ARE moves
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }
}
