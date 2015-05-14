package pal.algorithmics;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface StateProvider {
  public Object getStateReference();

  public void restoreState(Object stateReference);
}