package weightadjuster;

import java.util.HashMap;
import java.util.Random;

import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmAdjuster {
    private static Random rand = new Random();
    private int dim;
    private int realDim;
    private float[][] states;
    private float[] scores;
    private WeightedHeuristicPlayer w;
    private int stateNumber;
    private double mutationProbability = 0.001;
    private HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    
    public GeneticAlgorithmAdjuster(int _dim, int N) {
        dim = _dim;
        realDim = dim;
        stateNumber = N;
        w = new WeightedHeuristicPlayer();
        states = new float[stateNumber][];
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
        scores = new float[stateNumber];
    }
    
    public void fixValue(int position, float value) {
        fixedValue.put(position, value);
        --dim;
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
    }
    
    float[] generateRealWeights(float[] state) {
        float[] realWeights = new float[realDim];
        int index = 0;
        for (int i = 0; i < realDim; ++i) {
            if (fixedValue.containsKey(i)) {
                realWeights[i] = fixedValue.get(i);
            } else {
                realWeights[i] = state[index++];
            }
        }
        return realWeights;
    }
    
    private void selection() {
        float totalScore = 0;
        for (int i = 0; i < states.length; ++i) {
            float[] realWeights = generateRealWeights(states[i]);
            float[] result = w.playWithWeights(realWeights);
            scores[i] = result[0];
            totalScore += scores[i];
        }
        for (int i = 0; i < states.length; ++i) {
            scores[i] /= totalScore;
        }
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
    
    private void crossover() {
        for (int i = 0; i + 1 < states.length; i += 2) {
            boolean[] firstBitString = encode(states[i]);
            boolean[] secondBitString = encode(states[i+1]);
            int position = rand.nextInt(firstBitString.length);
            for (int j = 0; j < position; ++j) {
                firstBitString[j] ^= secondBitString[j] ^= firstBitString[j] ^= secondBitString[j];
                //CP style of swapping. yeay
            }
            states[i] = decode(firstBitString);
            states[i+1] = decode(secondBitString);
        }
    }
    
    private void mutation() {
        for (int i = 0; i < states.length; ++i) {
            boolean[] bitString = encode(states[i]);
            int position = rand.nextInt(bitString.length);
            double prob = rand.nextDouble();
            if (prob < mutationProbability) {
                bitString[position] ^= true;
            }
            states[i] = decode(bitString);
        }
    }
    
    public void adjust() {
        for (int i = 0; i < stateNumber; ++i) {
            for (int j = 0; j < dim; ++j) {
                states[i][j] = rand.nextFloat();
            }
        }
        int iteration = 10000;
        for (int i = 0; i < iteration; ++i) {
            selection();
            crossover();
            mutation();
        }
        for (int j = 0; j < stateNumber; ++j) {
            float[] realWeights = generateRealWeights(states[j]);
            float[] result = w.playWithWeights(realWeights);
            System.out.println("State #" + j + ". Score = " + result[0]);
        }
    }
}
