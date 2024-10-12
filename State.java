package edu.iastate.cs472.proj1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class State implements Cloneable, Comparable<State>
{
    public int[][] board;         // configuration of tiles 
    public State previous;        // previous node on the OPEN/CLOSED list
    public State next;            // next node on the OPEN/CLOSED list
    public State predecessor;     // predecessor node on the path from the initial state 
    public Move move;             // the move that generated this state from its predecessor
    public int numMoves;          // number of moves from the initial state to this state

    public static Heuristic heu;  // heuristic used. shared by all the states. 

    private int numMismatchedTiles = -1;   // number of mismatched tiles between this state 
                                           // and the goal state; negative if not computed yet.
    private int ManhattanDistance = -1;    // Manhattan distance between this state and the 
                                           // goal state; negative if not computed yet. 
    private int numSingleDoubleMoves = -1; // number of single and double moves with each double 
                                           // move counted as one; negative if not computed yet. 

    /**
     * Constructor for the initial state.
     */
    public State(int[][] board) throws IllegalArgumentException 
    {
        if (board.length != 3 || board[0].length != 3) {
            throw new IllegalArgumentException("Board must be 3x3");
        }
        
        this.board = new int[3][3];
        boolean[] used = new boolean[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] < 0 || board[i][j] > 8 || used[board[i][j]]) {
                    throw new IllegalArgumentException("Invalid board configuration");
                }
                this.board[i][j] = board[i][j];
                used[board[i][j]] = true;
            }
        }
        
        this.previous = null;
        this.next = null;
        this.predecessor = null;
        this.move = null;
        this.numMoves = 0;
    }

    /**
     * Constructor for the initial state from a file.
     */
    public State(String inputFileName) throws FileNotFoundException, IllegalArgumentException
    {
        this.board = new int[3][3];
        Scanner scanner = new Scanner(new File(inputFileName));
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!scanner.hasNextInt()) {
                    throw new IllegalArgumentException("Invalid file format");
                }
                this.board[i][j] = scanner.nextInt();
            }
        }
        
        scanner.close();
        
        if (!isValidBoard()) {
            throw new IllegalArgumentException("Invalid board configuration");
        }
        
        this.previous = null;
        this.next = null;
        this.predecessor = null;
        this.move = null;
        this.numMoves = 0;
    }

    /**
     * Generate the successor state resulting from a given move.
     */
    public State successorState(Move m) throws IllegalArgumentException 
    {
        int[] emptyPos = findEmptyTile();
        int row = emptyPos[0], col = emptyPos[1];
        
        State successor = (State) this.clone();
        successor.predecessor = this;
        successor.move = m;
        successor.numMoves = this.numMoves + 1;
        
        switch (m) {
            case LEFT:
                if (col == 2) throw new IllegalArgumentException("Cannot move left");
                successor.board[row][col] = successor.board[row][col + 1];
                successor.board[row][col + 1] = 0;
                break;
            case RIGHT:
                if (col == 0) throw new IllegalArgumentException("Cannot move right");
                successor.board[row][col] = successor.board[row][col - 1];
                successor.board[row][col - 1] = 0;
                break;
            case UP:
                if (row == 2) throw new IllegalArgumentException("Cannot move up");
                successor.board[row][col] = successor.board[row + 1][col];
                successor.board[row + 1][col] = 0;
                break;
            case DOWN:
                if (row == 0) throw new IllegalArgumentException("Cannot move down");
                successor.board[row][col] = successor.board[row - 1][col];
                successor.board[row - 1][col] = 0;
                break;
            // Implement DBL_LEFT, DBL_RIGHT, DBL_UP, DBL_DOWN similarly
        }
        
        return successor;
    }
    /* Implements the inversion count algorithm to determine if the puzzle is solvable */
    public boolean solvable()
    {
        int inversions = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 9; j++) {
                if (board[i / 3][i % 3] > board[j / 3][j % 3] && board[i / 3][i % 3] != 0 && board[j / 3][j % 3] != 0) {
                    inversions++;
                }
            }
        }
        return inversions % 2 == 0;
    }
    /* Checks if the current state matches the goal state */
    public boolean isGoalState()
    {
        int[][] goal = {{1, 2, 3}, {8, 0, 4}, {7, 6, 5}};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] != goal[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /* Provides string representation fo the board (empty tiles are a space) */
    @Override 
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    sb.append("  ");
                } else {
                    sb.append(board[i][j]).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
    /* Creates a deep copy of the state  */
    @Override
    public Object clone()
    {
        try {
            State cloned = (State) super.clone();
            cloned.board = new int[3][3];
            for (int i = 0; i < 3; i++) {
                System.arraycopy(this.board[i], 0, cloned.board[i], 0, 3);
            }
            cloned.previous = null;
            cloned.next = null;
            cloned.predecessor = null;
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    /* Compares two states based on configurations */
    @Override 
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (this.board[i][j] != state.board[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
    /* Calculates the cost of the state based on the chosen heuristic */
    public int cost() throws IllegalArgumentException
    {
        switch (heu) {
            case TileMismatch:
                return numMoves + computeNumMismatchedTiles();
            case ManhattanDist:
                return numMoves + computeManhattanDistance();
            case DoubleMoveHeuristic:
                return numMoves + computeNumSingleDoubleMoves();
            default:
                throw new IllegalArgumentException("Invalid heuristic");
        }
    }

    /* Compares two states based on their costs */
    @Override
    public int compareTo(State s)
    {
        return Integer.compare(this.cost(), s.cost());
    }

    /* ------------ Heuristic methods -----------------  */

    /* Count the number of tiles not in their goal positions */
    private int computeNumMismatchedTiles()
    {
        if (numMismatchedTiles < 0) {
            numMismatchedTiles = 0;
            int[][] goal = {{1, 2, 3}, {8, 0, 4}, {7, 6, 5}};
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] != 0 && board[i][j] != goal[i][j]) {
                        numMismatchedTiles++;
                    }
                }
            }
        }
        return numMismatchedTiles;
    }

    /* Calculates the sum of the Manhattan distances of each tile from its goal position */
    private int computeManhattanDistance()
    {
        if (ManhattanDistance < 0) {
            ManhattanDistance = 0;
            int[][] goal = {{1, 2, 3}, {8, 0, 4}, {7, 6, 5}};
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] != 0) {
                        int[] pos = findPositionInGoal(board[i][j], goal);
                        ManhattanDistance += Math.abs(i - pos[0]) + Math.abs(j - pos[1]);
                    }
                }
            }
        }
        return ManhattanDistance;
    }

    private int computeNumSingleDoubleMoves()
{
    if (numSingleDoubleMoves < 0) {
        int[][] goal = {{1, 2, 3}, {8, 0, 4}, {7, 6, 5}};
        numSingleDoubleMoves = 0;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] != 0) {
                    int[] goalPos = findPositionInGoal(board[i][j], goal);
                    int rowDiff = Math.abs(i - goalPos[0]);
                    int colDiff = Math.abs(j - goalPos[1]);
                    
                    numSingleDoubleMoves += (rowDiff + colDiff + 1) / 2;
                }
            }
        }
    }
    return numSingleDoubleMoves;
}

    /* Locates the empty tile on the board */
    private int[] findEmptyTile() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalStateException("No empty tile found");
    }

    /* Finds the goal position of a given tile */
    private int[] findPositionInGoal(int tile, int[][] goal) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (goal[i][j] == tile) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("Tile not found in goal state");
    }

    /* Checks if the board configuration is valid */
    private boolean isValidBoard() {
        boolean[] used = new boolean[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] < 0 || board[i][j] > 8 || used[board[i][j]]) {
                    return false;
                }
                used[board[i][j]] = true;
            }
        }
        return true;
    }
}