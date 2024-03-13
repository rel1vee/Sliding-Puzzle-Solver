package com.example.puzzlejfx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

class Solver {
    final int[][] startBoard;
    final int[][] goalBoard;
    HashMap<String, Boolean> visited = new HashMap<>();
    Stack<node> solution = new Stack<>();
    Queue<LinkedList<node>> queue = new LinkedList<>();
    String goalIndex;
    int steps = 0;

    Solver(int[][] startBoard, int[][] goalBoard) {
        this.startBoard = startBoard;
        this.goalBoard = goalBoard;
    }

    Stack<node> solve() {
        node puzzle = new node(startBoard, null);
        goalIndex = puzzle.indexMaker(goalBoard);
        if (puzzle.indexMaker(puzzle.board).equals(goalIndex)) {
            solution.add(puzzle);
            return solution;
        }
        visited.put(puzzle.indexMaker(puzzle.board), true);
        queue.add(puzzle.swap());

        while (queue.peek() != null) {
            for (node eachPuzzle : queue.poll()) { // {{node1, node2, node3, node4}, {node1, node2, node3}}
                if (eachPuzzle.indexMaker(eachPuzzle.board).equals(goalIndex)) {
                    solution.add(eachPuzzle);
                    node parent = eachPuzzle.parent;
                    while (parent != null) {
                        solution.add(parent);
                        parent = parent.parent;
                        steps++;
                    }
                    return solution;
                }
                queue.add(eachPuzzle.swap());
            }
        }
        return null;
    }

    public class node {
        int[][] board;
        node parent;
        LinkedList<node> childs = new LinkedList<>();

        node (int[][] board, node parent) {
            this.board = board;
            this.parent = parent;
        }

        public void print() {
            for (int[] numbers : board) {
                System.out.print("[ ");
                for (int number : numbers) {
                    System.out.print(number + " ");
                }
                System.out.println("]");
            }
        }

        LinkedList<node> swap() {
            int length = board.length;
            int[] zero = zeroFinder();
            int row = zero[0], column = zero[1];
            int[][] move = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            // child new board
            for (int i = 0; i < 4; i++) {
                if (row + move[i][0] >= 0 && row + move[i][0] < length && column + move[i][1] >= 0 && column + move[i][1] < length) {
                    int[][] newBoard = copyBoard(this.board);
                    int temp = newBoard[row + move[i][0]][column + move[i][1]];
                    newBoard[row + move[i][0]][column + move[i][1]] = 0;
                    newBoard[row][column] = temp;
                    String nodeIndex = indexMaker(newBoard);
                    if (nodeIndex.equals(goalIndex)) {
                        this.childs.clear();
                        this.childs.add(new node(newBoard, this));
                        return this.childs;
                    }
                    else if (visited.get(nodeIndex) == null) {
                        visited.put(nodeIndex,true);
                        this.childs.add(new node(newBoard, this));
                    }
                }
            }
            return this.childs;
        }

        int[] zeroFinder() {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == 0) return new int[]{i,j};
                }
            }
            return null;
        }

        String indexMaker(int[][] board) {
            StringBuilder accumulator = new StringBuilder();
            for (int[] ints : board) {
                for (int j = 0; j < board.length; j++) {
                    accumulator.append(ints[j]).append(" ");
                }
            }
            return accumulator.toString();
        }

        int[][] copyBoard(int[][] src) {
            int[][] result = new int[src.length][];
            for (int i = 0; i < src.length; i++) {
                result[i] = Arrays.copyOf(src[i], src[i].length);
            }
            return result;
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

        Solver slidingPuzzle = new Solver(startBoard, goalBoard);
        Stack<Solver.node> solution = slidingPuzzle.solve();

        if (solution != null) {
            while (!solution.empty()) {
                solution.pop().print();
                System.out.println();
            }
        }
        System.out.println(slidingPuzzle.visited.size() == 181440 ? "Unsolved\nIterations : 181440/181440" : "Solved\nIterations  : " + slidingPuzzle.visited.size() + "/181440\nTotal Steps : " + slidingPuzzle.steps);
    }
}