package weightadjuster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmAdjuster2 {
    private static Random rand = new Random();
    private int dim;
    private int realDim;
    private float[][] states;
    private float[] scores;
    private WeightedHeuristicPlayer w;
    private int stateNumber;
    //private double mutationProbability = 0.1;
    private double mutationProbability = 0.1;
    private HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    private HashMap<Integer,Float> fixedSign = new HashMap<>();
    
    private float[] highScoreWeights;
    private float highScore;

    private static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    private static final int WORD_SIZE = 5;
    
    public GeneticAlgorithmAdjuster2(WeightedHeuristicPlayer w, int _dim, int N) {
        dim = _dim;
        realDim = dim;
        stateNumber = N;
        this.w = w;
        states = new float[stateNumber][];
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
        scores = new float[stateNumber];
    }
    
    private float randomWeight(int position) {
        float res = (float)Math.pow(rand.nextInt(16), 5) / 100;
        if (fixedSign.containsKey(position)) {
            res = res * fixedSign.get(position);
        } else if (rand.nextDouble() < 0.5) {
            res = res * -1.0f;
        }
        return res;
    }
    
    public void fixValue(int position, float value) {
        fixedValue.put(position, value);
        --dim;
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
    }
    
    public void fixSign(int position, float value) {
        fixedSign.put(position,value);
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
    
    private void generateRandomStates() {
        for (int i = 0; i < stateNumber; ++i) {
            for (int j = 0; j < dim; ++j) {
                states[i][j] = randomWeight(j);
            }
        }
    }
    
    private void selection() {
        float totalScore = 0;
        while (totalScore == 0) {
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                float result = w.playWithWeights(realWeights, 6);
                scores[i] = result;
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
        }
        
        boolean[] chosen = new boolean[states.length];
        for (int i = 0; i < states.length; ++i) {
            chosen[i] = false;
        }
        float[][] newStates = new float[states.length][];
        
        for (int i = 0; i < states.length; ++i) {
            if (i >= states.length / 2) {
                newStates[i] = newStates[i-states.length / 2].clone();
                continue;
            }
            int maxIndex = -1;
            for (int j = 0; j < states.length; ++j) {
                if (!chosen[j] && (maxIndex == -1 || scores[j] > scores[maxIndex])) {
                    maxIndex = j;
                }
            }
            chosen[maxIndex] = true;
            newStates[i] = states[maxIndex].clone();
        }
        
        states = newStates;
    }
    
    private void crossover() {
        for (int i = states.length / 2; i + 1 < states.length; i += 2) {
            int position = rand.nextInt(states[i].length);
            for (int j = 0; j < position; ++j) {
                float temp = states[i][j];
                states[i][j] = states[i+1][j];
                states[i+1][j] = temp;
            }
        }
    }
    
    private void mutation() {
        for (int i = states.length / 2; i < states.length; ++i) {
            int position = rand.nextInt(states[i].length);
            double prob = rand.nextDouble();
            if (prob < mutationProbability) {
                System.out.println("MUTATION!");
                states[i][position] = randomWeight(position);
            }
        }
    }
    
    public void adjust() {
        generateRandomStates();
        int iteration = Integer.MAX_VALUE;
        int interval = 1;
        for (int i = 0; i < iteration; ++i) {
            //System.out.println("Iteration " + i);
            selection();
            crossover();
            mutation();
            
            if (i%interval == 0) {
                System.out.println("Iteration " + i);
                float total = 0;
                for (int j = 0; j < stateNumber; ++j) {
                    float[] realWeights = generateRealWeights(states[j]);
                    float result = w.playWithWeights(realWeights, 2);
                    System.out.println(
                            //bitString(encode(states[j])) + "    " +
                            "State #" + j + ". Score = " + result + "   " +
                            Arrays.toString(states[j]));
                            //Arrays.toString(states[j]));
                    total += result;
                    
                    if (result > highScore) {
                        highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
                        highScore = result;
                    }
                    
                }
                System.out.println("Average Score: " + (total/stateNumber));
                System.out.println("Hi-Score: " + highScore + " | " + Arrays.toString(highScoreWeights));
            }
        }
    }
    
}
