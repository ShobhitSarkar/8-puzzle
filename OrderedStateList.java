package edu.iastate.cs472.proj1;

import java.awt.Taskbar.State;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  
 * @author Shobhit Sarkar
 *
 */

/**
 * This class describes a circular doubly-linked list of states to represent both the OPEN and CLOSED lists
 * used by the A* algorithm.  The states on the list are sorted in the  
 * 
 *     a) order of non-decreasing cost estimate for the state if the list is OPEN, or 
 *     b) lexicographic order of the state if the list is CLOSED.  
 * 
 */
public class OrderedStateList 
{

	/**
	 * Implementation of a circular doubly-linked list with a dummy head node.
	 */
	  private State head;           // dummy node as the head of the sorted linked list 
	  private int size = 0;
	  
	  private boolean isOPEN;       // true if this OrderedStateList object is the list OPEN and false 
	                                // if the list CLOSED.

	  /**
	   *  Default constructor constructs an empty list. Initialize heuristic. Set the fields next and 
	   *  previous of head to the node itself. Initialize instance variables size and heuristic. 
	   * 
	   * ------------------Implementation Details---------------------------------
	   * 
	   * Initialize the heuristic for all State objects 
	   * Create a dummy head node for a circular doubly linked list (previous and next pointers to itself)
	   * set isOpen flag based on the parameter given 
	   * 
	   * @param h 
	   * @param isOpen   
	   */
	  public OrderedStateList(Heuristic h, boolean isOpen)
	  {
		  State.heu = h;   // initialize heuristic used for evaluating all State objects. 

		  //creating dummy head node 
		  head = new State(new int[][]{{0,0,0}, {0,0,0}, {0,0,0}}); 
		  head.next = head; 
		  head.previous = head; 

	  }

	  /**
	   * Return the size of the list
	   * @return : size of the list 
	   */
	  public int size()
	  {
		  return size; 
	  }
	  
	  
	  /**
	   * A new state is added to the sorted list.  Traverse the list starting at head.  Stop 
	   * right before the first state t such that compareStates(s, t) <= 0, and add s before t.  
	   * If no such state exists, simply add s to the end of the list. 
	   * 
	   * Precondition: s does not appear on the sorted list. 
	   * 
	   * ------------ Implementation Details----------------------------
	   * Traverse the list ot find the correct psition to insert the new state 
	   * Use compareStates to determine the order, (different methods for OPEN and CLOSED lists) 
	   * Inser the new state by adjusting pointers of the surrounding nodes 
	   * 
	   * @param s
	   */
	  public void addState(State s)
	  {
		  State current = head.next; 

		  while (current != head && compareStates(s, current) > 0){
			current = current.next;
		  }

		  //insert s before current 
		  s.next = current; 
		  s.previous = current.previous; 
		  current.previous.next = s; 
		  current.previous = s; 

		  size++; 
	  }
	  
	  
	  /**
	   * Conduct a sequential search on the list for a state that has the same board configuration 
	   * as the argument state s.  
	   * 
	   * Calls equals() from the State class. 
	   * -------------- Implementation Details -----------------
	   * Perform sequential search through the list 
	   * Use the equals method of the state class to compare states 
	   * 
	   * @param s
	   * @return the state on the list if found
	   *         null if not found 
	   */
	  public State findState(State s)
	  {
		  State current = head.next; 
		  while (current != head){
			if (current.equals(s)){
				return current;
			}
			current = current.next; 
		  }

		  return null; 
	  }
	  
	  
	  /**
	   * Remove the argument state s from the list.  It is used by the A* algorithm in maintaining 
	   * both the OPEN and CLOSED lists. 
	   * 
	   * ------------- Implementation Details -------------------
	   * Search for the state in the list 
	   * If found, we remove it by adjusting the pointers of surrounding nodes 
	   * If not found, we through IllegalStateExeption 
	   * 
	   * @param s
	   * @throws IllegalStateException if s is not on the list 
	   */
	  public void removeState(State s) throws IllegalStateException
	  {
		  State current = head.next; 

		  while (current != head){
			if (current == s){
				current.previous.next = current.next; 
				current.next.previous = current.previous; 
				size--; 
				return;
			}
			current = current.next; 
		  }

		  throw new IllegalStateException("State not found in the list"); 
	  }
	  
	  
	  /**
	   * Remove the first state on the list and return it.  This is used by the A* algorithm in maintaining
	   * the OPEN list. 
	   * 
	   * --------------- Implementation Details--------------
	   * Remove and return the first non - dummy node in the list 
	   * Throw a NoSuchElmentException if the list is empty 
	   * 
	   * @return  
	   */
	  public State remove()
	  {
		if (isEmpty()){
			throw new NoSuchElementException("List is empty");
		}

		State first = head.next; 
		head.next = first.next; 
		first.next.previous = head; 
		size--; 

		return first;
	  }
	  
	  
	  /**
	   * Compare two states depending on whether this OrderedStateList object is the list OPEN 
	   * or the list CLOSE used by the A* algorithm.  More specifically,  
	   * 
	   *     a) call the method compareTo() of the State if isOPEN == true, or 
	   *     b) create a StateComparator object to call its compare() method if isOPEN == false. 
	   * 
	   * --------------- Implementation Details---------------------
	   * For the OPEN list, use the compareTo method of the State Class 
	   * For the CLOSED list, use the compare method of a StateComparator Object 
	   * 
	   * @param s1
	   * @param s2
	   * @return -1 if s1 is less than s2 as determined by the corresponding comparison method
	   *         0  if they are equal 
	   *         1  if s1 is greater than s2
	   */
	  private int compareStates(State s1, State s2)
	  {
		  if (isOPEN){
			return s1.compareTo(s2); 
		  } else {
			return new StateComparator().compare(s1, s2); 
		  }
	  }

	  /**
	   * Checks if the list is empty 
	   * @return : true if the list is empty, false otherwise 
	   */
	  public boolean isEmpty(){
		return size == 0; 
	  }
}
