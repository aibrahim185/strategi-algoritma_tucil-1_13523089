package com.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolverRR {
    private static int N, M, P;
    private static final char EMPTY = '_';
    private static final char PADDING = '.';
    private static char[][] board;
    private static final List<Piece> pieces = new ArrayList<>();

    private static int totalIterations = 0;

    static class Piece {
        char symbol;
        int[][] shape;

        Piece(char symbol, int[][] shape) {
            this.symbol = symbol;
            this.shape = shape;
        }

        Piece transform(int type) {
            int[][] tempShape = Arrays.stream(this.shape).map(int[]::clone).toArray(int[][]::new);
            Piece newPiece = new Piece(symbol, tempShape);

            switch (type) {
                case 1 -> {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[0];
                        coord[0] = coord[1];
                        coord[1] = -temp;
                    }
                }
                case 2 -> {
                    for (int[] coord : newPiece.shape) {
                        coord[0] *= -1;
                        coord[1] *= -1;
                    }
                }
                case 3 -> {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[1];
                        coord[1] = coord[0];
                        coord[0] = -temp;
                    }
                }
                case 4 -> {
                    for (int[] coord : newPiece.shape) {
                        coord[1] = -coord[1];
                    }
                }
                case 5 -> {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[0];
                        coord[0] = coord[1];
                        coord[1] = temp;
                    }
                }
                case 6 -> {
                    for (int[] coord : newPiece.shape) {
                        coord[0] *= -1;
                    }
                }
                case 7 -> {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[1];
                        coord[1] = -coord[0];
                        coord[0] = -temp;
                    }
                }
            }

            return newPiece;
        }
    }

    private static boolean nextTransformation(int[] trans) {
        int i = trans.length - 1;
        while (i >= 0) {
            if (trans[i] < 7) {
                trans[i]++;
                return true;
            }
            trans[i] = 0;
            i--;
        }
        return false;
    }

    private static boolean solve() {
        int[] perm = new int[P];
        
        do {
            boolean[][][] visited = new boolean[P][N][M];
            
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < M; c++) {
                    char[][] tempBoard = Arrays.stream(board).map(char[]::clone).toArray(char[][]::new);

                    totalIterations++;
                    
                    for (int i = 0; i < P; i++) {
                        for (int row = 0; row < N; row++) {
                            boolean isPlaced = false;
                            
                            for (int col = 0; col < M; col++) {
                                Piece newPiece = pieces.get(i).transform(perm[i]);
                                if (!visited[i][row][col] && !place(tempBoard, newPiece, row, col)) {
                                    removePiece(tempBoard, newPiece, row, col);
                                } else {
                                    visited[i][row][col] = true;
                                    isPlaced = true;
                                    break;
                                }
                            }

                            if (isPlaced) break;
                        }
                    }
                    
                    if (check(tempBoard)) {
                        board = tempBoard;
                        return true;
                    }
                }
            }
        } while (nextTransformation(perm));

        return false;
    }

    private static boolean place(char[][] localBoard, Piece piece, int row, int col) {
        for (int[] coord : piece.shape) {
            int r = row + coord[0];
            int c = col + coord[1];
            if (r < 0 || r >= N || c < 0 || c >= M || localBoard[r][c] != EMPTY) {
                return false;
            }
        }
        for (int[] coord : piece.shape) {
            localBoard[row + coord[0]][col + coord[1]] = piece.symbol;
        }
        return true;
    }
    
    private static void removePiece(char[][] localBoard, Piece piece, int row, int col) {
        for (int[] coord : piece.shape) {
            int r = row + coord[0];
            int c = col + coord[1];
            if (r < 0 || r >= N || c < 0 || c >= M || localBoard[r][c] == PADDING) {
                continue;
            }
            if (localBoard[r][c] == piece.symbol) {
                localBoard[r][c] = EMPTY;
            }
        }
    }
    
    private static void printBoard(char[][] localBoard) {
        String[] style = {
            "",
            "\u001B[1m",
            "\u001B[3m",
            "\u001B[1;3m", 
        };
    
        String[] backgrounds = {
            "\u001B[41m", 
            "\u001B[42m", 
            "\u001B[43m", 
            "\u001B[44m", 
            "\u001B[45m", 
            "\u001B[46m",
            "\u001B[101m", 
            "\u001B[102m", 
            "\u001B[103m", 
            "\u001B[104m", 
            "\u001B[105m", 
            "\u001B[106m", 
            "\u001B[107m"
        };
    
        String reset = "\u001B[0m";
        
        for (char[] row : localBoard) {
            for (char cell : row) {
                if (cell >= 'A' && cell <= 'Z') {
                    String color = style[(cell - 'A') / 12] + backgrounds[(cell - 'A') % 12];
                    System.out.print(color + cell + reset);
                } else {
                    System.out.print(cell);
                }
            }
            System.out.println();
        }
    }
    
    private static boolean check(char[][] localBoard) {
        for (char[] row : localBoard) {
            for (char cell : row) {
                if (cell == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean input(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String[] dimensions = br.readLine().split(" ");
            N = Integer.parseInt(dimensions[0]);
            M = Integer.parseInt(dimensions[1]);
            P = Integer.parseInt(dimensions[2]);

            board = new char[N][M];
            int totalArea = N * M;
            for (int i = 0; i < N; i++) {
                Arrays.fill(board[i], EMPTY);
            }

            String caseType = br.readLine();
            if (caseType.equals("CUSTOM")) {
                totalArea = 0;
                for (int i = 0; i < N; i++) {
                    String line = br.readLine();

                    for (int j = 0; j < M; j++) {
                        if (line.charAt(j) == 'X') {
                            board[i][j] = EMPTY;
                            totalArea++;
                        } else {
                            board[i][j] = PADDING;
                        }
                    }
                }
            }

            int area = 0;
            String line = br.readLine();
            for (int i = 0; i < P; i++) {
                if (line == null || line.isEmpty()) {
                    throw new IllegalArgumentException("Unexpected end of file while reading pieces");
                }
                
                char symbol = (char) (i + 'A');
                for (int j = 0; j < line.length(); j++) {
                    if (line.charAt(j) >= 'A' && line.charAt(j) <= 'Z') {
                        symbol = line.charAt(j);
                        break;
                    }
                }

                List<int[]> shapeList = new ArrayList<>();
                int row = 0;

                readPiece:
                do {
                    boolean isFirst = true;

                    for (int col = 0; col < line.length(); col++) {
                        if (line.charAt(col) == symbol) {
                            isFirst = false;
                            shapeList.add(new int[]{row, col});
                            area++;
                        } else if (line.charAt(col) >= 'A' && line.charAt(col) <= 'Z' && isFirst) {
                            break readPiece;
                        } else if (line.charAt(col) != ' ') {
                            return false;
                        } 
                    }
                    row++;
                } while ((line = br.readLine()) != null && !line.isEmpty());

                if (shapeList.isEmpty()) {
                    throw new IllegalArgumentException("Invalid shape data for piece " + symbol);
                }
                int[][] shape = shapeList.toArray(new int[0][]);
                pieces.add(new Piece(symbol, shape));
            }
            if (area != totalArea) {
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
        }
        
        return false;
    }
    
    public static void driver(String filePath) {
        input(filePath);
        
        long startTime = System.currentTimeMillis();
        boolean isSolved = solve();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (isSolved) {
            printBoard(board);
        } else {
            printBoard(board);
            System.out.println("Tidak ada solusi.");
        }

        System.out.println("\nWaktu pencarian: " + duration + " ms");
        System.out.println("\nBanyak kasus yang ditinjau: " + totalIterations);
        System.out.println("\nApakah anda ingin menyimpan solusi? (ya/tidak) ");
    }
}
