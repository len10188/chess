package ui;

import chess.ChessGame;


import static ui.EscapeSequences.*;


public class DrawBoard {
    private DrawBoard() {}

    private static final int CELL_W = 4;

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
        board[5] = fillRow(EMPTY);
        board[4] = fillRow(EMPTY);
        board[3] = fillRow(EMPTY);
        board[2] = fillRow(EMPTY);

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



        //Choose perspective
        int[] rankOrder = (persp == ChessGame.TeamColor.WHITE)
                ? new int []{7,6,5,4,3,2,1,0}
                : new int []{0,1,2,3,4,5,6,7};
        int[] fileOrder = (persp == ChessGame.TeamColor.WHITE)
                ? new int []{0,1,2,3,4,5,6,7}
                : new int []{7,6,5,4,3,2,1,0};

        // Top border
        stringBuilder.append(filesHeader(persp)).append('\n');

        for (int rank : rankOrder) {
            int rankNumber = rank + 1;
            // left label rank
            stringBuilder.append(SET_TEXT_COLOR_BLUE).append(' ').append(rankNumber).append(' ').append(RESET_TEXT_COLOR);

            for (int file : fileOrder) {

                boolean lightSquare = ((file + rankNumber) % 2 == 0);
                String bg = lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

                String piece = boardWhitePersp[rank][file];
                String glyph = (piece == null) ? EMPTY : piece;



                stringBuilder.append(bg).append(glyph).append(RESET_BG_COLOR);
            }

            // right rank label
            stringBuilder.append(' ').append(SET_TEXT_COLOR_BLUE).append(rankNumber).append(RESET_TEXT_COLOR).append('\n');
        }
        stringBuilder.append(filesHeader(persp)).append('\n');

        return stringBuilder.toString();
    }
    private static String filesHeader(ChessGame.TeamColor persp) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(SET_TEXT_COLOR_BLUE).append(EMPTY);
        if (persp == ChessGame.TeamColor.WHITE) {
            for (char file = 'a'; file <= 'h'; file++) {
                stringBuilder.append(center(String.valueOf(file), CELL_W));
            }
        } else {
            for (char file = 'h'; file >= 'a'; file--) {
                stringBuilder.append(center(String.valueOf(file), CELL_W));
            }
        }
        stringBuilder.append(RESET_TEXT_COLOR);
        return stringBuilder.toString();
    }

    private static String[] fillRow(String piece) {
        String[] row = new String[8];
        for (int i = 0; i < 8; i++) row[i] = (piece == null) ? null : piece;
        return row;
    }

    public static String center (String s, int width) {
        int i = width + 1;
        return (" " + s + " ");
    }


}
