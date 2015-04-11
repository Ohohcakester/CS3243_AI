package weightadjuster;

import java.util.Arrays;

import main.Sequence;
import main.SequenceStore;
import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmSD extends GeneticAlgorithmAdjuster {
    //private static int SAVE_INTERVAL = 1;
    private static final double DATABASE_SEQUENCE_PROBABILTY = 1f;
    private static final float HARD_BIAS = 0.2f; // HARD_BIAS of 1 is 100% hardest sequence. HARD_BIAS of 0 is even distribution.
    
    private static final int PARTIAL_TRIES = 16;
    private static final int REAL_TRIES = 4;
    private int selectionIteration;
    private int realInterval = 1;
    
    private static final float WEIGHT_REAL = 0.8f; // between 0 and 1.
    
    
    private Sequence[] sequences;
    
    private SequenceStore store;
    
    public GeneticAlgorithmSD(WeightedHeuristicPlayer w, int dim, int N) {
        super(w, dim, N);
        //store = SequenceStore.empty();
        store = SequenceStore.loadTrimmed();
        sequences = new Sequence[PARTIAL_TRIES];
        System.out.println(store);
        
        PRINT_INTERVAL = 4;
    }
    

    @Override
    protected void selection() {
        selectionIteration++;
        float totalScore = 0;
        while (totalScore == 0) {

            for (int i=0; i<PARTIAL_TRIES; ++i) {
                if (rand.nextFloat() <= DATABASE_SEQUENCE_PROBABILTY) {
                    sequences[i] = store.getRandomSequence(HARD_BIAS);
                } else {
                    sequences[i] = null;
                }
            }
            
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                
                if (selectionIteration%realInterval == 0) {
                    float resultP = w.playPartialWithWeights(realWeights, PARTIAL_TRIES, sequences);
                    float resultR = w.playWithWeights(realWeights, REAL_TRIES);
                    scores[i] = weightedMean(resultP, resultR);
                    if (resultR > 100000) {
                        System.out.println(resultR + " attained from " + Arrays.toString(realWeights));
                    }
                } else {
                    float resultP = w.playPartialWithWeights(realWeights, PARTIAL_TRIES, sequences);
                    scores[i] = resultP;
                }
                //System.out.println(resultR + " | " + resultP + " | " + scores[i]);
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
            System.out.println((selectionIteration%realInterval == 0) + " | " + totalScore/states.length);
        }
        
        Integer[] ranked = new Integer[scores.length];
        for (int i=0; i<ranked.length; ++i) ranked[i] = i;
        Arrays.sort(ranked, (a, b) -> {
            float cmp = scores[b] - scores[a];
            if (cmp < 0) return -1;
            else if (cmp == 0) return 0;
            else return 1;
        });
        float[] probability = new float[scores.length];
        int totalProb = 0;
        for (int i=0; i<probability.length; ++i) {
            probability[i] = scores.length-i;
            totalProb += scores.length-i;
        }
        for (int i=0; i<probability.length; ++i) {
            probability[i] /= totalProb;
        }
        for (int i=0; i<ranked.length; ++i) {
            scores[ranked[i]] = probability[i];
            //System.out.println(i + " | " + ranked[i] + " | " + scores[ranked[i]] + " | " + probability[i]);
        }
        states[ranked[ranked.length-1]] = generateRandomState(states[0].length);
        
        
        
        /*for (int i = 0; i < states.length; ++i) {
            scores[i] /= totalScore;
        }*/
        float[][] newStates = new float[stateNumber][];
        for (int i = 0; i < states.length; ++i) {
            double random = rand.nextDouble();
            for (int j = 0; j < states.length; ++j) {
                random -= scores[j];
                if (random < 0) {
                    newStates[i] = states[j];
                    break;
                }
            }
        }
        states = newStates;
    }
    
    /**
     * Both resultP and resultR must be nonnegative!!!
     */
    private float weightedMean(float resultP, float resultR) {
        
        // Note: both must be nonnegative!
        
        double p = Math.pow(resultP, 1 - WEIGHT_REAL);
        double r = Math.pow(resultR, WEIGHT_REAL);

        // Currently: Geometric Mean
        return (float)(p*r);
        
        
        /*// Harmonic Mean
        float sum = resultP + resultR;
        float product = resultP*resultR;
        
        if (sum == 0)
            return 0;
        return product/sum;*/
    }


    @Override
    protected float playWithWeights(float[] realWeights, int times) {
        return w.playWithWeights(realWeights, times, store);
    }
    
    @Override
    protected void maybeSave(int iteration) {
        if ((iteration+1)%PRINT_INTERVAL == 0) {
            store.saveSequences();
        }
    }
    

    @Override
    protected float printAndReturnResult(int j, float[] realWeights) {
        float result = playWithWeights(realWeights, 2);
        float resultPartial = w.playPartialWithWeights(realWeights, 10);
        float weightedMean = weightedMean(resultPartial, result);

        System.out.printf("State #%2d. Score = %10.3f - %8.3f - %8.3f [",j,result,resultPartial,weightedMean);
        for (int k = 0; k < states[j].length; ++k) {
            System.out.printf("%9.2f", states[j][k]);
            if (k + 1 < states[j].length) {
                System.out.printf(",");
            } else {
                System.out.println("]");
            }
        }
        return result;
    }
    
    
}
