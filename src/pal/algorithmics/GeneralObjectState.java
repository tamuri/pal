package pal.algorithmics;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class GeneralObjectState implements ObjectState {
  private final UndoableAction action_;
  private final StateProvider subject_;
  private final boolean maximise_;
  public GeneralObjectState(UndoableAction action, StateProvider subject, boolean maximise) {
    this.action_ = action;
    this.subject_ = subject;
    this.maximise_ = maximise;
  }
  /**
   * Perform an action
   * @param currentScore the current score before doing the action
   * @param desparationValue An indication of how desparate we are, values closer to 1 mean more desparate while values towards 0 mean less desparate
   * @return the current score after doing the action
   */
  public double doAction(double currentScore, double desparationValue) {
    boolean succeeded = false;
    double score = currentScore;
    while(!succeeded) {
      score = action_.doAction(currentScore,desparationValue);
      succeeded = action_.isActionSuccessful();
    }
    return score;
  }
    /**
     *
     * @return true if undo was successful
     */
  public boolean undoAction() {
    return action_.undoAction();
  }

  public Object getStateReference() {
    return subject_.getStateReference();
  }
  public void restoreState(Object stateReference) {
    subject_.restoreState(stateReference);
  }

  public boolean isMaximiseScore() {
    return maximise_;
  }


}