package com.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Solver {
    
    @FXML
    private ComboBox<String> CaseField;
    @FXML
    private TextField NField;
    @FXML
    private TextField MField;
    @FXML
    private TextField PField;
    @FXML 
    private Label ErrorField;
    @FXML
    private ImageView Image; 
    @FXML
    private TextArea PiecesField;
    @FXML
    private TextArea CustomField;
    @FXML
    private Button SolveButton;
    
    FileChooser fc = new FileChooser();

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
                case 1: {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[0];
                        coord[0] = coord[1];
                        coord[1] = -temp;
                    }
                    break;
                }
                case 2: {
                    for (int[] coord : newPiece.shape) {
                        coord[0] *= -1;
                        coord[1] *= -1;
                    }
                    break;
                }
                case 3: {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[1];
                        coord[1] = coord[0];
                        coord[0] = -temp;
                    }
                    break;
                }
                case 4: {
                    for (int[] coord : newPiece.shape) {
                        coord[1] = -coord[1];
                    }
                    break;
                }
                case 5: {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[0];
                        coord[0] = coord[1];
                        coord[1] = temp;
                    }
                    break;
                }
                case 6: {
                    for (int[] coord : newPiece.shape) {
                        coord[0] *= -1;
                    }
                    break;
                }
                case 7: {
                    for (int[] coord : newPiece.shape) {
                        int temp = coord[1];
                        coord[1] = -coord[0];
                        coord[0] = -temp;
                    }
                    break;
                }
            }

            return newPiece;
        }
    }
    
    private boolean parse() {
        try {
            board = new char[N][M];
            int totalArea = N * M;
            for (int i = 0; i < N; i++) {
                Arrays.fill(board[i], EMPTY);
            }

            String caseType = CaseField.getValue();
            if (caseType.equals("CUSTOM")) {
                totalArea = 0;
                String[] customBoard = CustomField.getText().split("\n");
                for (int i = 0; i < N; i++) {
                    String line = customBoard[i];

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

            
            String[] piecesData = PiecesField.getText().split("\n");
            List<int[]> shapeList = new ArrayList<>();
            int area = 0;
            int idx = 0;
            for (int i = 0; i < P; i++) {
                String line = piecesData[idx];

                char symbol = (char) (i + 'A');
                for (int j = 0; j < line.length(); j++) {
                    if (line.charAt(j) >= 'A' && line.charAt(j) <= 'Z') {
                        symbol = line.charAt(j);
                        break;
                    }
                }
                
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
                            ErrorField.setText("Karakter tidak valid.");
                            return false;
                        } 
                    }
                    row++;
                } while (idx++ < piecesData.length);

                int[][] shape = shapeList.toArray(new int[0][]);
                pieces.add(new Piece(symbol, shape));
            }

            if (area != totalArea) {
                ErrorField.setText("Jumlah area tidak sesuai.");
                return false;
            }

            return true;
        } catch (Exception e) {
            ErrorField.setText("Error: " + e.getMessage());
        }
        return false;
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
    
    @FXML 
    private void driver() {
        N = Integer.parseInt(NField.getText());
        M = Integer.parseInt(MField.getText());
        P = Integer.parseInt(PField.getText());

        if (parse()) {
        }
    }

    @FXML
    private void inputFile() {
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File inputFile = fc.showOpenDialog(new Stage());
        if (inputFile != null) {
            try (Scanner sc = new Scanner(inputFile)) {
                if (sc.hasNextLine()) {
                    String[] dimensions = sc.nextLine().split(" ");
                    NField.setText(dimensions[0]);
                    MField.setText(dimensions[1]);
                    PField.setText(dimensions[2]);
                }

                if (sc.hasNextLine()) {
                    String caseType = sc.nextLine();
                    if (caseType.equals("CUSTOM")) {
                        StringBuilder customBoard = new StringBuilder();
                        for (int i = 0; i < Integer.parseInt(NField.getText()); i++) {
                            String line = sc.nextLine();
                            customBoard.append(line).append("\n");
                            for (char c : line.toCharArray()) {
                                if (c != 'X' && c != '.') {
                                    System.out.print(c);
                                    ErrorField.setText("Karakter tidak valid.");
                                    return;
                                }
                            }
                        }
                        CaseField.setValue("CUSTOM");
                        CustomField.setText(customBoard.toString());
                    }
                }
                
                StringBuilder inputData = new StringBuilder();
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    inputData.append(line).append("\n");
                    for (char c : line.toCharArray()) {
                        if ((c < 'A' || c > 'Z') && c != ' ') {
                            ErrorField.setText("Karakter tidak valid: " + c);
                            return;
                        }
                    }
                }
                PiecesField.setText(inputData.toString());
            } catch (IOException e) {
                ErrorField.setText("Gagal membaca file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void initialize() {
        ObservableList<String> caseOptions = FXCollections.observableArrayList(
            "DEFAULT", "CUSTOM"
        );
        CaseField.setItems(caseOptions);
        CaseField.setValue("DEFAULT");
    
        CustomField.setVisible(false);
    
        CaseField.setOnAction(e -> {
            String selectedCase = CaseField.getValue();
            
            boolean isCUSTOM = "CUSTOM".equals(selectedCase);
            CustomField.setVisible(isCUSTOM);
            
            System.out.println("Case dipilih: " + selectedCase);
        });
    }
}
