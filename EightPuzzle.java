package edu.iastate.cs472.proj1;

import java.awt.Taskbar.State;
import java.io.FileNotFoundException;

/**
 *  
 * @author Shobhit Sarkar
 *
 */

public class EightPuzzle 
{
	/**
	 * This static method solves an 8-puzzle with a given initial state using three heuristics. The 
	 * first two, allowing single moves only, compare the board configuration with the goal configuration 
	 * by the number of mismatched tiles, and by the Manhattan distance, respectively.  The third 
	 * heuristic, designed by yourself, allows double moves and must be also admissible.  The goal 
	 * configuration set for all puzzles is
	 * 
	 * 			1 2 3
	 * 			8   4
	 * 			7 6 5
	 * 
	 * 
	 * Check to see if the puzzle is solvable using the `s0.solvable()` code. If not, we return. 
	 * Created a StringBuilder to accumulate the solutions for all 3 heuristics 
	 * Loop through the 3 heuristics, call A star for each and append results to the solution string. 
	 * @param s0
	 * @return a string specified in the javadoc below
	 */
	public static String solve8Puzzle(State s0)
	{
		if (!s0.solvable()){
			return "No solution exists for the following initial state:\n\n" + s0.toString(); 
		}
		
		Heuristic h[] = {Heuristic.TileMismatch, Heuristic.ManhattanDist, Heuristic.DoubleMoveHeuristic }; 
		String [] moves = new String[3]; 
		
		for (int i = 0; i < 3; i++)
		{
			moves[i] = AStar(s0, h[i]); 
			solution.append(moved[i]).append("\n"); 
		}
		
		
		return solution.toString();
	}

	
	/**
	 * This method implements the A* algorithm to solve the 8-puzzle with an input initial state s0. 
	 * The algorithm implementation is described in Section 3 of the project description. 
	 * 
	 * Precondition: the puzzle is solvable with the initial state s0.
	 * 
	 * ---------------Implementation Details--------------------
	 * 
	 * Implemented the A* algorithm. 
	 * Initialize OPEN and CLOSED lists using OrderedStateList 
	 * We add the initial state to OPEN 
	 * 
	 * We then enter a loop that coninutes till OPEN is empty: 
	 * 	Remove the first state from OPEN 
	 * 	If it's goal state, we return the solution path
	 * 	Add the state to CLOSED. 
	 * 	Generate all possible successor states: 
	 * 		- if a successor is not in open or closed, add it to open 
	 * 		- if it's in open with a higher cost, remove it from open and add new version
	 * 		- if it's in closed with a higher cost, remove it from closed and add it to open 
	 * 
	 * If we exit the loop without having found a solution, then return a messsage. 
	 * 
	 * 
	 * 
	 * @param s0  initial state
	 * @param h   heuristic 
	 * @return    solution string 
	 */
	public static String AStar(State s0, Heuristic h)
	{
		// Initialize the two lists used by the algorithm. 
		OrderedStateList OPEN = new OrderedStateList(h, true); 
		OrderedStateList CLOSE = new OrderedStateList(h, false);

		OPEN.addState(s0); 

		while (!OPEN.isEmpty()){
			State s = OPEN.remove(); 

			if (s.isGoalState()){
				return solutionPath(s); 
			}

			CLOSED.addState(s); 

			for (Move move: Move.values()){
				try {
					State t = s.successorState(move);
					
					if (t == null) continue; 

					State tOpen = OPEN.findState(t); 
					State tClosed = CLOSED.findState(t); 

					if (tOpen == null && tClosed == null){
						OPEN.addState(t); 
					} else if (tOpen != null && t.cost() < tOpen.cost()){
						OPEN.removeState(tOpen); 
						OPEN.addState(t); 
					} else if (tClosed != null && t.cost() < tClosed.cost()){
						CLOSED.removeState(tClosed); 
						OPEN.addState(t); 
					} 
				} catch (IllegalArguementException e){
					// TODO: Just go to the next move
				}
			}
		}
					 
			
		
		return "No solution found"; 
						
	}
	
	
	
	/**
	 * From a goal state, follow the predecessor link to trace all the way back to the initial state. 
	 * Meanwhile, generate a string to represent board configurations in the reverse order, with 
	 * the initial configuration appearing first. Between every two consecutive configurations 
	 * is the move that causes their transition. A blank line separates a move and a configuration.  
	 * In the string, the sequence is preceded by the total number of moves and a blank line. 
	 * 
	 * See Section 6 in the projection description for an example. 
	 * 
	 * Call the toString() method of the State class. 
	 * 
	 * -------------------Implementation Details----------------
	 * Use a StringBuilder to construct solution path 
	 * We start from the goal state and follow the predecessor links back to the initial state 
	 * We insert each stae and its move at the beginning of the path string (basically reversing the order)
	 * We count the number of moves 
	 * We add the move count and heuristic information at the beginning of the string. 
	 * 
	 * @param goal
	 * @return
	 */
	private static String solutionPath(State goal)
	{
		StringBuilder path = new StringBuilder(); 
		State current = goal; 
		int moves = 0; 

		while (current.predecessor != null){
			path.insert(0, "\n\n" + current.toString() + "\n" + current.move); 
			current = current.predecessor; 
			moves ++; 
		}

		path.insert(0, current.toString()); 

		path.insert(0, moves + "moves in total (heuristic: )" + State.heu + ")\n\n"); 
		
		return path.toString(); 
	}
	
	
	
}
