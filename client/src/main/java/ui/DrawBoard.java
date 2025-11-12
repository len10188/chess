package ui;

import chess.ChessGame;

public class DrawBoard {
    private DrawBoard() {}

    // COLORS
    private static final String LIGHT = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK = EscapeSequences.SET_BG_COLOR_BLACK;
    private static final String RESET = EscapeSequences.RESET_BG_COLOR;

    private static final String CELL_PAD = EscapeSequences.EMPTY;

    public static String renderInitial(ChessGame.TeamColor perspective) {
        char[] whiteBack = pieceRow(true);
        char[] whitePawn = pawnRow(true);
        char[] blackBack = pieceRow(false);
        char[] blackPawns = pawnRow(false);

        // make 8x8 char array
        char[][] board = new char[8][8];

        // row 8 is black back row (index 7), row 1 is white back row (index 0)
        board[7] = blackBack;
        board[6] = blackPawns;
        board[5] = emptyRow();
        board[4] = emptyRow();
        board[3] = emptyRow();
        board[2] = emptyRow();
        board[1] = whitePawn;
        board[0] = whiteBack;

        return DrawBoard.renderBoard(board, perspective);
    }

    private static String renderBoard(char[][] boardWhitePersp, ChessGame.TeamColor persp) {
        StringBuilder stringBuilder = new StringBuilder();

        //Choose perspective
        int startRank = (persp == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRank = (persp == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rankStep = (persp == ChessGame.TeamColor.WHITE) ? 1 : -1;
    }
}
