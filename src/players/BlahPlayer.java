package players;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import weightadjuster.SmoothingAdjuster;

public class BlahPlayer extends WeightedHeuristicPlayer {

    protected void configure() {
        features = new Feature[]{
                (s,n)->FeatureFunctions.lost(s,n),
                (s,n)->FeatureFunctions.maximumColumnHeight(s,n),
                (s,n)->FeatureFunctions.totalHoles(s,n),
                (s,n)->FeatureFunctions.totalColumnsHeight(s,n),
                (s,n)->FeatureFunctions.bumpiness(s, n),
                (s,n)->FeatureFunctions.completedLines(s, n),
                (s,n)->FeatureFunctions.totalFilledCells(s, n)
                //nextStateFeatureFunction((s,n)->FeatureFunctions.totalFilledCells(s, n)),
                //good
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        weights[0] = -99999.0f;
        weights[1] = -1.0f;
        weights[2] = -50.0f;
        weights[3] = -30.0f;
        weights[4] = -50.0f;
        weights[5] = 50.0f;
        weights[6] = -25.0f;
    }
    
    private static Feature nextStateFeatureFunction(Feature feature) {
        
        return (state,nextState) -> {
            int[] action = new int[]{0,3};
            NextState nextNextState = nextState.generate(nextState, 0, action);
            NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
            return feature.compute(state, nextNextNextState);
        };
    }

    private static Feature good = (state, nextState) -> {

        int[] action = new int[]{0,3};
        NextState nextNextState = nextState.generate(nextState, 0, action);
        NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
        return 4;
        
    };
    
    private static float good(State state, NextState nextState) {
        
        
        int[] action = new int[]{0,3};
        NextState nextNextState = nextState.generate(nextState, 0, action);
        NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
        return 4;
    }


    public static void main(String[] args) {
        BlahPlayer p = new BlahPlayer();
        SmoothingAdjuster adj = new SmoothingAdjuster(p.dim());
        
        adj.fixValue(0, -99999f);
        learn(p, adj);
    }
    
}
