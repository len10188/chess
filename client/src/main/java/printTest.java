import chess.*;
import ui.DrawBoard;

public class printTest {
    public static void main(String[] args) {
        // Render the initial board from White's perspective
        String boardWhite = DrawBoard.renderInitial(ChessGame.TeamColor.WHITE);
        System.out.println("White's Perspective:");
        System.out.println(boardWhite);

        // Render the initial board from Black's perspective
        String boardBlack = DrawBoard.renderInitial(ChessGame.TeamColor.BLACK);
        System.out.println("Black's Perspective:");
        System.out.println(boardBlack);
    }
}
