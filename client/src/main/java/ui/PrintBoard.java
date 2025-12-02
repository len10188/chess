package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_BISHOP;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;
import static ui.EscapeSequences.BLACK_QUEEN;
import static ui.EscapeSequences.BLACK_ROOK;
import static ui.EscapeSequences.EMPTY;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.WHITE_BISHOP;
import static ui.EscapeSequences.WHITE_KING;
import static ui.EscapeSequences.WHITE_KNIGHT;
import static ui.EscapeSequences.WHITE_PAWN;
import static ui.EscapeSequences.WHITE_QUEEN;
import static ui.EscapeSequences.WHITE_ROOK;

public class PrintBoard {

    private static final int CELL_W = 4;

    public static String render(ChessBoard board, ChessGame.TeamColor persp) {
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
            stringBuilder.append(SET_BG_COLOR_NAVY_BLUE).append(SET_TEXT_COLOR_WHITE).append(" ")
                    .append(rankNumber)
                    .append(" ").append(QUARTER_SPACE).append(LITTLE_SPACE)
                    .append(RESET_TEXT_COLOR).append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);

            // make each column on board for the row.
            for (int file : fileOrder) {

                boolean lightSquare = ((file + rankNumber) % 2 == 0);
                String bg = lightSquare ? SET_BG_COLOR_LIGHT_LIGHT_GREY : SET_BG_COLOR_LIGHT_GREY;

                ChessPiece piece = board.getPiece(new ChessPosition(rankNumber, file + 1));
                String glyph = (piece == null) ? EMPTY : pieceToGlyph(piece);



                stringBuilder.append(bg).append(SET_TEXT_COLOR_BLACK).append(glyph).
                        append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);
            }

            // right rank label
            stringBuilder.append(SET_BG_COLOR_NAVY_BLUE).append(SET_TEXT_COLOR_WHITE).append(" ")
                    .append(QUARTER_SPACE).append(rankNumber)
                    .append(" ").append(LITTLE_SPACE).append(LITTLE_SPACE).append(RESET_TEXT_COLOR)
                    .append(RESET_BG_COLOR).append('\n');
        }
        // bottom file footer
        stringBuilder.append(filesHeader(persp)).append('\n');

        return stringBuilder.toString();
    }
    private static String filesHeader(ChessGame.TeamColor persp) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(SET_BG_COLOR_NAVY_BLUE).append(SET_TEXT_COLOR_WHITE).append(EMPTY);
        if (persp == ChessGame.TeamColor.WHITE) {
            for (char file = 'a'; file <= 'h'; file++) {
                stringBuilder.append(HALF_SPACE).append(center(String.valueOf(file), CELL_W));
            }
        } else {
            for (char file = 'h'; file >= 'a'; file--) {
                stringBuilder.append(HALF_SPACE).append(center(String.valueOf(file), CELL_W));
            }
        }
        stringBuilder.append("   ").append(LITTLE_SPACE).append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);
        return stringBuilder.toString();
    }


    public static String center (String s, int width) {

        return (BIG_HALF_SPACE + HALF_SPACE + s + HALF_SPACE + BIG_HALF_SPACE);
    }

    private static String pieceToGlyph (ChessPiece piece){
        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();

        switch (type) {
            case KING:   return color == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN:  return color == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK:   return color == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP: return color == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT: return color == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN:   return color == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
            default:     return EMPTY;
        }
    }

    public static String renderHighlighted(ChessBoard board, ChessGame.TeamColor perspective,
                                           ChessPosition selected, Collection<ChessMove> legalMoves) {
        Set<ChessPosition> targets = new HashSet<>();
        if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                targets.add(move.getEndPosition());
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        int[] rankOrder;
        int[] fileOrder;
        if (perspective == ChessGame.TeamColor.WHITE){
            rankOrder = new int[]{7,6,5,4,3,2,1,0};
            fileOrder = new int[]{0,1,2,3,4,5,6,7};
        }else { // Black
            rankOrder = new int[]{0,1,2,3,4,5,6,7};
            fileOrder = new int[]{7,6,5,4,3,2,1,0};
        }

        stringBuilder.append(filesHeader(perspective)).append("\n");

        for (int rank : rankOrder) {
            int rankNumber = rank + 1;

            // left rank label
            stringBuilder.append(SET_BG_COLOR_NAVY_BLUE).append(SET_TEXT_COLOR_WHITE).append(" ")
                    .append(rankNumber)
                    .append(" ").append(QUARTER_SPACE).append(LITTLE_SPACE)
                    .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);

            for (int file : fileOrder) {
                ChessPosition pos = new ChessPosition(rankNumber, file + 1);

                boolean lightSquare = ((file + rankNumber) % 2 == 0);
                String bg = lightSquare ? SET_BG_COLOR_LIGHT_LIGHT_GREY : SET_BG_COLOR_LIGHT_GREY;

                // highlight background if selected / target
                if (selected != null && pos.equals(selected)) {
                    // pick whatever highlight color you like from EscapeSequences
                    bg = SET_BG_COLOR_GREEN; // <-- adjust if your EscapeSequences uses a different name
                } else if (targets.contains(pos)) {
                    bg = SET_BG_COLOR_YELLOW; // <-- adjust name as needed
                }

                ChessPiece piece = board.getPiece(pos);
                String glyph = (piece == null) ? EMPTY : pieceToGlyph(piece);

                stringBuilder.append(bg).append(SET_TEXT_COLOR_BLACK).append(glyph)
                        .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);
            }

            // right rank label
            stringBuilder.append(SET_BG_COLOR_NAVY_BLUE).append(SET_TEXT_COLOR_WHITE).append(" ")
                    .append(QUARTER_SPACE).append(rankNumber)
                    .append(" ").append(LITTLE_SPACE).append(LITTLE_SPACE)
                    .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append('\n');
        }

        stringBuilder.append(filesHeader(perspective)).append('\n');
        return stringBuilder.toString();
    }

}
