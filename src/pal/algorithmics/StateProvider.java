package pal.algorithmics;

public interface StateProvider {
    Object getStateReference();

    void restoreState(Object stateReference);
}