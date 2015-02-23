
public class FeatureFunctions {

    public static float exampleFeature(State s, NextState nextState) {
        return 4;
    }

    /**
     * returns the height of the tallest "skyscraper"
     */
    public static float maximumColumnHeight(State state, NextState nextState) {
        int maximumHeight = Integer.MIN_VALUE;
        int top[] = nextState.getTop();
        for (int x:top) {
            if (x > maximumHeight) {
                maximumHeight = x;
            }
        }
        return maximumHeight;
    }
    
}
