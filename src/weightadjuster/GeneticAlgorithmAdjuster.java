package weightadjuster;

import java.util.Arrays;
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
    private double mutationProbability = 0.1;
    private HashMap<Integer,Float> fixedValue = new HashMap<>(); 

    public static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 60f, 80f, 100f, 150f};
    public static final int WORD_SIZE = 5;
    
    public GeneticAlgorithmAdjuster(WeightedHeuristicPlayer w, int _dim, int N) {
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
    
    float[] generateRandomState(int length) {
        boolean[] encoded = new boolean[length*WORD_SIZE];
        for (int i=0; i<encoded.length; ++i) {
            encoded[i] = rand.nextBoolean();
        }
        return decode(encoded);
    }
    
    private void generateRandomStates() {
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = generateRandomState(dim);
            /*for (int j = 0; j < dim; ++j) {
                states[i][j] = rand.nextFloat();
            }*/
        }
    }
    
    private void selection() {
        float totalScore = 0;
        while (totalScore == 0) {
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                float[] result = w.playWithWeights(realWeights);
                scores[i] = result[0];
                //scores[i] *= scores[i];
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
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
                boolean temp = firstBitString[j];
                firstBitString[j] = secondBitString[j];
                secondBitString[j] = temp;
               
//                firstBitString[j] ^= secondBitString[j];
//                secondBitString[j] ^= firstBitString[j];
//                firstBitString[j] ^= secondBitString[j];
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

    public static int encodeFloat(float f) {
        boolean neg = f < 0;
        if (neg) f = -f;
        int index = 0;
        while (index+1 < conversionTable.length && f > conversionTable[index]) {
            index++;
        }
        if (neg) return conversionTable.length-index;
        else return conversionTable.length+index;
    }

    public static float decodeFloat(int v) {
        boolean neg = v < 16;
        if (neg) v = 16 - v - 1;
        else v -= 16;
        if (neg) return -conversionTable[v];
        else return conversionTable[v];
    }

    public static boolean[] encode(float[] array) {
        boolean[] encoded = new boolean[array.length*WORD_SIZE];
        for (int i=0; i<array.length; ++i) {
            int offset = i*WORD_SIZE;
            int v = encodeFloat(array[i]);
            int index = 0;
            while (v > 0) {
                encoded[offset+index] = v%2 == 1;
                v /= 2;
                index++;
            }
        }


        int index = 0;
        return encoded;
    }

    public static float[] decode(boolean[] encoded) {
        float[] decoded = new float[encoded.length/WORD_SIZE];
        for (int i=0; i<decoded.length; ++i) {
            int offset = i*WORD_SIZE;
            int v = 0;
            for (int j = WORD_SIZE-1; j>=0; --j) {
                v *= 2;
                if (encoded[offset+j]) v++;
            }
            decoded[i] = decodeFloat(v);
        }
        return decoded;
    }

    public void adjust() {
        generateRandomStates();
        int iteration = Integer.MAX_VALUE;
        int interval = 1;
        for (int i = 0; i < iteration; ++i) {
            //System.out.println("Iteration " + i);
            selection();
//            crossover();
//            mutation();
            
            if (i%interval == 0) {
                System.out.println("Iteration " + i);
                for (int j = 0; j < stateNumber; ++j) {
                    float[] realWeights = generateRealWeights(states[j]);
                    float[] result = w.playWithWeights(realWeights);
                    System.out.println("State #" + j + ". Score = " + result[0] + "   " + 
                            Arrays.toString(states[j]));
                }
            }
            
            crossover();
            mutation();
        }
    }
    
}
