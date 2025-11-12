package ui;

import chess.ChessGame;

import static ui.EscapeSequences.*;


public class DrawBoard {
    private DrawBoard() {}

    public static String renderInitial(ChessGame.TeamColor perspective) {

        // make 8x8 char array
        String[][] board = new String[8][8];

        // Rank 8: black pieces
        board[7] = new String[]{
            BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK
            };
        // Rank 7: black pawn
        board[6] = fillRow(BLACK_PAWN);

        // RANK 6 to 3: empty
        board[5] = fillRow(null);
        board[4] = fillRow(null);
        board[3] = fillRow(null);
        board[2] = fillRow(null);

        //Rank 2: white pawn
        board[1] = fillRow(WHITE_PAWN);

        //Rank 1: white pieces
        board[0] = new String[]{
                WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK
        };

        return renderBoard(board, perspective);

    }

    private static String renderBoard(String[][] boardWhitePersp, ChessGame.TeamColor persp) {
        StringBuilder stringBuilder = new StringBuilder();

        // Top border
        stringBuilder.append(filesHeader(persp)).append('\n');

        //Choose perspective
        int startRank = (persp == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRank = (persp == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rankStep = (persp == ChessGame.TeamColor.WHITE) ? 1 : -1;

        char startFile = (persp == ChessGame.TeamColor.WHITE) ? 'a' : 'h';
        char endFile = (persp == ChessGame.TeamColor.WHITE) ? 'h' : 'a' ;
        int fileStep = (persp == ChessGame.TeamColor.WHITE) ? 1 : -1;

        for (int rank = startRank; rank != endRank; rank += rankStep) {
            // label rank
            stringBuilder.append(SET_TEXT_COLOR_BLUE).append(' ').append(RESET_TEXT_COLOR);

            for (char file = startFile; file != endFile; file += fileStep) {
                int fileIndex = file - 'a';
                int rankIndex = rank - 1;

                boolean lightSquare = ((fileIndex +rank) % 2 == 0);
                String bg = lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

                String cell = boardWhitePersp[rankIndex][fileIndex];
                if (cell == null) cell = EMPTY;

                stringBuilder.append(cell).append(RESET_BG_COLOR);
            }

            // right rank label
            stringBuilder.append(' ').append(SET_TEXT_COLOR_BLUE).append(rank).append(RESET_TEXT_COLOR).append('\n');
        }
        stringBuilder.append(filesHeader(persp)).append('\n');

        return stringBuilder.toString();
    }
    private static String filesHeader(ChessGame.TeamColor persp) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("   "); // left margin under left rank label
        stringBuilder.append(SET_TEXT_COLOR_BLUE);
        if (persp == ChessGame.TeamColor.WHITE) {
            for (char file = 'a'; file <= 'h'; file++) stringBuilder.append(file).append(' ');
        } else {
            for (char file = 'h'; file >= 'a'; file--) stringBuilder.append(file).append(' ');
        }
        stringBuilder.append(RESET_TEXT_COLOR);
        return stringBuilder.toString();
    }

    private static String[] fillRow(String piece) {
        String[] row = new String[8];
        for (int i = 0; i < 8; i++) row[i] = (piece == null) ? null : piece;
        return row;
    }
}
