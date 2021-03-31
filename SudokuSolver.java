import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SudokuSolver extends Application {
    
    int cellSize = 50;
    int size = 9;
    int board[][];

    int width = 600;
    int height = 500;
    
    int horOffSet = (width - (size * cellSize))/2;
    int verOffSet = (height - (size * cellSize))/2;

    Button Solve;
    Button Play;
    Button OK;
    Button Custom;
    Button Generate;

    int unsolved[][];

    boolean creatingBoard = false;
    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext g = canvas.getGraphicsContext2D();

        drawBoard(g);
        
        Play = new Button("Play");
        Play.setDisable(true);
        Custom = new Button("Custom Board");
        OK = new Button("OK");
        OK.setDisable(true);
        Generate = new Button("Generate Board");
        Solve = new Button("Solve");

        Play.setPadding(new Insets(10));
        Custom.setPadding(new Insets(10));
        OK.setPadding(new Insets(10));
        Generate.setPadding(new Insets(10));
        Solve.setPadding(new Insets(10));
        Solve.setDisable(true);

        Custom.setOnAction(e -> createCustomBoard(g));
        OK.setOnAction(e -> {
            creatingBoard = false;
            Play.setDisable(false);
            Custom.setDisable(false);
            Generate.setDisable(false);
            OK.setDisable(true); //disable ok
            Solve.setDisable(false);
            unsolved = new int[size][size];
            for(int i = 0; i < size; i++){
                for(int j = 0; j < size; j++){
                    unsolved[i][j] = board[i][j];
                }
            }
            drawBoard(g);
            drawContents(board, g);
        });
        Generate.setOnAction(e -> generateBoard(g));
        Solve.setOnAction(e -> {
            Play.setDisable(true);
            Solve.setDisable(true);
            if(solveBoard(board)){
                System.out.println("Solved!");
                drawAnswer(board, g);
            }else{
                System.out.println("No Solution!");
            };
        });

        HBox buttonGroup = new HBox(Play, Custom, Generate, OK, Solve);
        buttonGroup.setSpacing(20);
        buttonGroup.setPadding(new Insets(20));
        buttonGroup.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(buttonGroup);
        root.setBackground(new Background(new BackgroundFill(Color.AQUAMARINE, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonGroup.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Sudoku");
        stage.show();
    }

    void createCustomBoard(GraphicsContext g){
        creatingBoard = true;
        //disable buttons
        Play.setDisable(true);
        Custom.setDisable(true);
        Generate.setDisable(true);
        OK.setDisable(false); //except ok
        Solve.setDisable(true);
        drawBoard(g);
        board = new int[size][size];

        int canvasW = (int) g.getCanvas().getWidth();
        int canvasH = (int) g.getCanvas().getHeight();

        int boardLeft = horOffSet;
        int boardRight = canvasW - horOffSet;
        int boardTop = verOffSet;
        int boardBottom = canvasH - verOffSet;
        boolean selectedCell = false;
        

        g.getCanvas().setOnMouseClicked(e -> {
            if(!creatingBoard) return;
            if(e.getX() <= horOffSet || e.getY() <= verOffSet){
                System.out.println("Out of canvas");
            }else if(e.getX() >= g.getCanvas().getWidth() - horOffSet || e.getY() >= g.getCanvas().getHeight() - verOffSet){
                System.out.println("Out of canvas");
            }else if(boardLeft < e.getX() && e.getX() < boardRight && boardTop < e.getY() && e.getY() < boardBottom){
                System.out.println("Valid");
                int row = 0;
                int col = 0;
                for(int i = 0; i < size*cellSize; i+=cellSize){
                    row = 0;
                    for(int j = 0; j < size*cellSize; j+=cellSize){
                        if(e.getX() > i + horOffSet && e.getX() < i+cellSize+horOffSet){
                            if(e.getY() > j + verOffSet && e.getY() < j+cellSize+verOffSet){
                                System.out.println("That's board [" +row+ "][" +col+ "].");
                                g.setStroke(Color.RED);
                                g.setLineWidth(3);
                                g.strokeRect(col*cellSize + horOffSet, row*cellSize + verOffSet, cellSize, cellSize);
                                getInput(row, col, g);
                                
                            }
                        }
                        row++;
                    }
                    col++;
                }
            }
        });

        g.getCanvas().setOnMouseMoved(e -> {
            if(!creatingBoard) return;
            if(boardLeft < e.getX() && e.getX() < boardRight && boardTop < e.getY() && e.getY() < boardBottom){
                int row = 0;
                int col = 0;
                for(int i = 0; i < size*cellSize; i+=cellSize){
                    row = 0;
                    for(int j = 0; j < size*cellSize; j+=cellSize){
                        if(e.getX() > i + horOffSet && e.getX() < i+cellSize+horOffSet && e.getY() > j + verOffSet && e.getY() < j+cellSize+verOffSet){
                            g.setStroke(Color.RED);
                            g.setLineWidth(3);
                            g.strokeRect(col*cellSize + horOffSet, row*cellSize + verOffSet, cellSize, cellSize);
                        }else{
                            g.setStroke(Color.WHITE);
                            g.setLineWidth(3);
                            g.strokeRect(col*cellSize + horOffSet, row*cellSize + verOffSet, cellSize, cellSize);
                            g.setStroke(Color.BLACK);
                            g.setLineWidth(1);
                            g.strokeRect(col*cellSize + horOffSet, row*cellSize + verOffSet, cellSize, cellSize);
                        }
                        row++;
                    }
                    col++;
                }
            }
        });
    }

    private void getInput(int row, int col, GraphicsContext g) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        TextField in = new TextField();
        Button enter = new Button("Enter");
        enter.setDefaultButton(true);

        enter.setOnAction(e -> {
            board[row][col] = Integer.parseInt(in.getText());
            System.out.println("Input is " + in.getText() + " for board [" +row+ "][" +col+ "].");
            drawBoard(g);
            drawContents(board, g);
            dialog.close();
        });
        VBox root = new VBox(in, enter);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        Scene dialogScene = new Scene(root);
        dialog.setScene(dialogScene);
        dialog.show();
        
    }

    private void drawBoard(GraphicsContext g) {
        for(int i = 0; i < size*cellSize; i+=cellSize){
            for(int j = 0; j < size*cellSize; j+=cellSize){
                g.setFill(Color.WHITE);
                g.fillRect(i + horOffSet, j + verOffSet, cellSize, cellSize);
                if((i%3 == 0 && j%3 == 0)){
                    g.setStroke(Color.BLACK);
                    g.setLineWidth(4);
                    g.strokeRect(i + horOffSet, j+verOffSet, cellSize*3, cellSize*3);
                }else{
                    g.setStroke(Color.BLACK);
                    g.setLineWidth(1);
                    g.strokeRect(i + horOffSet, j + verOffSet, cellSize, cellSize);
                }
            }
        }
    }

    void generateBoard(GraphicsContext g){
        drawBoard(g);
        board = new int[size][size];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                int chance = (int) (Math.random() * 4);
                if(chance == 0){
                    while(true){
                        int num = (int) ((Math.random() * 8) + 1);
                        if(isSafe(num, board, i, j)){
                            board[i][j] = num;
                            break;
                        }
                    }
                }else{
                    board[i][j] = 0;
                }
            }
        }

        drawContents(board, g);

        unsolved = new int[size][size];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                unsolved[i][j] = board[i][j];
            }
        }
        Solve.setDisable(false);
        Play.setDisable(false);

    }
    private void drawContents(int[][] board2, GraphicsContext g) {
        for(int row = 0; row < size; row++){
            for(int col = 0; col < size; col++){
                if(board[row][col] != 0){
                    g.setStroke(Color.BLACK);
                    g.strokeText(Integer.toString(board[row][col]), 25+ horOffSet + (col*cellSize), 25+ verOffSet + (row*cellSize));
                }
            }
        }
    }

    private boolean isSafe(int value, int[][] board, int row, int column) {
        //check row
        for(int i = 0; i < size; i++){
            if(board[i][column] == value) return false;
        }
        //check column
        for(int j = 0; j < size; j++){
            if(board[row][j] == value) return false;
        }
        //chech submatrix
        int subRow = row - (row % 3);
        int subCol = column - (column % 3);
        for(int i = subRow; i < subRow+3; i++){
            for(int j = subCol; j < subCol + 3; j++){
                if(board[i][j] == value){
                    return false;
                }
            }
        }
        return true;
    }

    boolean solveBoard(int[][] board){
        System.out.println(".");
        int row = -1;
        int col = -1;
        int i = 0, j = 0;
        for(i=0; i < size; i++){
            for(j=0; j < size; j++){
                if(board[i][j] == 0){
                    row = i;
                    col = j;
                }
            }
            if(row != -1 && col != -1) break;
        }

        if(i == size && j == size) return true;
        else{
            for(int value = 1; value < size+1; value++){
                if(isSafe(value, board, row, col)){
                    board[row][col] = value;
                    if(!solveBoard(board)){
                        board[row][col] = 0;
                    }else{
                        return true;
                    }
                }
            }
            return false;
        }
    }

    void drawAnswer(int[][] board, GraphicsContext g){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(unsolved[i][j] == 0){
                    g.setStroke(Color.RED);
                    g.strokeText(Integer.toString(board[i][j]), 25+ horOffSet + (j*cellSize), 25+ verOffSet + (i*cellSize));
                }
            }
        }
    }

    public static void main(String[] args) {
            launch(args);
    }
}