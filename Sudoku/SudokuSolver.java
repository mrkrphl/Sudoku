import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

//* This program runs a Sudoku Game that allows you to generate random boards or to create your own custom game to solve.//
public class SudokuSolver extends Application {
    
    int cellSize = 50; //size of each cell that contains the number
    int size = 9;//size of matrix
    

    int width = 600;
    int height = 500;
    
    int horOffSet = (width - (size * cellSize))/2; //to center the board on the canvas
    int verOffSet = (height - (size * cellSize))/2;

    Button Solve;
    Button Play;
    Button OK;
    Button Custom;
    Button Generate;

    int board[][]; //current board
    int unsolved[][];//board to solve
    int answers[][];//answers to board

    boolean creatingBoard = false; //is true when creating a custom board
    boolean playing = false;//is true when player is currently solving board
    boolean solved = false;

    int keyRow = 9;
    int keyCol = 0;

    GraphicsContext g;
    
    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(width, height);
        g = canvas.getGraphicsContext2D();

        drawBoard();
        
        Play = new Button("Play");
        Custom = new Button("Custom Board");
        OK = new Button("OK");
        Generate = new Button("Generate Board");
        Solve = new Button("Solve");

        //disable when no board is currently set
        Play.setDisable(true);
        OK.setDisable(true);
        Solve.setDisable(true);

        //padding
        Play.setPadding(new Insets(10));
        Custom.setPadding(new Insets(10));
        OK.setPadding(new Insets(10));
        Generate.setPadding(new Insets(10));
        Solve.setPadding(new Insets(10));
        
        //on Action
        Play.setOnAction(a -> play(a));
        OK.setOnAction(e -> okPressed(e));
        Generate.setOnAction(e -> generateBoard(e));

        Solve.setOnAction(e -> {
            Play.setDisable(true);
            Solve.setDisable(true);
            solveBoard(board);
        });

        HBox buttonGroup = new HBox(Play, Custom, Generate, OK, Solve);
        buttonGroup.setSpacing(20);
        buttonGroup.setPadding(new Insets(20));
        buttonGroup.setAlignment(Pos.CENTER);

        Custom.setOnAction(e -> createCustomBoard(buttonGroup));

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(buttonGroup);
        root.setBackground(new Background(new BackgroundFill(Color.AQUAMARINE, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonGroup.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root);

        g.getCanvas().setOnMouseClicked(e -> mouseClicked(e));
        g.getCanvas().setOnMouseMoved(e -> mouseMoved(e, buttonGroup)); 

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
            keyPressed(key, buttonGroup);
        });

        stage.setScene(scene);
        stage.setTitle("Sudoku");
        stage.show();
    }

    private void okPressed(ActionEvent e) {
        if(playing){
            if(!checkBoard()){
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Game not Finished!");
                alert.setContentText("You haven't solved the board! Are you sure you want to quit?");
                Optional<ButtonType> result = alert.showAndWait();
                System.out.println(result.get());
                if(result.get() == ButtonType.OK){
                    playing = false;
                }else{
                    keyRow--;
                    g.setStroke(Color.RED);
                    g.setLineWidth(3);
                    g.strokeRect(keyCol*cellSize + horOffSet, keyRow*cellSize + verOffSet, cellSize, cellSize);
                    return;
                }

            }else{
                Alert congrats = new Alert(AlertType.INFORMATION);
                congrats.setTitle("Congratulations!");
                congrats.setHeaderText("You Completed the Puzzle!");
                congrats.setContentText("Good Game!");
                congrats.showAndWait();
            }
        }
        playing = false;
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
        drawBoard();
        drawContents();
    }

    private boolean checkBoard() {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(board[i][j] == 0) return false;
            }
        }
        return true;
    }

    private void play(ActionEvent a) {
        playing = true;
        OK.setDisable(false);
        Custom.setDisable(true);
        Play.setDisable(true);
        Solve.setDisable(true);
        Generate.setDisable(true);
        answers = new int[size][size];

    }

    private void keyPressed(KeyEvent e, Node node) {
        if(!creatingBoard && !playing) return;
        
        if(keyRow == size) node.setDisable(false);
        else node.setDisable(true);
        KeyCode code = e.getCode();
        if(code == KeyCode.ENTER && keyRow < size){
            OK.setDisable(true);
            getInput(keyRow, keyCol);
        }
        
        if(code == KeyCode.UP && keyRow > 0) keyRow-=1;
        else if(code == KeyCode.UP && keyRow == 0) keyRow = 8;
        else if(code == KeyCode.DOWN && keyRow < size) keyRow+=1;
        else if(code == KeyCode.DOWN && keyRow == size) keyRow = 0;
        else if(code == KeyCode.LEFT && keyCol > 0) keyCol-=1;
        else if(code == KeyCode.LEFT && keyCol == 0) keyCol = 8;
        else if(code == KeyCode.RIGHT && keyCol < size-1) keyCol+=1;
        else if(code == KeyCode.RIGHT && keyCol == size-1) keyCol=0;
        System.out.println(keyRow);

        if(keyRow == size){
            drawBoard();
            drawContents();
            if(playing){
                drawAnswers();
            }
            node.setDisable(false);
        }
        else node.setDisable(true);

        if(keyRow < size){
            drawBoard();
            drawContents();
            if(playing){
                drawAnswers();
            }
            g.setStroke(Color.RED);
            g.setLineWidth(3);
            g.strokeRect(keyCol*cellSize + horOffSet, keyRow*cellSize + verOffSet, cellSize, cellSize);
        }
    }

    void createCustomBoard(Node node){
        solved = false;
        if(node.isDisabled()) node.setDisable(false);
        creatingBoard = true;
        //disable buttons
        Play.setDisable(true);
        Custom.setDisable(true);
        Generate.setDisable(true);
        OK.setDisable(false); //except ok
        Solve.setDisable(true);
        drawBoard();
        board = new int[size][size];
    }

    private void mouseClicked(MouseEvent e) {
        if(!creatingBoard && !playing) return;
        int canvasW = (int) g.getCanvas().getWidth();
        int canvasH = (int) g.getCanvas().getHeight();
        int boardLeft = horOffSet;
        int boardRight = canvasW - horOffSet;
        int boardTop = verOffSet;
        int boardBottom = canvasH - verOffSet;
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
                                getInput(row, col);
                                
                            }
                        }
                        row++;
                    }
                    col++;
                }
            }
    }
    private void mouseMoved(MouseEvent e, Node node) {
        if(node.isDisabled())node.setDisable(false);
        if(!creatingBoard && !playing) return;
        int canvasW = (int) g.getCanvas().getWidth();
        int canvasH = (int) g.getCanvas().getHeight();
        int boardLeft = horOffSet;
        int boardRight = canvasW - horOffSet;
        int boardTop = verOffSet;
        int boardBottom = canvasH - verOffSet;
            if(boardLeft < e.getX() && e.getX() < boardRight && boardTop < e.getY() && e.getY() < boardBottom){
                int row = 0;
                int col = 0;
                for(int i = 0; i < size*cellSize; i+=cellSize){
                    row = 0;
                    for(int j = 0; j < size*cellSize; j+=cellSize){
                        if(e.getX() > i + horOffSet && e.getX() < i+cellSize+horOffSet && e.getY() > j + verOffSet && e.getY() < j+cellSize+verOffSet){
                            keyRow = row;
                            keyCol = col;
                            drawBoard();
                            drawContents();
                            if(playing) drawAnswers();
                            g.setStroke(Color.RED);
                            g.setLineWidth(3);
                            g.strokeRect(col*cellSize + horOffSet, row*cellSize + verOffSet, cellSize, cellSize);
                        }else{
                            
                        }
                        row++;
                    }
                    col++;
                }
            }
    }
    private void getInput(int row, int col) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        TextField in = new TextField();
        Button enter = new Button("Enter");
        enter.setDefaultButton(true);

        enter.setOnAction(e -> {
            if(in.getText().isEmpty()){
                dialog.close();
                OK.setDisable(false);
                return;
            }
            if(!isSafe(Integer.parseInt(in.getText()), board, row, col) && Integer.parseInt(in.getText()) != 0){
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(in.getText() + " is doubled!");
                alert.showAndWait();
                in.clear();
                OK.setDisable(false);
                dialog.close();
                return;
            }
            if(playing){
                try{
                    answers[row][col] = Integer.parseInt(in.getText());
                }catch(Exception error){
                    error.printStackTrace();
                }
            }
            try{
                board[row][col] = Integer.parseInt(in.getText());
            }catch(Exception error){
                error.printStackTrace();
            }
            System.out.println("Input is " + in.getText() + " for board [" +row+ "][" +col+ "].");
            drawBoard();
            drawContents();
            if(playing){
                drawAnswers();
            }
            OK.setDisable(false);
            dialog.close();
        });
        VBox root = new VBox(in, enter);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        Scene dialogScene = new Scene(root);
        dialog.setScene(dialogScene);
        dialog.show();
        
    }
    private void drawAnswers() {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(answers[i][j] != 0){
                    g.setStroke(Color.RED);
                    g.strokeText(Integer.toString(answers[i][j]), 25+ horOffSet + (j*cellSize), 25+ verOffSet + (i*cellSize));
                }
            }
        }
    }

    private void drawBoard() {
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

    void generateBoard(ActionEvent e){
        solved = false;
        drawBoard();
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

        drawContents();

        unsolved = new int[size][size];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                unsolved[i][j] = board[i][j];
            }
        }
        Solve.setDisable(false);
        Play.setDisable(false);

    }
    private void drawContents() {
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
        //check column
        for(int i = 0; i < size; i++){
            if(board[i][column] == value) return false;
        }
        //check row
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

    void solveBoard(int[][] board){
        
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

        if(i == size && j == size){
            solved = true;
        }
        if(solved){
            drawBoard();
            drawContents();
            drawAnswer();
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Game Solved!");
            alert.setContentText("There might be more solutions! Wanna check?");
            Optional<ButtonType> result = alert.showAndWait();
            System.out.println(result.get());
            if(result.get() == ButtonType.OK){
                solved = false;
            }
            return;
        }
        else{
            for(int value = 1; value < size+1; value++){
                if(isSafe(value, board, row, col)){
                    board[row][col] = value;
                    solveBoard(board);
                    if(!solved) board[row][col] = 0;
                    else if(solved) System.out.println("Congrats");
                }
            }
            return;
        }
    }

    void drawAnswer(){
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