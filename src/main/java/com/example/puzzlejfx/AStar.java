package com.example.puzzlejfx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

class AStar {
    static class Node {
        int[][] board;
        Node parent;
        int cost;
        int heuristic;

        Node(int[][] board, Node parent) {
            this.board = board;
            this.parent = parent;
            this.cost = 0;
            if (parent != null) {
                this.cost = parent.cost + 1;
            }
            this.heuristic = calculateManhattanDistance(board);
        }
    }

    PriorityQueue<Node> open;
    Set<String> closed;
    int[][] startBoard;
    int[][] goalBoard;

    AStar(int[][] start, int[][] goal) {
        this.startBoard = start;
        this.goalBoard = goal;
        this.open = new PriorityQueue<>(Comparator.comparingInt(a -> a.cost + a.heuristic));
        this.closed = new HashSet<>();
    }

    static int calculateManhattanDistance(int[][] board) {
        int distance = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) continue; // empty tile
                int num = board[i][j];
                int goalRow = (num-1) / board[0].length;
                int goalCol = (num-1) % board[0].length;
                int rowDelta = Math.abs(goalRow - i);
                int colDelta = Math.abs(goalCol - j);
                distance += (rowDelta + colDelta);
            }
        }
        return distance;
    }

    AStar.Node solve() {
        open.add(new Node(startBoard, null));

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (isGoal(current.board)) {
                return current;
            }

            closed.add(boardToString(current.board));

            for (Node child : getChildren(current)) {
                if (!closed.contains(boardToString(child.board))) {
                    open.add(child);
                }
            }
        }
        return null; // no solution
    }

    List<Node> getChildren(Node current) {
        List<Node> children = new ArrayList<>();

        int rowEmpty = -1;
        int colEmpty = -1;

        // find empty tile location
        for (int i = 0; i < current.board.length; i++) {
            for (int j = 0; j < current.board[0].length; j++) {
                if (current.board[i][j] == 0) {
                    rowEmpty = i;
                    colEmpty = j;
                    break;
                }
            }
        }

        // get possible swap locations
        int[][] moves = {{1,0}, {0,1}, {-1,0}, {0,-1}};
        for (int[] move : moves) {
            int rowTarget = rowEmpty + move[0];
            int colTarget = colEmpty + move[1];

            if (rowTarget >= 0 && rowTarget < current.board.length && colTarget >= 0 && colTarget < current.board[0].length) {
                int[][] newBoard = copyBoard(current.board);
                swap(newBoard, rowEmpty, colEmpty, rowTarget, colTarget);
                Node child = new Node(newBoard, current);
                children.add(child);
            }
        }
        return children;
    }

    boolean isGoal(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != goalBoard[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    int[][] copyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[0].length);
        }
        return copy;
    }

    void swap(int[][] board, int row1, int col1, int row2, int col2) {
        int temp = board[row1][col1];
        board[row1][col1] = board[row2][col2];
        board[row2][col2] = temp;
    }

    String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] ints : board) {
            for (int j = 0; j < board[0].length; j++) {
                sb.append(ints[j]);
            }
        }
        return sb.toString();
    }

    static void printSolutionPath(AStar.Node node) {
        List<AStar.Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }

        for (int i = path.size() - 1; i >= 0; i--) {
            printBoard(path.get(i).board);
            if (i > 0) {
                System.out.println("Swipe : " + getMovedNumber(path.get(i - 1).board, path.get(i).board) + "\n");
            }
        }
    }

    static int getMovedNumber(int[][] before, int[][] after) {
        int movedNumber = -1;
        int emptyRowBefore = -1, emptyColBefore = -1;
        int emptyRowAfter = -1, emptyColAfter = -1;

        // Find the positions of the empty space in the before and after states
        outerloop:
        for (int i = 0; i < before.length; i++) {
            for (int j = 0; j < before[0].length; j++) {
                if (before[i][j] == 0) {
                    emptyRowBefore = i;
                    emptyColBefore = j;
                    break outerloop;
                }
            }
        }

        outerloop:
        for (int i = 0; i < after.length; i++) {
            for (int j = 0; j < after[0].length; j++) {
                if (after[i][j] == 0) {
                    emptyRowAfter = i;
                    emptyColAfter = j;
                    break outerloop;
                }
            }
        }

        // Find the moved number
        if (emptyRowBefore != emptyRowAfter || emptyColBefore != emptyColAfter) {
            movedNumber = before[emptyRowAfter][emptyColAfter];
        }

        return movedNumber;
    }

    static void printBoard(int[][] board) {
        for (int[] row : board) {
            System.out.print("[ ");
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) {
        int[][] startBoard = {
                {8, 1, 3},
                {7, 5, 2},
                {4, 6, 0}
        };

        int[][] goalBoard = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 0}
        };

        AStar solver = new AStar(startBoard, goalBoard);
        AStar.Node solution = solver.solve();

        if (solution != null) {
            System.out.println("Solution found in " + solution.cost + " steps.");
            printSolutionPath(solution);
            System.out.println("Solved!\n\nTotal Iterations: " + solver.closed.size() + "/181440");
        } else {
            System.out.println("No solution found.");
        }
    }
}