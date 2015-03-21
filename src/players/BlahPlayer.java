package players;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import weightadjuster.SmoothingAdjuster;

public class BlahPlayer extends WeightedHeuristicPlayer {

    protected void configure() {
        features = new Feature[]{
                (n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.maximumColumnHeight(n),
                (n)->FeatureFunctions.totalHoles(n),
                (n)->FeatureFunctions.totalColumnsHeight(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.completedLines(n),
                (n)->FeatureFunctions.totalFilledCells(n)
                //nextStateFeatureFunction(n)->FeatureFunctions.totalFilledCells(n)),
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
        
        return (nextState) -> {
            int[] action = new int[]{0,3};
            NextState nextNextState = nextState.generate(nextState, 0, action);
            NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
            return feature.compute(nextNextNextState);
        };
    }

    private static Feature good = (nextState) -> {

        int[] action = new int[]{0,3};
        NextState nextNextState = nextState.generate(nextState, 0, action);
        NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
        return 4;
        
    };
    
    private static float good(NextState nextState) {
        
        
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
