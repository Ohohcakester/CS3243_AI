package weightadjuster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import players.WeightedHeuristicPlayer;

public class GeneticAlgorithmAdjuster {
    protected static Random rand = new Random();
    protected int dim;
    protected int realDim;
    protected float[][] states;
    protected float[] scores;
    protected WeightedHeuristicPlayer w;
    protected PartialGamePlayer p;
    protected int stateNumber;
    protected final int INITIAL_GOOD_STATES = 0;
    protected int PRINT_INTERVAL = 10;
    
    
    protected double mutationProbability = 0.06;
    //protected double mutationProbability = 0.4;
    protected HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    
    protected float[] highScoreWeights;
    protected float highScore;

    //protected static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    protected static float[] conversionTable = new float[]{0.01f, 0.05f, 0.3f, 1.5f, 4f, 10f, 30f, 60f, 90f, 140, 200, 300, 500, 1000, 1500, 6000};
    protected static final int WORD_SIZE = 5;
    
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
    
    protected float[] generateRandomState(int length) {
        boolean[] encoded = new boolean[length*WORD_SIZE];
        for (int i=0; i<encoded.length; ++i) {
            encoded[i] = rand.nextBoolean();
        }
        return decode(encoded);
    }
    
    protected void generateRandomStates() {
        generateMostlyRandomStates();
    }
    
    private void generateTotallyRandomStates() {
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = generateRandomState(dim);
        }
    }
    
    private void generateMostlyRandomStates() {
        for (int i=0; i<INITIAL_GOOD_STATES; ++i) {
            states[i] = generateInitialGoodState();
        }
        for (int i = INITIAL_GOOD_STATES; i < stateNumber; ++i) {
            states[i] = generateRandomState(dim);
        }
    }
    
    private float[] generateInitialGoodState() {
        float[][] goodWeights = new float[][] {
            //new float[]{-100.0f, -2.0f, -0.5f, 5.0f, -500.0f, -40.0f, -70.0f, -40.0f, -3.0f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, -0.1f},
            //new float[]{-40.0f, -8000.0f, 100.0f, 10.0f, 0.5f, -5.0f, 0.5f, -8000.0f, -40.0f, 0.01f, -5.0f, -2.0f, -0.1f, -40.0f, -1.0f},
            //new float[]{0.05f, 2.0f, -2.0f, -0.05f, -8000.0f, -500.0f, -8000.0f, -500.0f, -500.0f, -0.01f, -10.0f, 40.0f, 20.0f, -3.0f, -1.0f},
            new float[]{0.01f, -200f, -1000f, -1500f, -1500f, -200f, 1000f, 4f, -1.5f,0,0,0},
            new float[]{1.5f, -1000f, -1000f, -1500f, -1500f, -200f, 1000f, 150f, -1.5f,0,0,0},
            new float[]{1.5f, -1000f, -1000f, -1500f, -1500f, -200f, 1000f, 4f, -1.5f,0,0,0},
            new float[]{1.5f, -1000f, -1000f, -1500f, -1500f, -200f, 1000f, 4f, -1.5f,0,0,0},
            new float[]{10f, -1000f, -1000f, -1000f, -1000f, -200f, 1000f, 25f, -1.5f,0,0,0},
            new float[]{10f, -150f, -1000f, -1500f, -150f, -500f, 1000f, -25f, -0.05f,0,0,0},
            new float[]{10f, -150f, -1000f, -1500f, -150f, -200f, 1000f, -100f, -0.05f,0,0,0}


        };
        int choice = rand.nextInt(goodWeights.length);
        float[] weights = goodWeights[choice];
        if (weights.length != dim) {
            throw new UnsupportedOperationException("ERROR!!! WRONG DIM!");
        }
        return weights;
    }
    
    protected void selection() {
        float totalScore = 0;
        while (totalScore == 0) {
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                float result = w.playPartialWithWeights(realWeights,50);
                //float result = w.playWithWeights(realWeights, 15);
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
               
            }
            states[i] = decode(firstBitString);
            states[i+1] = decode(secondBitString);
        }
    }
    
    private void mutation() {
        for (int i = 0; i < states.length; ++i) {
            boolean[] bitString = encode(states[i]);
            for (int j=0; j<3; ++j) {
                int position = rand.nextInt(bitString.length);
                double prob = rand.nextDouble();
                if (prob < mutationProbability) {
                    //System.out.println("MUTATION!");
                    bitString[position] ^= true;
                }
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
        //return encoded;
        return binToGray(encoded);
    }

    public static float[] decode(boolean[] encoded) {
        encoded = grayToBin(encoded);
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

    
    protected void maybeSave(int iteration) {
    }
    
    protected float playWithWeights(float[] realWeights, int times) {
        return w.playWithWeights(realWeights, 2);
    }
    
    public void adjust() {
        generateRandomStates();
        int iteration = Integer.MAX_VALUE;
        for (int i = 0; i < iteration; ++i) {
            System.out.println("Iteration " + i);
            selection();
            crossover();
            mutation();
            
            if ((i+1)%PRINT_INTERVAL == 0) {
                //System.out.println("Iteration " + i);
                float total = 0;
                for (int j = 0; j < stateNumber; ++j) {
                    float[] realWeights = generateRealWeights(states[j]);
                    
                    float result = printAndReturnResult(j, realWeights);
                    
                    total += result;
                    
                    if (result > highScore) {
                        highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
                        highScore = result;
                    }
                    
                }
                System.out.println("Average Score: " + (total/stateNumber));
                System.out.println("Hi-Score: " + highScore + " | " + Arrays.toString(highScoreWeights));
            }

            maybeSave(i);
        }
    }

    protected float printAndReturnResult(int j, float[] realWeights) {
        float result = playWithWeights(realWeights, 2);
        float resultPartial = w.playPartialWithWeights(realWeights, 10);

        System.out.printf("State #%2d. Score = %10.3f - %8.3f [",j,result,resultPartial);
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
