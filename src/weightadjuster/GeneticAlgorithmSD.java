package weightadjuster;

import java.util.Arrays;

import main.Sequence;
import main.SequenceStore;
import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmSD extends GeneticAlgorithmAdjuster {
    //private static int SAVE_INTERVAL = 1;
    private static final double DATABASE_SEQUENCE_PROBABILTY = 0.8f;
    private static final float HARD_BIAS = 0.8f; // HARD_BIAS of 1 is 100% hardest sequence. HARD_BIAS of 0 is even distribution.
    private static final int TRIES = 20;
    private Sequence[] sequences;
    
    private SequenceStore store;
    
    public GeneticAlgorithmSD(WeightedHeuristicPlayer w, int dim, int N) {
        super(w, dim, N);
        store = SequenceStore.loadTrimmed();
        sequences = new Sequence[TRIES];
        System.out.println(store);
    }
    

    @Override
    protected void selection() {
        float totalScore = 0;
        while (totalScore == 0) {

            for (int i=0; i<TRIES; ++i) {
                if (rand.nextFloat() < DATABASE_SEQUENCE_PROBABILTY) {
                    sequences[i] = store.getRandomSequence(HARD_BIAS);
                    //System.out.println("Use sequence " + sequence);
                } else {
                    sequences[i] = null;
                }
            }
            
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                
                
                float result = w.playPartialWithWeights(realWeights, TRIES, sequences);

                scores[i] = result;
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
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
    
    
}
