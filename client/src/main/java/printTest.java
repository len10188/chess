import chess.*;
import ui.DrawBoard;
import ui.printBoard;

public class printTest {
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println("White's perspective:");
        System.out.println(printBoard.render(board, ChessGame.TeamColor.WHITE));

        System.out.println("Black's perspective:");
        System.out.println(printBoard.render(board, ChessGame.TeamColor.BLACK));
    }
}
