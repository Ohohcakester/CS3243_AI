package weightadjuster;

import java.util.Arrays;

import main.Sequence;
import main.SequenceStore;
import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmSD extends GeneticAlgorithmAdjuster {
    //private static int SAVE_INTERVAL = 1;
    private static final double DATABASE_SEQUENCE_PROBABILTY = 1f;
    private static final float HARD_BIAS = 0.15f; // HARD_BIAS of 1 is 100% hardest sequence. HARD_BIAS of 0 is even distribution.
    
    private static final int PARTIAL_TRIES = 50;
    private static final int REAL_TRIES = 8;
    private int selectionIteration;
    private int realInterval = 1;
    
    private static final float WEIGHT_REAL = 0.8f; // between 0 and 1.

    private boolean readyToPrint;
    private boolean printed;

    protected float lastAverage;
    
    private Sequence[] sequences;
    
    private SequenceStore store;
    
    public GeneticAlgorithmSD(WeightedHeuristicPlayer w, int dim, int N) {
        super(w, dim, N);
        //store = SequenceStore.empty();
        store = SequenceStore.loadTrimmed();
        sequences = new Sequence[PARTIAL_TRIES];
        System.out.println(store);
        
        PRINT_INTERVAL = 1;
    }
    
    @Override
    protected void selection() {
        selectionIteration++;
        boolean playReal = (selectionIteration%realInterval == 0);
        
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
                
                if (playReal) {
                    float resultP = w.playPartialWithWeights(realWeights, PARTIAL_TRIES, sequences);
                    float resultR = w.playWithWeightsMin(realWeights, REAL_TRIES, store);
                    scores[i] = weightedMean(resultP, resultR);
                    if (resultR > 100000) {
                        //System.out.println(resultR + " attained from " + Arrays.toString(realWeights));
                    }
                    if (readyToPrint) {
                        analyseResults(i, resultR, resultP, scores[i], realWeights);
                        printed = true;
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
            
            if (playReal) {
                lastAverage = totalScore/states.length;
            }
            
            if (printed) {
                printTotalAndHighScore(totalScore);
            } else {
                System.out.println((selectionIteration%realInterval == 0) + " | " + totalScore/states.length);
            }

            readyToPrint = false;
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
            int prob = scores.length-i;
            prob = prob*prob;
            probability[i] = prob;
            totalProb += prob;
        }
        for (int i=0; i<probability.length; ++i) {
            probability[i] /= totalProb;
        }
        for (int i=0; i<ranked.length; ++i) {
            scores[ranked[i]] = probability[i];
            //System.out.println(i + " | " + ranked[i] + " | " + scores[ranked[i]] + " | " + probability[i]);
        }
        //states[ranked[ranked.length-1]] = generateRandomState(states[0].length);
        states[ranked[ranked.length-1]] = bestHeap.getRandomWeights();
        
        
        
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
    protected int mutationBits() {
        int s = (int)lastAverage;
        int bits = 12;
        while (s > 3) {
            s /= 3;
            bits--;
        }
        bits = bits*3/2;
        System.out.println("Mutation bits: " + lastAverage + " => " + Math.max(bits, MUTATION_BITS));
        return Math.max(bits, MUTATION_BITS);
    }


    @Override
    protected float playWithWeights(float[] realWeights, int times) {
        return w.playWithWeights(realWeights, times, store);
    }
    
    @Override
    public void adjust() {
        generateRandomStates();
        printed = false;
        readyToPrint = false;
        
        int iteration = Integer.MAX_VALUE;
        for (int i = 0; i < iteration; ++i) {
            if ((i+1)%PRINT_INTERVAL == 0) readyToPrint = true;
            System.out.println("Iteration " + i);
            
            selection();
            crossover();
            mutation();
            
            maybeReset(i);
            System.out.println("Staleness: " + bestHeap.consecutiveRejects + " / " + STALE_HEAP_THRESHOLD);
            
            if (printed) {
                store.saveSequences();
                printed = false;
            }
        }
    }
    
    
    
    private void analyseResults(int stateNo, float result, float resultPartial, float weightedMean, float[] realWeights) {
        System.out.printf("State #%2d. Score = %10.3f - %8.3f - %8.3f [",stateNo,result,resultPartial,weightedMean);
        for (int k = 0; k < states[stateNo].length; ++k) {
            System.out.printf("%9.2f", states[stateNo][k]);
            if (k + 1 < states[stateNo].length) {
                System.out.printf(",");
            } else {
                System.out.println("]");
            }
        }

        bestHeap.tryInsert(result, realWeights);
        /*if (result > highScore) {
            highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
            highScore = result;
        }*/
    }
    
    
}

