package com.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
    
    // private void parse() {
    //     if (selectedFile != null) {
    //         BufferedImage img;
    //         try {
    //             img = ImageIO.read(selectedFile);

    //             double w = Double.parseDouble(widthField.getText());
    //             double h = Double.parseDouble(heightField.getText());

    //             // BufferedImage resizedImage = ImageProcessing.imageProcessing(img, w, h);
                
    //             File output = new File("test/image/hasil/hasil_" + selectedFile.getName());
    //             output.getParentFile().mkdirs();
    //             // ImageIO.write(resizedImage, "jpg", output); 
    //             Image resultImage = new Image(output.toURI().toString());
    //             // afterImage.setImage(resultImage);
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         } catch (NumberFormatException e) {
    //             System.out.println("Input tidak valid untuk lebar/tinggi.");
    //         }
    //     }
    // }

    @FXML 
    private void driver() {
        N = Integer.parseInt(NField.getText());
        M = Integer.parseInt(MField.getText());
        P = Integer.parseInt(PField.getText());
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
                                    // return;
                                }
                            }
                        }
                        CaseField.setValue("CUSTOM");
                        CustomField.setText(customBoard.toString());
                        // CustomField.setVisible(true);
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
