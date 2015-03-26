package players;

import weightadjuster.SmoothingAdjuster;
import weightadjuster.WeightAdjuster;
import main.FeatureFunctions;
import main.NextState;
import main.State;

public class OhPlayer extends WeightedHeuristicPlayer {


    protected void configure() {
        features = new Feature[]{
                (n) -> FeatureFunctions.lost(n),
                (n) -> FeatureFunctions.bumpiness(n),
                (n) -> FeatureFunctions.sumHeight(n),
                (n) -> FeatureFunctions.numRowsCleared(n),
                (n) -> FeatureFunctions.maxHeightDifference(n),
                (n) -> FeatureFunctions.numHoles(n),
                (n) -> FeatureFunctions.sumHoleDistanceFromTop(n),
                (n) -> FeatureFunctions.holeAndPitColumns(n),
                (n) -> FeatureFunctions.topPerimeter(n)
                
                //(n)->FeatureFunctions.lost(n),
                /*FeatureFunctions.minimax(1, (n)-> {
                    float total = 0;
                    int height = (int)FeatureFunctions.maximumColumnHeight(n);
                    height = ((height+1) / 7) - 1;
                    //total -= 50f*height;
                    total -= FeatureFunctions.totalHoles(n);
                    total -= 10f*FeatureFunctions.bumpiness(n);
                    total -= 30f*FeatureFunctions.totalColumnsHeight(n);
                    total += 20f*FeatureFunctions.completedLines(n);
                    total -= 2f*FeatureFunctions.differenceHigh(n);
                    total -= 26f*FeatureFunctions.totalHolePieces(n);
                    total -= 0f*FeatureFunctions.totalHoles(n);
                    total -= 5f*FeatureFunctions.weightedTotalHolePieces(n);
                    total -= 50f*FeatureFunctions.holeAndPitColumns(n);
                    
                    //System.out.println(FeatureFunctions.totalHoles(s,n) - FeatureFunctions.totalHolePieces(s,n));
                    
                    return total;
                })*/
                //(n)->FeatureFunctions.maximumColumnHeight(n),
                //(n)->FeatureFunctions.totalHoles(n),
                //(n)->FeatureFunctions.totalColumnsHeight(n),
                //(n)->FeatureFunctions.bumpiness(n),
                //(n)->FeatureFunctions.completedLines(n),
                //(n)->FeatureFunctions.totalFilledCells(n),
                //(n)->FeatureFunctions.minMaximumColumnHeight(n),
                //(n)->FeatureFunctions.minMaxTotalHoles(n),
                //(n)->FeatureFunctions.differenceHigh(n)
        };
    }
    
    /**
     * Genetic Algorithm Results
     * Features: lost, bumpiness, sumHeight, numRowsCleared, maxHeightDifference,
     *           numHoles, sumHoleDistanceFromTop, holeAndPitColumns, topPerimeter
     *  Hi-Score: 1561.0 | [2.0, -5.0, -150.0, 0.1, 0.5, -2.0, -80.0, -40.0, -80.0]
     *  State #17. Score = 1236.5   [-0.02, -20.0, -150.0, -2.0, 2.0, -2.0, -10.0, -5.0, -20.0]
     *  Hi-Score: 2340.5 | [-80.0, -5.0, 0.01, 0.02, 0.05, -20.0, -5.0, -2.0, -5.0]
     *  Hi-Score: 2259.0 | [-2.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  Hi-Score: 2525.0 | [-2.0, -100.0, -2.0, -0.01, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  Hi-Score: 4496.5 | [-0.01, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  
     *  
     */
    protected void initialiseWeights() {
        weights = new float[features.length];
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -5, -30, 0};
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -15, -5, -10};
        //weights = new float[]{-99999.0f, 0, 0, 0, 0, -50, 0, -30, -50, -50};

        //weights = new float[]{-99999f, -100.0f, -2.0f, -0.01f, 5.0f, -500.0f, -40.0f, -70.0f, -3.0f};
        weights = new float[]{-99999f, -100.0f, -2.0f, -0.5f, 5.0f, -500.0f, -40.0f, -70.0f, -3.0f}; // Very good weights.
        
        //weights = new float[]{-8000.0f, 0.01f, -3.0f, -0.05f, -0.05f, -100.0f, -20.0f, -70.0f, -20.0f};
        
        //weights = new float[]{-99999.0f, -0.0f, -72.27131f, -0.39263827f, -18.150364f, 1.9908575f, -4.523054f, 2.6717715f}; // <-- good weights.
        //weights = new float[]{-99999.0f, -0.0f, -80.05821f, 0.2864133f, -16.635815f, -0.0488357f, -2.9707198f, -1f, -1f, -1f}; // <-- good weights.
        //weights = new float[]{-99999.0f, -1, -4, -95};
    }
    
    /**
     * return the min max value of feature
     */
    public static Feature minMax(Feature feature) {
        return (NextState nextState) -> {
            float worstPiece = Float.POSITIVE_INFINITY;
            for (int i = 0; i < State.N_PIECES; ++i) {
                float bestMove = Float.NEGATIVE_INFINITY;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j=0; j<legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState,i,legalMoves[j]);
                    float score = feature.compute(ns);
                    if (score > bestMove) {
                        bestMove = score;
                        if (bestMove >= worstPiece) {
                            j = legalMoves.length; // break;
                            //break;
                        }
                    }
                }
                if (bestMove < worstPiece) {
                    worstPiece = bestMove;
                }
            }
            return worstPiece;
        };
    }


    
    public static void main(String[] args) {
        int choice = 0; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new OhPlayer();
        WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        adjuster.fixValue(0, -99999f);
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
        
        //p.switchToMinimax(1);
        switch(choice) {
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                learn(p, adjuster);break;
        }
    }
}
