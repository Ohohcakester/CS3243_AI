package players;

import main.FeatureFunctions;
import main.FeatureFunctions2;
import main.NextState;
import main.State;
import weightadjuster.GeneticAlgorithmAdjuster;
import weightadjuster.GeneticAlgorithmSD;

/**
 * Oh's Player
 * choose features in configure()
 * choose weights in initialiseWeights()
 */
public class OhPlayer extends WeightedHeuristicPlayer {

    /**
     * Choose features here
     */
    protected void configure() {
        features = new Feature[]{
                /*(n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.sumHeight(n), //
                // (n)->FeatureFunctions.numRowsCleared(n), //
                //(n)->FeatureFunctions.maxHeightDifference(n), //
                (n)->FeatureFunctions.numHoles(n),
                (n)->FeatureFunctions.sumHoleDistanceFromTop(n),
                (n)->FeatureFunctions.holeAndPitColumns(n),
                (n)->FeatureFunctions.holeAndPitColumnsMin(n),
                (n)->FeatureFunctions.topPerimeter(n),
                //(n)->FeatureFunctions.maxHeight(n),     //
                //(n)->FeatureFunctions.maxHeightPow(n, 3), //
                (n)->FeatureFunctions.sumEmptyCellDistanceFromTop(n),
                //(n)->FeatureFunctions.numFilledCells(n), //
                //(n)->FeatureFunctions.numRowsWithMoreThanOneEmptyCell(n), ////
                //(n)->FeatureFunctions.holeCoverEmptyCells(n), ////
                (n)->FeatureFunctions2.weightedFilledCells(n), //////
                (n)->FeatureFunctions2.deepestOneHole(n),
                (n)->FeatureFunctions2.sumOfAllHoles(n),
                (n)->FeatureFunctions2.horizontalRoughness(n),
                (n)->FeatureFunctions2.verticalRoughness(n),
                (n)->FeatureFunctions2.wellCount(n),
                (n)->FeatureFunctions2.weightedEmptyCells(n),
                (n)->FeatureFunctions2.highestHole(n),
                (n)->FeatureFunctions2.surfaceSmoothness(n),
                //(n)->FeatureFunctions2.totalHeightNewPiece(n), // messes up sequence database
                (n)->FeatureFunctions2.sumSquareWells(n),
                (n)->FeatureFunctions2.landingHeight(n)*/
                

                (n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions2.tetrominoHeight(n),
                //(n)->FeatureFunctions2.clearLines(n),
                (n)->FeatureFunctions.numRowsCleared(n),
                (n)->FeatureFunctions2.rowTransitions(n),
                (n)->FeatureFunctions2.columnTransitions(n),
                (n)->FeatureFunctions.numEmptyCells(n),
                (n)->FeatureFunctions2.sumWellDepths(n),
                (n)->FeatureFunctions2.highestHole(n)
                
        };
    }
    
    /**
     * Set weights here
     */
    protected void initialiseWeights() {
        weights = new float[features.length];

        // Our best set of weights (average 5mil)
        weights = new float[]{-99999999f,-1224f, -133f, -424f, -2026f, -832f, -663f, 0};
        
        // Another set of relatively good weights (average 5mil) using the same set of feature functions
        //weights = new float[]{-99999999, -1037, 573, -924, -1954, -2040, -1298, -288};
        
        // A set of good weights (average 100k) using the commeted out set of feature functions above.
        //weights = new float[]{-9999999f, -1835f, 1886f, 1881f, -1353f, -1668f, -1782f, -693f, 1304f, -77f, 873f, -1623f, -1990f, -805f, 752f, 901f, -1421f, 632f, -7f, -10f};
    };
    
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
        int choice = -2; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new OhPlayer();
        //System.out.println(Arrays.toString(p.weights));
        //WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
       
        //p.switchToMinimax(1);
        switch(choice) {
            case -2:
                checkAverageScore(p, 5);break;
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                GeneticAlgorithmAdjuster adjuster = new GeneticAlgorithmSD(p, p.dim(), 20);
                adjuster.fixValue(0, -9999999f);
                learn(adjuster);break;
                //learn(p, adjuster);break;
            case 2:
                record(p);break;
            case 3:
                int[] pState = new int[0];
                System.out.println(pState.length);
                watchWithPredeterminedState(p, pState);break;
        }
    }
}
