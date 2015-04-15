package players;

import main.FeatureFunctions;
import main.FeatureFunctions2;
import main.NextState;
import main.State;
import weightadjuster.GeneticAlgorithmSD;

public class JonathanPlayer extends WeightedHeuristicPlayer {


    protected void configure() {
        features = new Feature[]{
                        (n)->FeatureFunctions.lost(n),
                        (n)->FeatureFunctions.numFilledCells(n),
                        (n)->FeatureFunctions2.weightedFilledCells(n),
                        (n)->FeatureFunctions.maxHeight(n),
                        (n)->FeatureFunctions.numEmptyCells(n),
                        (n)->FeatureFunctions2.clearLines(n),
                        (n)->FeatureFunctions.maxHeightDifference(n),
                        (n)->FeatureFunctions2.deepestOneHole(n),
                        (n)->FeatureFunctions2.sumOfAllHoles(n),
                        (n)->FeatureFunctions2.horizontalRoughness(n),
                        (n)->FeatureFunctions2.verticalRoughness(n),
                        (n)->FeatureFunctions2.wellCount(n),
                        (n)->FeatureFunctions2.weightedEmptyCells(n),
                        (n)->FeatureFunctions2.highestHole(n),
                        (n)->FeatureFunctions2.surfaceSmoothness(n),
                        (n)->FeatureFunctions2.sumSquareWells(n),
                        (n)->FeatureFunctions2.totalHeightNewPiece(n),
                        (n)->FeatureFunctions2.landingHeight(n),
                        
                        (n)->FeatureFunctions.bumpiness(n),
                        (n)->FeatureFunctions.topPerimeter(n),
                        (n)->FeatureFunctions.holeAndPitColumns(n),
                        (n)->FeatureFunctions.holeAndPitColumnsMin(n),
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -5, -30, 0};
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -15, -5, -10};
        //weights = new float[]{-99999.0f, 299.2644f, -241.19064f, -269.57117f, -271.10962f, -175.32292f, 149.07938f, -269.22433f, -184.39595f, -274.9451f, -233.19809f, -293.61053f};
        //weights = new float[]{-9999999.0f, -40.0f, -500.0f, -0.01f, -40.0f, 20.0f, 1.0f, 0.01f, -20.0f, 3.0f, -500.0f, -40.0f, 0.05f, -40.0f, -100.0f, 0f};
        
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
        int choice = 1; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new JonathanPlayer();
        //WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        GeneticAlgorithmSD adjuster = new GeneticAlgorithmSD(p, p.dim(), 20);
        adjuster.fixValue(0, -9999999f);
        /*adjuster.fixSign(0,-1);
        adjuster.fixSign(1,-1);
        adjuster.fixSign(2,-1);
        //adjuster.fixSign(3,-1);
        adjuster.fixSign(4,+1);
        adjuster.fixSign(5,-1);
        adjuster.fixSign(6,-1);
        adjuster.fixSign(7,-1);
        adjuster.fixSign(8,-1);
        //adjuster.fixSign(9,-1);
        //adjuster.fixSign(10,-1);
        adjuster.fixSign(11,-1);*/
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
        
        //p.configure();
        //p.switchToMinimax(2);
        int[] sequence = new int[]{6, 2, 3, 2, 6, 3, 6, 3, 3, 3, 4, 1, 6, 2, 3, 2, 5, 0, 6, 4, 3, 5, 2, 5, 2, 4, 3, 6, 5, 0, 4, 2, 2, 5, 0, 0, 4, 6, 0, 0, 6, 6, 6, 4, 6, 4, 4, 3, 3, 0, 5, 3, 6, 0, 3, 2, 4, 1, 4, 3, 2, 6, 1, 5, 3, 2, 2, 2, 6, 6, 4, 5, 6, 1, 4, 4, 4, 0, 6, 6, 1, 1, 6, 1, 5, 3, 5, 0, 3, 0, 5, 6, 2, 4, 4, 0, 5, 0, 2, 6, 0, 6, 2, 2, 0, 1, 3, 0, 3, 1, 1, 5, 5, 5, 5, 2};
        switch(choice) {
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                learn(adjuster);break;
            case 2:
                record(p); break;
            case 3:
                watchWithPredeterminedState(p,sequence); break;
        }
    }
}
