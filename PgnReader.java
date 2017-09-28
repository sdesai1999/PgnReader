import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PgnReader {

    /**
     * Find the tagName tag pair in a PGN game and return its value.
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm
     *
     * @param tagName the name of the tag whose value you want
     * @param game a `String` containing the PGN text of a chess game
     * @return the value in the named tag pair
     */
    public static String tagValue(String tagName, String game) {
        if (!(game.contains(tagName))) {
            return "NOT GIVEN";
        }
        int beginningOfLine = game.indexOf(tagName) - 1;
        int endOfLine = game.indexOf("]", beginningOfLine) + 1;
        String eventLine = game.substring(beginningOfLine, endOfLine);
        String[] splitUpTag = eventLine.split("\"");
        return splitUpTag[1];
    }

    /**
     * Play out the moves in game and return a String with the game's
     * final position in Forsyth-Edwards Notation (FEN).
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm#c16.1
     *
     * @param game a `Strring` containing a PGN-formatted chess game or opening
     * @return the game's final position in FEN.
     */
    public static String finalPosition(String game) {
        char[][] chessBoard = initializeBoard();
        boolean gameExists = true;
        int tempStartIndex = game.lastIndexOf("]");
        int gameStartIndex = -1;
        String tmpMovesOnly = "", gameMovesOnly = "";
        if (tempStartIndex == -1) {
            gameStartIndex = game.indexOf("1.");
            if (gameStartIndex == -1) {
                gameExists = false;
            } else {
                gameMovesOnly = game.substring(gameStartIndex);
            }
        } else {
            tmpMovesOnly = game.substring(tempStartIndex);
            int first1Index = tmpMovesOnly.indexOf("1.");
            if (first1Index == -1) {
                gameExists = false;
            } else {
                gameMovesOnly = tmpMovesOnly.substring(first1Index);
            }
        }

        if (gameExists) {
            // split the game on any whitespace (spaces and/or newlines)
            String[] movesArray = gameMovesOnly.split("\\s+");
            int moveToPerform = -1;
            boolean toContinue = true;
            String whiteW = "1-0";
            String blackW = "0-1";
            String drawStr = "1/2-1/2";
            String moveString = "";
            boolean isWhiteW = false, isBlackW = false, isDraw = false;
            for (int i = 0; i < movesArray.length; i++) {
                moveString = movesArray[i];
                isWhiteW = moveString.equals(whiteW);
                isBlackW = moveString.equals(blackW);
                isDraw = moveString.equals(drawStr);
                if ((i % 3 != 0) && toContinue) {
                    moveToPerform = determineMoveType(movesArray[i]);
                    if (!isWhiteW && !isBlackW && !isDraw) {
                        chessBoard = performMove(moveToPerform, i % 3 - 1,
                            movesArray[i], chessBoard);
                        // if white i % 3 - 1 = 0; if black i % 3 - 1 = 1
                    } else {
                        toContinue = false;
                    }
                }
            }
        }
        System.out.println();
        printBoard(chessBoard);
        System.out.println();
        return getFEN(chessBoard);
    }

    /**
     * Reads the file named by path and returns its content as a String.
     *
     * @param path the relative or abolute path of the file to read
     * @return a String containing the content of the file
     */
    public static String fileContent(String path) {
        Path file = Paths.get(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Add the \n that's removed by readline()
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            System.exit(1);
        }
        return sb.toString();
    }

    public static char[][] initializeBoard() {
        char[][] board = new char[8][8];
        board[0][0] = 'r';
        board[0][1] = 'n';
        board[0][2] = 'b';
        board[0][3] = 'q';
        board[0][4] = 'k';
        board[0][5] = 'b';
        board[0][6] = 'n';
        board[0][7] = 'r';
        for (int i = 0; i < board.length; i++) {
            board[1][i] = 'p';
        }

        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = ' ';
            }
        }

        for (int i = 0; i < board.length; i++) {
            board[6][i] = 'P';
        }
        board[7][0] = 'R';
        board[7][1] = 'N';
        board[7][2] = 'B';
        board[7][3] = 'Q';
        board[7][4] = 'K';
        board[7][5] = 'B';
        board[7][6] = 'N';
        board[7][7] = 'R';
        return board;
    }

    public static int getRow(int rank) {
        return 8 - rank; // converts rank number to row for 2d array
    }

    public static int getCol(char file) {
        int fileInt = file - 0;
        return fileInt - 97; // converts file letter to a column for 2d array
    }

    public static int determineMoveType(String move) {
        boolean containsEquals = move.contains("="); // for pawn promotions
        boolean containsX = move.contains("x");
        // getting rid of x, !, ?, +, #, and e.p. to simplify moves
        move = move.replace("x", "");
        move = move.replace("!", "");
        move = move.replace("?", "");
        move = move.replace("+", "");
        move = move.replace("#", "");
        move = move.replace("e.p.", "");
        int length = move.length();
        String piece = move.substring(0, 1);
        if (length == 2 || ((containsEquals && !containsX))) {
            return 0; // pawn move forward
        } else if (length == 3 && piece.equals("R")) {
            return 1; // rook move
        } else if ((length >= 3 && !(isUpperCase(piece)))) {
            return 2; // pawn capture move
        } else if (length == 3 && piece.equals("B")) {
            return 3; // bishop move
        } else if (length == 3 && piece.equals("K")) {
            return 4; // king move
        } else if (length == 3 && piece.equals("Q")) {
            return 5; // queen move
        } else if (length == 3 && piece.equals("N")) {
            return 6; // knight move
        } else if (length == 3 && move.equals("O-O")) {
            return 7; // king-side castle
        } else if (length == 5 && move.equals("O-O-O")) {
            return 8; // queen-side castle
        } else if (length == 4 && piece.equals("R")) {
            return 9; // disambiguating rook move (rank or file)
        } else if (length == 4 && piece.equals("N")) {
            return 10; // disambiguating knight move (rank or file)
        } else if (length == 4 && piece.equals("Q")) {
            return 11; // disambiguating queen move (rank or file)
        } else if (length == 4 && piece.equals("B")) {
            return 12; // disambiguating bishop move (rank or file)
        } else {
            System.out.println("THIS SHOULDN'T HAPPEN");
            return -1;
        }
    }

    public static char[][] performMove(int moveType, int color, String move,
        char[][] board) {
        move = move.replace("x", "");
        move = move.replace("!", "");
        move = move.replace("?", "");
        move = move.replace("+", "");
        move = move.replace("#", "");
        move = move.replace("e.p.", "");
        if (moveType == 0 && color == 0) { // white pawn move forward
            board = whitePawnMove(move, board);
        } else if (moveType == 0 && color == 1) { // black pawn move forward
            board = blackPawnMove(move, board);
        } else if (moveType == 1 && color == 0) { // white rook move
            board = rookMove(move, board, 'R');
        } else if (moveType == 1 && color == 1) { // black rook move
            board = rookMove(move, board, 'r');
        } else if (moveType == 2 && color == 0) { // white pawn capture
            board = pawnCapture(move, board, true);
        } else if (moveType == 2 && color == 1) { // black pawn capture
            board = pawnCapture(move, board, false);
        } else if (moveType == 3 && color == 0) { // white bishop move
            board = bishopMove(move, board, 'B');
        } else if (moveType == 3 && color == 1) { // black bishop move
            board = bishopMove(move, board, 'b');
        } else if (moveType == 4 && color == 0) { // white king move
            board = kingMove(move, board, 'K');
        } else if (moveType == 4 && color == 1) { // black king move
            board = kingMove(move, board, 'k');
        } else if (moveType == 5 && color == 0) { // white queen move
            board = queenMove(move, board, 'Q');
        } else if (moveType == 5 && color == 1) { // black queen move
            board = queenMove(move, board, 'q');
        } else if (moveType == 6 && color == 0) { // white knight move
            board = knightMove(move, board, 'N');
        } else if (moveType == 6 && color == 1) { // black knight move
            board = knightMove(move, board, 'n');
        } else if (moveType == 7 && color == 0) { // white king-side castle
            board = kingSideCastle(board, true);
        } else if (moveType == 7 && color == 1) { // black king-side castle
            board = kingSideCastle(board, false);
        } else if (moveType == 8 && color == 0) { // white queen-side castle
            board = queenSideCastle(board, true);
        } else if (moveType == 8 && color == 1) { // black queen-side castle
            board = queenSideCastle(board, false);
        } else if (moveType == 9 && color == 0) { // white rook disambig move
            board = disamMove(move, board, 'R');
        } else if (moveType == 9 && color == 1) { // black rook disambig move
            board = disamMove(move, board, 'r');
        } else if (moveType == 10 && color == 0) { // white knight disambig move
            board = disamMove(move, board, 'N');
        } else if (moveType == 10 && color == 1) { // black knight disambig move
            board = disamMove(move, board, 'n');
        } else if (moveType == 11 && color == 0) { // white queen disambig move
            board = disamMove(move, board, 'Q');
        } else if (moveType == 11 && color == 1) { // black queen disambig move
            board = disamMove(move, board, 'q');
        } else if (moveType == 12 && color == 0) {
            board = disamMove(move, board, 'B');
        } else if (moveType == 12 && color == 1) {
            board = disamMove(move, board, 'b');
        } else {
            System.out.println("THIS SHOULDN'T HAPPEN");
            board = board;
        }
        return board;
    }

    public static boolean isUpperCase(String a) {
        return Character.isUpperCase(a.charAt(0));
    }

    public static char[][] whitePawnMove(String move, char[][] board) {
        int column = getCol(move.substring(0, 1).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(1, 2)));
        int count = 0; // so the loop exits once P is found
        for (int i = row; i < board.length; i++) {
            if (board[i][column] == 'P' && count == 0) {
                board[i][column] = ' ';
                count++;
            }
        }
        int indOfEq = move.indexOf("=");
        if (indOfEq != -1) { // checking for pawn promotion
            String tmpPiece = move.substring(indOfEq + 1, indOfEq + 2);
            board[row][column] = tmpPiece.charAt(0);
        } else {
            board[row][column] = 'P';
        }
        return board;
    }

    public static char[][] blackPawnMove(String move, char[][] board) {
        int column = getCol(move.substring(0, 1).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(1, 2)));
        int count = 0; // so the loop exits once p is found
        for (int i = row; i >= 0; i--) {
            if (board[i][column] == 'p' && count == 0) {
                board[i][column] = ' ';
                count++;
            }
        }
        int indOfEq = move.indexOf("=");
        if (indOfEq != -1) { // checking for pawn promotion
            String tmpPiece1 = move.substring(indOfEq + 1, indOfEq + 2);
            char tmpPiece = tmpPiece1.charAt(0);
            board[row][column] = Character.toLowerCase(tmpPiece);
        } else {
            board[row][column] = 'p';
        }
        return board;
    }

    public static char[][] pawnCapture(String move, char[][] board,
        boolean isWhite) {
        int startCol = getCol(move.substring(0, 1).charAt(0));
        int endCol = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        int indOfEq = move.indexOf("=");
        if (isWhite) {
            board[row + 1][startCol] = ' ';
            if (indOfEq != -1) { // check for pawn promotion
                String tmpPiece = move.substring(indOfEq + 1, indOfEq + 2);
                board[row][endCol] = tmpPiece.charAt(0);
            } else {
                if (board[row][endCol] == ' ') { // if en passant
                    board[row + 1][endCol] = ' ';
                }
                board[row][endCol] = 'P';
            }
        } else {
            board[row - 1][startCol] = ' ';
            if (indOfEq != -1) { // check for pawn promotion
                String tmpPiece1 = move.substring(indOfEq + 1, indOfEq + 2);
                char tmpPiece = tmpPiece1.charAt(0);
                board[row][endCol] = Character.toLowerCase(tmpPiece);
            } else {
                if (board[row][endCol] == ' ') { // if en passant
                    board[row - 1][endCol] = ' ';
                }
                board[row][endCol] = 'p';
            }
        }
        return board;
    }

    public static boolean canMoveInRow(int row, int fromCol, int endCol,
        char[][] board) {
        if (fromCol < endCol) {
            for (int j = fromCol + 1; j < endCol; j++) {
                if (board[row][j] != ' ') {
                    return false;
                }
            }
        } else {
            for (int j = fromCol - 1; j > endCol; j--) {
                if (board[row][j] != ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canMoveInColumn(int column, int fromRow, int endRow,
        char[][] board) {
        if (fromRow < endRow) {
            for (int i = fromRow + 1; i < endRow; i++) {
                if (board[i][column] != ' ') {
                    return false;
                }
            }
        } else {
            for (int i = fromRow - 1; i > endRow; i--) {
                if (board[i][column] != ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean canMoveInDiagonal(int fromCol, int endCol,
        int fromRow, int endRow, char[][] board) {
        int i = -1;
        int j = -1;
        if ((fromCol < endCol) && (fromRow > endRow)) {
            i = fromRow - 1;
            for (j = fromCol + 1; j < endCol; j++, i--) {
                if (board[i][j] != ' ') {
                    return false;
                }
            }
        } else if ((fromCol < endCol) && (fromRow < endRow)) {
            i = fromRow + 1;
            for (j = fromCol + 1; j < endCol; j++, i++) {
                if (board[i][j] != ' ') {
                    return false;
                }
            }
        } else if ((fromCol > endCol) && (fromRow > endRow)) {
            i = fromRow - 1;
            for (j = fromCol - 1; j > endCol; j--, i--) {
                if (board[i][j] != ' ') {
                    return false;
                }
            }
        } else {
            i = fromRow + 1;
            for (j = fromCol - 1; j > endCol; j--, i++) {
                if (board[i][j] != ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isInDiagonal(int fromCol, int endCol,
        int fromRow, int endRow) {
        int colDifference = Math.abs(endCol - fromCol);
        int rowDifference = Math.abs(endRow - fromRow);
        if (colDifference == rowDifference) {
            return true;
        }
        return false;
    }

    public static String[] possibleKnightOrigins(int endCol, int endRow) {
        String[] knightList = new String[8];
        int colM1 = endCol - 1;
        int colM2 = endCol - 2;
        int colP1 = endCol + 1;
        int colP2 = endCol + 2;
        int rowM1 = endRow - 1;
        int rowM2 = endRow - 2;
        int rowP1 = endRow + 1;
        int rowP2 = endRow + 2;
        knightList[0] = "" + colM1 + rowM2;
        knightList[1] = "" + colP1 + rowM2;
        knightList[2] = "" + colM2 + rowM1;
        knightList[3] = "" + colP2 + rowM1;
        knightList[4] = "" + colM2 + rowP1;
        knightList[5] = "" + colP2 + rowP1;
        knightList[6] = "" + colM1 + rowP2;
        knightList[7] = "" + colP1 + rowP2;
        if (colM1 < 0 || rowM2 < 0) {
            knightList[0] = null;
        }
        if (colP1 > 7 || rowM2 < 0) {
            knightList[1] = null;
        }
        if (colM2 < 0 || rowM1 < 0) {
            knightList[2] = null;
        }
        if (colP2 > 7 || rowM1 < 0) {
            knightList[3] = null;
        }
        if (colM2 < 0 || rowP1 > 7) {
            knightList[4] = null;
        }
        if (colP2 > 7 || rowP1 > 7) {
            knightList[5] = null;
        }
        if (colM1 < 0 || rowP2 > 7) {
            knightList[6] = null;
        }
        if (colP1 > 7 || rowP2 > 7) {
            knightList[7] = null;
        }
        // knightList elements have column first and row second
        // i.e. knightList[i] = "21" means column index 2 and row index 1
        return knightList;
    }

    public static char[][] knightMove(String move, char[][] board,
        char knight) {
        int column = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        int count = 0; // so the loop exits once the correct N is found
        String origCol1 = "", origRow1 = "";
        int origCol = -1, origRow = -1;
        String[] knightOrigins = possibleKnightOrigins(column, row);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == knight && count == 0) {
                    for (int x = 0; x < knightOrigins.length; x++) {
                        if (knightOrigins[x] != null) {
                            origCol1 = knightOrigins[x].substring(0, 1);
                            origCol = Integer.parseInt(origCol1);
                            origRow1 = knightOrigins[x].substring(1, 2);
                            origRow = Integer.parseInt(origRow1);
                            if (i == origRow && j == origCol) {
                                board[i][j] = ' ';
                                count++;
                            }
                        }
                    }
                }
            }
        }
        board[row][column] = knight;
        return board;
    }

    public static char[][] queenMove(String move, char[][] board, char queen) {
        int column = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        int count = 0; // so the loop exits once the correct Q is found
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == queen && row == i
                    && canMoveInRow(row, j, column, board) && count == 0) {
                    board[i][j] = ' ';
                    count++;
                } else if (board[i][j] == queen && column == j
                    && canMoveInColumn(column, i, row, board) && count == 0) {
                    board[i][j] = ' ';
                    count++;
                } else if (board[i][j] == queen
                    && isInDiagonal(j, column, i, row)
                    && canMoveInDiagonal(j, column, i, row, board)
                    && count == 0) {
                    board[i][j] = ' ';
                    count++;
                }
            }
        }
        board[row][column] = queen;
        return board;
    }

    public static char[][] bishopMove(String move, char[][] board,
        char bishop) {
        int column = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        int count = 0; // so the loop exits once the correct B is found
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == bishop && isInDiagonal(j, column, i, row)
                    && canMoveInDiagonal(j, column, i, row, board)
                    && count == 0) {
                    board[i][j] = ' ';
                    count++;
                }
            }
        }
        board[row][column] = bishop;
        return board;
    }

    public static char[][] singleBishopDisambig(int origCol, int origRow,
        int endCol, int endRow, char bishop, char[][] board) {
        int count = 0;
        if (origCol >= 0) { // check if disambiguating by file or rank
            for (int i = 0; i < board.length; i++) {
                if (board[i][origCol] == bishop
                    && isInDiagonal(origCol, endCol, i, endRow)
                    && canMoveInDiagonal(origCol, endCol, i, endRow, board)
                    && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                }
            }
        } else {
            for (int j = 0; j < board[origRow].length; j++) {
                if (board[origRow][j] == bishop
                    && isInDiagonal(j, endCol, origRow, endRow)
                    && canMoveInDiagonal(j, endCol, origRow, endRow, board)
                    && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                }
            }
        }
        board[endRow][endCol] = bishop;
        return board;
    }

    public static char[][] rookMove(String move, char[][] board, char rook) {
        int column = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        int count = 0; // so the loop exits once the correct R is found
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == rook && row == i
                    && canMoveInRow(row, j, column, board) && count == 0) {
                    board[i][j] = ' ';
                    count++;
                } else if (board[i][j] == rook && column == j
                    && canMoveInColumn(column, i, row, board) && count == 0) {
                    board[i][j] = ' ';
                    count++;
                }
            }
        }
        board[row][column] = rook;
        return board;
    }

    public static char[][] kingMove(String move, char[][] board, char king) {
        int column = getCol(move.substring(1, 2).charAt(0));
        int row = getRow(Integer.parseInt(move.substring(2, 3)));
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == king) {
                    board[i][j] = ' ';
                }
            }
        }
        board[row][column] = king;
        return board;
    }

    public static char[][] kingSideCastle(char[][] board, boolean isWhite) {
        if (isWhite) {
            board[7][4] = ' ';
            board[7][7] = ' ';
            board[7][5] = 'R';
            board[7][6] = 'K';
        } else {
            board[0][4] = ' ';
            board[0][7] = ' ';
            board[0][5] = 'r';
            board[0][6] = 'k';
        }
        return board;
    }

    public static char[][] queenSideCastle(char[][] board, boolean isWhite) {
        if (isWhite) {
            board[7][0] = ' ';
            board[7][4] = ' ';
            board[7][3] = 'R';
            board[7][2] = 'K';
        } else {
            board[0][0] = ' ';
            board[0][4] = ' ';
            board[0][3] = 'r';
            board[0][2] = 'k';
        }
        return board;
    }

    public static char[][] disamMove(String move, char[][] board,
        char piece) {
        int endColumn = getCol(move.substring(2, 3).charAt(0));
        int endRow = getRow(Integer.parseInt(move.substring(3, 4)));
        String origColumnStr = "", origRowStr = "";
        int origColumn = -1, origRow = -1;
        if (Character.isDigit(move.substring(1, 2).charAt(0))) {
            origRow = getRow(Integer.parseInt(move.substring(1, 2)));
        } else {
            origColumn = getCol(move.substring(1, 2).charAt(0));
        }

        if (Character.toUpperCase(piece) == 'R') {
            board = singleRookDisambig(origColumn, origRow, endColumn, endRow,
                piece, board);
        } else if (Character.toUpperCase(piece) == 'N') {
            board = singleKnightDisambig(origColumn, origRow, endColumn, endRow,
                piece, board);
        } else if (Character.toUpperCase(piece) == 'Q') {
            board = singleQueenDisambig(origColumn, origRow, endColumn, endRow,
                piece, board);
        } else {
            board = singleBishopDisambig(origColumn, origRow, endColumn,
                endRow, piece, board);
        }
        return board;
    }

    public static char[][] singleRookDisambig(int origCol, int origRow,
        int endCol, int endRow, char rook, char[][] board) {
        int count = 0;
        if (origCol >= 0) { // check if disambiguating by file or rank
            for (int i = 0; i < board.length; i++) {
                if (board[i][origCol] == rook && endRow == i && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                } else if (board[i][origCol] == rook
                    && endCol == origCol
                    && canMoveInColumn(origCol, i, endRow, board)
                    && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                }
            }
        } else {
            for (int j = 0; j < board[origRow].length; j++) {
                if (board[origRow][j] == rook && endCol == j && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                } else if (board[origRow][j] == rook && endRow == origRow
                    && canMoveInRow(origRow, j, endCol, board)
                    && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                }
            }
        }

        board[endRow][endCol] = rook;
        return board;
    }

    public static char[][] singleKnightDisambig(int origCol, int origRow,
        int endCol, int endRow, char knight, char[][] board) {
        int count = 0;
        String[] knightOrigins = possibleKnightOrigins(endCol, endRow);
        if (origCol >= 0) { // check if disambiguating by file or rank
            String tmpOrigRow1 = "";
            int tmpOrigRow = -1;
            for (int i = 0; i < board.length; i++) {
                if (board[i][origCol] == knight && count == 0) {
                    for (int x = 0; x < knightOrigins.length; x++) {
                        if (knightOrigins[x] != null) {
                            tmpOrigRow1 = knightOrigins[x].substring(1, 2);
                            tmpOrigRow = Integer.parseInt(tmpOrigRow1);
                            if (i == tmpOrigRow) {
                                board[i][origCol] = ' ';
                                count++;
                            }
                        }
                    }
                }
            }
        } else {
            String tmpOrigCol1 = "";
            int tmpOrigCol = -1;
            for (int j = 0; j < board[origRow].length; j++) {
                if (board[origRow][j] == knight && count == 0) {
                    for (int x = 0; x < knightOrigins.length; x++) {
                        if (knightOrigins[x] != null) {
                            tmpOrigCol1 = knightOrigins[x].substring(0, 1);
                            tmpOrigCol = Integer.parseInt(tmpOrigCol1);
                            if (j == tmpOrigCol) {
                                board[origRow][j] = ' ';
                                count++;
                            }
                        }
                    }
                }
            }
        }

        board[endRow][endCol] = knight;
        return board;
    }

    public static char[][] singleQueenDisambig(int origCol, int origRow,
        int endCol, int endRow, char queen, char[][] board) {
        int count = 0;
        if (origCol >= 0) { // check if disambiguating by file or rank
            for (int i = 0; i < board.length; i++) {
                if (board[i][origCol] == queen && endRow == i && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                } else if (board[i][origCol] == queen && endCol == origCol
                    && canMoveInColumn(endCol, i, endRow, board)
                    && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                } else if (board[i][origCol] == queen
                    && isInDiagonal(origCol, endCol, i, endRow)
                    && canMoveInDiagonal(origCol, endCol, i, endRow, board)
                    && count == 0) {
                    board[i][origCol] = ' ';
                    count++;
                }
            }
        } else {
            for (int j = 0; j < board[origRow].length; j++) {
                if (board[origRow][j] == queen && endRow == origRow
                    && canMoveInRow(endRow, j, endCol, board) && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                } else if (board[origRow][j] == queen && endCol == j
                    && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                } else if (board[origRow][j] == queen
                    && isInDiagonal(j, endCol, origRow, endRow)
                    && canMoveInDiagonal(j, endCol, origRow, endRow, board)
                    && count == 0) {
                    board[origRow][j] = ' ';
                    count++;
                }
            }
        }
        board[endRow][endCol] = queen;
        return board;
    }

    public static String getFEN(char[][] board) {
        int emptySquareCounter = 0;
        String fenString = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == ' ') {
                    emptySquareCounter++;
                } else if (emptySquareCounter > 0 && board[i][j] != ' ') {
                    fenString += emptySquareCounter;
                    emptySquareCounter = 0;
                    fenString += board[i][j];
                } else {
                    fenString += board[i][j];
                }
            }
            if (emptySquareCounter > 0) {
                fenString += emptySquareCounter;
            }
            if (i != (board.length - 1)) {
                fenString += "/";
            }
            emptySquareCounter = 0;
        }
        return fenString;
    }

    public static void printBoard(char[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String game = fileContent(args[0]);
        System.out.format("Event: %s%n", tagValue("Event", game));
        System.out.format("Site: %s%n", tagValue("Site", game));
        System.out.format("Date: %s%n", tagValue("Date", game));
        System.out.format("Round: %s%n", tagValue("Round", game));
        System.out.format("White: %s%n", tagValue("White", game));
        System.out.format("Black: %s%n", tagValue("Black", game));
        System.out.format("Result: %s%n", tagValue("Result", game));
        System.out.println("Final Position:");
        System.out.println(finalPosition(game));
        System.out.println();
    }
}