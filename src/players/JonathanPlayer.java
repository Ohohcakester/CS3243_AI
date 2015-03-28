package players;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import weightadjuster.GeneticAlgorithmAdjuster;

public class JonathanPlayer extends WeightedHeuristicPlayer {


    protected void configure() {
        features = new Feature[]{
                        (n)->FeatureFunctions.lost(n),
                        (n)->FeatureFunctions.maxHeight(n),
                        (n)->FeatureFunctions.numHoles(n),
                        (n)->FeatureFunctions.sumHeight(n),
                        (n)->FeatureFunctions.bumpiness(n),
                        (n)->FeatureFunctions.numRowsCleared(n),
                        (n)->FeatureFunctions.numFilledCells(n),
                        (n)->FeatureFunctions.maxHeightDifference(n),
                        (n)->FeatureFunctions.sumHoleDistanceFromTop(n),
                        (n)->FeatureFunctions.sumEmptyCellDistanceFromTop(n),
                        (n)->FeatureFunctions.holeAndPitColumns(n),
                        (n)->FeatureFunctions.topPerimeter(n),
                        (n)->FeatureFunctions.maxHeightCube(n),
                        (n)->FeatureFunctions.holeAndPitColumnsMin(n),
                        (n)->FeatureFunctions.numRowsThatHasMoreThanOneHole(n),
                        (n)->FeatureFunctions.holeCoverEmptyCells(n)/*,
                        FeatureFunctions.variableHeightMinimaxInt(
                                (h) -> State.ROWS-h,
                                FeatureFunctions.negHeightRegion(1)
                                )*/
                        //FeatureFunctions.minimaxInt(2, FeatureFunctions.negHeightRegion(10)),
                        //FeatureFunctions.minimaxInt(2, FeatureFunctions.negHeightRegion(7))
                        //FeatureFunctions.minimaxInt(2, FeatureFunctions.lost())
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -5, -30, 0};
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -15, -5, -10};
        //weights = new float[]{-99999.0f, 299.2644f, -241.19064f, -269.57117f, -271.10962f, -175.32292f, 149.07938f, -269.22433f, -184.39595f, -274.9451f, -233.19809f, -293.61053f};
        weights = new float[]{-99999.0f, -0.0f, -27.0f, -512.0f, -27.0f, 64.0f, -125.0f, -343.0f, -2197.0f, -27.0f, -512.0f, -3375.0f, -1.0f};
        
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
        GeneticAlgorithmAdjuster adjuster = new GeneticAlgorithmAdjuster(p, p.dim(), 20);
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
        switch(choice) {
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                learn(adjuster);break;
        }
    }
}
