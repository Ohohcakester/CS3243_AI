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
    //private double mutationProbability = 0.1;
    private double mutationProbability = 0.1;
    private HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    
    private float[] highScoreWeights;
    private float highScore;

    private static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    private static final int WORD_SIZE = 5;
    
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
    
    private float[] generateRandomState(int length) {
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
                float result = w.playWithWeights(realWeights, 6);
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
    
    private void crossover() {
        for (int i = 0; i + 1 < states.length; i += 2) {
            boolean[] firstBitString = encode(states[i]);
            boolean[] secondBitString = encode(states[i+1]);
            int position = rand.nextInt(firstBitString.length);
            int position2 = rand.nextInt(firstBitString.length);
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
                System.out.println("MUTATION!");
                bitString[position] ^= true;
            }
            states[i] = decode(bitString);
        }
    }

    public static boolean[] binToGray(boolean[] bin) {
        boolean[] newBin = Arrays.copyOf(bin, bin.length);
        int words = newBin.length/WORD_SIZE;
        for (int i=words-1; i>=0; --i) {
            int offset = i*WORD_SIZE;
            for (int j=0; j<WORD_SIZE-1; ++j) {
                newBin[offset+j] = newBin[offset+j] ^ newBin[offset+j+1];
            }
        }
        return newBin;
    }
    
    public static boolean[] grayToBin(boolean[] bin) {
        boolean[] newBin = Arrays.copyOf(bin, bin.length);
        int words = newBin.length/WORD_SIZE;
        for (int i=words-1; i>=0; --i) {
            int offset = i*WORD_SIZE;
            for (int j=WORD_SIZE-2; j>=0; --j) {
                newBin[offset+j] = newBin[offset+j] ^ newBin[offset+j+1];
            }
        }
        return newBin;
    }
    
    public static String bitString(boolean[] encoded) {
        StringBuilder sb = new StringBuilder(encoded.length);
        for (boolean b : encoded) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public static int encodeFloat(float f) {
        boolean neg = f < 0;
        if (neg) f = -f;
        int index = 0;
        while (index+1 < conversionTable.length && f > conversionTable[index]) {
            index++;
        }
        if (neg) return conversionTable.length-index-1;
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
        return encoded;//binToGray(encoded);
    }

    public static float[] decode(boolean[] encoded) {
        //encoded = grayToBin(encoded);
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
