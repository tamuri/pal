// ObjectState.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;

/**
 * <p>Title: Object State</p>
 * <p>Description: A stateful, single thread object, that can act upon itself</p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface ObjectState {
  /**
   * Perform an action
   * @param currentScore The current score before doing the action
   * @param desparationValue An indication by the processing machines of willingness to do more extreme actions. A value of 0 means not desparate at all, a value of 1 means very desparate
   * @return the current score after doing the action (or the input score if not successful)
   */
  public double doAction(double currentScore, double desparationValue);
  /**
   * Undo the previous action if possible
   * @return true if undo was successful, false otherwise
   */
   /**
   * Undo the last action (if it was successful)
   * Users of an ObjectState should accept that sometimes undoing an action isn't possible.
   * If an undo was not possible the object state should be in the same state as it was previous to the call to undoAction()
   * @return true if undo was successful
   */
  public boolean undoAction();

  /**
   *
   * @return An object that can be used to reconstruct the current state of this object
   */
  public Object getStateReference();
  /**
   * Used to restore the state of the this object to that of a previous time point
   * @param stateReference An object returned by getStateReference()
   */
  public void restoreState(Object stateReference);

  /**
   * If true, than a bigger score is better, otherwise a smaller score is better
   * @return True if the aim is to maximise
   */
  public boolean isMaximiseScore();
}