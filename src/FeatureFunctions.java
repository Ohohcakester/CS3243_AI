
public class FeatureFunctions {

    public static float exampleFeature(State s, NextState nextState) {
        return 4;
    }

    /**
     * returns the height of the tallest "skyscrapers"
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
    
    /**
     * returns the total height of all "skyscrapers"
     */
    public static float totalColumnsHeight(State state, NextState nextState) {
        int totalHeight = 0;
        int top[] = nextState.getTop();
        for (int x:top) {
            totalHeight += x;
        }
        return totalHeight;
    }

    
    /**
     * returns the total height of all "skyscrapers"
     */
    public static float lost(State state, NextState nextState) {
        return nextState.hasLost() ? 1: 0;
    }
    
}
