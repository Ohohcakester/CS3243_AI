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
    protected final int INITIAL_GOOD_STATES = 18;
    protected int PRINT_INTERVAL = 10;
    
    protected float total = 0;
    
    protected final int MUTATION_BITS = 2;
    protected double mutationProbability = 0.2;
    //protected double mutationProbability = 0.4;
    protected HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    
    protected float[] highScoreWeights;
    protected float highScore;
    
    //protected static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    //protected static float[] conversionTable = new float[]{0.01f, 0.05f, 0.3f, 1.5f, 4f, 10f, 30f, 60f, 90f, 140, 200, 300, 500, 1000, 1500, 6000};
    //protected static float[] conversionTable = new float[]{0.01f, 0.05f, 0.3f, 1.5f, 4f, 10f, 30f, 60f, 90f, 140, 200, 250,300,350,400,500,700,850, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 4000, 5000, 6000};
    protected static final int WORD_SIZE = 12;
    protected static final int HALF_TABLE_SIZE = pow2(WORD_SIZE-1);
    private static final int MUTATE_LENGTH_MAX = 8;
    
    private static int pow2(int n) {
        int v = 1;
        for (; n > 0; --n) v *= 2;
        return v;
    }
    
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
                new float[]{-1647f, 1870f, 1992f, -1439f, -1302f, -1792f, -790f, 1419f, -95f, 734f, -1794f, -1967f, -845f, -273f, 902f, 1233f, 132f},
                new float[]{-1653f, 1868f, 1918f, -1457f, -1283f, -1791f, -541f, 1414f, -85f, 736f, -1798f, -1966f, -837f, -273f, 881f, 1231f, 117f},
                new float[]{-1626f, 1800f, 1921f, -1202f, -1283f, -1796f, -537f, 1291f, -85f, 729f, -1857f, -1966f, -846f, -341f, 899f, 1492f, 386f},
                new float[]{-1613f, 1814f, 1917f, -1188f, -1294f, -1806f, -566f, 1283f, -88f, 739f, -1602f, -1971f, -844f, -274f, 898f, -823f, 379f},
                new float[]{-1636f, 1816f, 1916f, -1197f, -1301f, -1793f, -567f, 1292f, -92f, 721f, -1602f, -1976f, -838f, -288f, 897f, -799f, 376f},
                new float[]{-1664f, 1859f, 1977f, -1458f, -1291f, -1799f, -792f, 1404f, -86f, 730f, -1800f, -1977f, -833f, -275f, 896f, 1234f, 132f},
                new float[]{-1659f, 1871f, 1924f, -1443f, -1296f, -1791f, -536f, 1404f, -98f, 743f, -1797f, -1966f, -847f, -280f, 884f, 1234f, 119f},
                new float[]{-1629f, 1802f, 1923f, -1200f, -1301f, -1800f, -548f, 1274f, -92f, 729f, -1858f, -1981f, -856f, -349f, 897f, 1495f, 381f},
                new float[]{-1628f, 1805f, 1934f, -1200f, -1288f, -1793f, -578f, 1273f, -90f, 725f, -1613f, -1967f, -828f, -279f, 888f, -809f, 387f},
                new float[]{-1640f, 1815f, 1920f, -1196f, -1303f, -1795f, -570f, 1294f, -103f, 727f, -1601f, -1959f, -844f, -297f, 887f, -812f, 372f},
                new float[]{-1658f, 1859f, 1987f, -1444f, -1302f, -1805f, -800f, 1411f, -85f, 742f, -1797f, -1971f, -841f, -283f, 902f, 1242f, 124f},
                new float[]{-1663f, 1860f, 1929f, -1446f, -1297f, -1796f, -531f, 1401f, -89f, 728f, -1796f, -1969f, -842f, -280f, 892f, 1242f, 120f},
                new float[]{-1618f, 1806f, 1924f, -1198f, -1283f, -1794f, -543f, 1284f, -87f, 734f, -1865f, -1982f, -856f, -342f, 896f, 1493f, 375f},
                new float[]{-1620f, 1797f, 1929f, -1198f, -1295f, -1800f, -572f, 1275f, -92f, 725f, -1600f, -1972f, -838f, -275f, 897f, -819f, 380f},
                new float[]{-1627f, 1814f, 1917f, -1195f, -1309f, -1797f, -572f, 1291f, -97f, 733f, -1602f, -1972f, -833f, -284f, 890f, -811f, 362f},
                new float[]{-1654f, 1862f, 1993f, -1439f, -1294f, -1791f, -804f, 1401f, -94f, 729f, -1802f, -1980f, -838f, -278f, 900f, 1236f, 115f},
                new float[]{-1656f, 1860f, 1917f, -1455f, -1287f, -1799f, -546f, 1405f, -98f, 741f, -1797f, -1978f, -835f, -283f, 883f, 1244f, 120f},
                new float[]{-1624f, 1813f, 1928f, -1187f, -1296f, -1804f, -538f, 1279f, -84f, 728f, -1853f, -1972f, -855f, -351f, 900f, 1483f, 376f},
                new float[]{-1625f, 1809f, 1929f, -1186f, -1289f, -1797f, -572f, 1283f, -90f, 724f, -1597f, -1971f, -843f, -276f, 896f, -807f, 379f},
                new float[]{-1629f, 1812f, 1923f, -1189f, -1297f, -1793f, -577f, 1278f, -102f, 726f, -1605f, -1957f, -840f, -287f, 884f, -803f, 368f}
        };
        int choice = rand.nextInt(goodWeights.length);
        float[] weights = goodWeights[choice];
        if (weights.length != dim) {
            generateRandomState(dim);
            //throw new UnsupportedOperationException("ERROR!!! WRONG DIM!");
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
    
    protected void crossover() {
        for (int i = 0; i + 1 < states.length; i += 2) {
            boolean[] firstBitString = encode(states[i]);
            boolean[] secondBitString = encode(states[i+1]);
            int position = rand.nextInt(firstBitString.length);
            int position2 = rand.nextInt(firstBitString.length);
            if (position2 < position) {
                int temp = position2;
                position2 = position;
                position = temp;
            }
            
            for (int j = position; j < position2; ++j) {
                boolean temp = firstBitString[j];
                firstBitString[j] = secondBitString[j];
                secondBitString[j] = temp;
               
            }
            states[i] = decode(firstBitString);
            states[i+1] = decode(secondBitString);
        }
    }
    
    protected int mutationBits() {
        return MUTATION_BITS;
    }
    
    protected void mutation() {
        int mutationBits = mutationBits();
        for (int i = 0; i < states.length; ++i) {
            boolean[] bitString = encode(states[i]);
            for (int j=0; j<mutationBits; ++j) {
                int mutateLength = rand.nextInt(MUTATE_LENGTH_MAX);
                int position = rand.nextInt(bitString.length-mutateLength);
                double prob = rand.nextDouble();
                if (prob < mutationProbability) {
                    //System.out.println("MUTATION!");
                    for (int k=0; k<mutateLength; ++k)
                        bitString[position+k] ^= true;
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
        return (int)(f + HALF_TABLE_SIZE);
        
        /*boolean neg = f < 0;
        if (neg) f = -f;
        
        int index = 0;
        while (index+1 < conversionTable.length && f > conversionTable[index]) {
            index++;
        }
        if (neg) return conversionTable.length-index-1;
        else return conversionTable.length+index;*/
    }

    public static float decodeFloat(int v) {
        return v - HALF_TABLE_SIZE;
        /*boolean neg = v < HALF_TABLE_SIZE;
        if (neg) v = HALF_TABLE_SIZE - v - 1;
        else v -= HALF_TABLE_SIZE;
        if (neg) return -conversionTable[v];
        else return conversionTable[v];*/
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
                printTotalAndHighScore(total);
            }
        }
    }

    protected void printTotalAndHighScore(float total) {
        System.out.println("Average Score: " + (total/stateNumber));
        System.out.println("Hi-Score: " + highScore + " | " + Arrays.toString(highScoreWeights));
    }

    protected float printAndReturnResult(int j, float[] realWeights) {
        float result = playWithWeights(realWeights, 2);
        float resultPartial = w.playPartialWithWeights(realWeights, 10);

        printResults(j, result, resultPartial);
        return result;
    }

    protected void printResults(int stateNo, float result, float resultPartial) {
        System.out.printf("State #%2d. Score = %10.3f - %8.3f [",stateNo,result,resultPartial);
        for (int k = 0; k < states[stateNo].length; ++k) {
            System.out.printf("%9.2f", states[stateNo][k]);
            if (k + 1 < states[stateNo].length) {
                System.out.printf(",");
            } else {
                System.out.println("]");
            }
        }
    }
    
    
}
