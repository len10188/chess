import chess.*;
import ui.PrintBoard;

public class PrintTest {
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println("White's perspective:");
        System.out.println(PrintBoard.render(board, ChessGame.TeamColor.WHITE));

        System.out.println("Black's perspective:");
        System.out.println(PrintBoard.render(board, ChessGame.TeamColor.BLACK));
    }
}
