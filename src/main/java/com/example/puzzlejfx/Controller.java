package com.example.puzzlejfx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import java.lang.*;
import java.util.*;

public class Controller {
    private Solver bfsSolver;
    private AStar aStarSolver;
    private Button emptyButton;

    @FXML
    TextArea solutionArea;

    @FXML
    private GridPane startState;

    @FXML
    private void initialize() {
        emptyButton = (Button) startState.getChildren().get(8); // 8 is the index of the empty button
        initializeSolvers();
    }

    private void initializeSolvers() {
        int[][] targetPuzzle = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
        int[][] currentPuzzleState = getCurrentPuzzleState();

        bfsSolver = new Solver(currentPuzzleState, targetPuzzle);
        aStarSolver = new AStar(currentPuzzleState, targetPuzzle);
    }

    @FXML
    private void handleButtonClick(ActionEvent event) {
        final Button clickedButton = (Button) event.getSource();

        if ((Math.abs(GridPane.getColumnIndex(emptyButton) - GridPane.getColumnIndex(clickedButton)) == 1 && Objects.equals(GridPane.getRowIndex(emptyButton), GridPane.getRowIndex(clickedButton))) || (Math.abs(GridPane.getRowIndex(emptyButton) - GridPane.getRowIndex(clickedButton)) == 1 && Objects.equals(GridPane.getColumnIndex(emptyButton), GridPane.getColumnIndex(clickedButton)))) {
            String tempText = emptyButton.getText();

            emptyButton.setText(clickedButton.getText());
            clickedButton.setText(tempText);

            String tempStyle = emptyButton.getStyle();
            emptyButton.setStyle(clickedButton.getStyle());
            clickedButton.setStyle(tempStyle);

            // Update emptyButton reference
            emptyButton = clickedButton;

            // Update the current puzzle state in Solver
            initializeSolvers();

            // Check if the puzzle is solved
            if (isPuzzleSolved()) {
                showSolvedPopup();
            }
        }
    }

    @FXML
    private void handleShuffle(ActionEvent event) {
        shufflePuzzle();
        // Update the current puzzle state in Solver
        initializeSolvers();
    }

    @FXML
    private void handleHintBFS(ActionEvent event) {
        Thread solverThread = new Thread(() -> {
            Stack<Solver.node> result = bfsSolver.solve();
            int totalIterations = bfsSolver.visited.size();
            int steps = bfsSolver.steps;

            displayHint(result, totalIterations, steps);

        });
        solverThread.start();
    }


    private void displayHint(Stack<Solver.node> solution, int totalIterations, int steps) {
        StringBuilder sb = new StringBuilder();

        if(solution == null) {
            displayNoSolutionFound();
            return;
        }

        sb.append("Found in ").append(steps).append(" steps.\n\n");
        //int step = 1;
        while(!solution.isEmpty()) {
            Solver.node puzzle = solution.pop();

            /*sb.append("Step ").append(step++).append(":\n");*/
            sb.append(getBoardString(puzzle.board)).append("\n");
        }

        sb.append("Iterations: ").append(totalIterations).append("/181440");

        Platform.runLater(() -> solutionArea.setText(sb.toString()));
    }

    private String getBoardString(int[][] board) {
        if (board == null) {
            return null;
        }

        StringBuilder boardString = new StringBuilder();
        for (int[] row : board) {
            if (row != null) {
                boardString.append("[ ");
                for (int num : row) {
                    boardString.append(num).append(" ");
                }
                boardString.append("]").append("\n");
            } else {
                // Handle the case where a row is null
                boardString.append("null\n");
            }
        }
        return boardString.toString();
    }

    @FXML
    private void handleHintAStar(ActionEvent event) {
        Thread solverThread = new Thread(() -> {
            AStar.Node solution = aStarSolver.solve();
            int totalIterations = aStarSolver.closed.size();

            if(solution == null) {
                displayNoSolutionFound();
                return;
            }

            displayHintAStar(solution, totalIterations);

        });
        solverThread.start();
    }

    private void displayHintAStar(AStar.Node solution, int totalIterations) {
        StringBuilder sb = new StringBuilder();
        int steps = solution.cost;

        List<AStar.Node> path = new ArrayList<>();
        while (solution != null) {
            path.add(solution);
            solution = solution.parent;
        }

        sb.append("Found in ").append(steps).append(" steps.\n\n");
        for (int i = path.size() - 1; i > 0; i--) {
            AStar.Node currentNode = path.get(i);
            sb.append(getBoardString(currentNode.board));

            AStar.Node nextNode = path.get(i - 1);
            int movedNumber = AStar.getMovedNumber(currentNode.board, nextNode.board);
            sb.append("Swipe : ").append(movedNumber).append("\n\n");
        }
        sb.append("Iterations: ").append(totalIterations).append("/181440");
        Platform.runLater(() -> solutionArea.setText(sb.toString()));
    }

    private void displayNoSolutionFound() {
        Platform.runLater(() -> {
            solutionArea.clear();
            solutionArea.setText("No Solution Found!\nIterations: 181440/181440");
        });
    }

    private void shufflePuzzle() {
        // Collect all puzzle buttons into a list
        List<Button> puzzleButtons = new ArrayList<>();
        for (int i = 0; i < startState.getChildren().size(); i++) {
            Button button = (Button) startState.getChildren().get(i);
            puzzleButtons.add(button);
        }

        // Shuffle the buttons
        Collections.shuffle(puzzleButtons);

        // Update the grid with shuffled buttons
        startState.getChildren().clear();
        for (int i = 0; i < puzzleButtons.size(); i++) {
            Button button = puzzleButtons.get(i);
            GridPane.setColumnIndex(button, i % 3);
            GridPane.setRowIndex(button, i / 3);
            startState.getChildren().add(button);
        }
    }

    @FXML
    private boolean isPuzzleSolved() {
        int[][] targetPuzzle = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button button = getNodeByRowColumnIndex(row, col);

                // Check if the button's text is equal to the target puzzle
                assert button != null;
                if (!button.getText().equals(String.valueOf(targetPuzzle[row][col]))) {
                    return false; // Puzzle is not solved
                }
            }
        }
        return true; // Puzzle is solved
    }

    // Helper method to get a button by its row and column index in the GridPane
    private Button getNodeByRowColumnIndex(final int row, final int column) {
        for (Node node : startState.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column && node instanceof Button) {
                return (Button) node;
            }
        }
        return null;
    }

    private void showSolvedPopup() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Solved!");
        alert.setHeaderText(null);
        alert.setContentText("Congratulations! You solved the puzzle.");

        // Mendapatkan dialog pane dari alert
        DialogPane dialogPane = alert.getDialogPane();

        // Mengatur gaya sesuai dengan file CSS
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        dialogPane.getStyleClass().add("alert");

        // Menampilkan dan menunggu sampai alert ditutup
        alert.showAndWait();
    }

    private int[][] getCurrentPuzzleState() {
        int[][] startBoard = new int[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button button = getNodeByRowColumnIndex(row, col);
                assert button != null;
                startBoard[row][col] = Integer.parseInt(button.getText());
            }
        }
        return startBoard;
    }
}