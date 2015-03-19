package weightadjuster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Attempts to create a n-dimensional map of the scores using a finite number
 * of data points, which it will continually generate.
 */
public class SmoothingAdjuster implements WeightAdjuster {
    final float weightRange = 100f;
    //final int nIntervals = 12;

    private final int dim;
    private int unfixedDim;
    private int[] unfixedMappings;
    ArrayList<DataPoint> dataPoints;
    private HashMap<Integer, Float> fixedWeights;
    private float[] lastWeights;
    Random rand = new Random();
    
    private int sequence;

    public SmoothingAdjuster(int dim) {
        dataPoints = new ArrayList<>();
        fixedWeights = new HashMap<>();

        this.dim = dim;
        adjustUnfixedMappings();
    }

    @Override
    public String adjust(float[] results, float[] weights) {
        if (lastWeights != null)
            dataPoints.add(new DataPoint(lastWeights, results));
        boolean report = setWeights(results, weights);
        setFixedWeights(weights);
        
        lastWeights = copy(weights);
        return report ? "SUGGESTED!." : null;// "explore.....";
    }
    
    @Override
    public void fixValue(int index, float value) {
        fixedWeights.put(index, value);
        adjustUnfixedMappings();
    }
    
    public void adjustUnfixedMappings() {
        unfixedDim = dim - fixedWeights.size();
        unfixedMappings = new int[unfixedDim];
        int unfixedIndex = 0;
        for (int i=0; i<dim; i++) {
            if (!fixedWeights.containsKey(i)) {
                unfixedMappings[unfixedIndex++] = i;
            }
        }
    }
    
    private float[] copy(float[] weights) {
        float[] unfixedWeights = new float[unfixedDim];
        for (int i=0; i<unfixedDim; i++) {
            unfixedWeights[i] = weights[unfixedMappings[i]];
        }
        return unfixedWeights;
    }

    final float sqrtWeightRange = (float)Math.sqrt(weightRange);
    final float twoTimesSqrtWeightRange = 2*sqrtWeightRange;
    private boolean setWeights(float[] results, float[] weights) {
        
        boolean report;
        if (exploring()) {
            for (int i=0; i<unfixedMappings.length; i++) {
                float value = rand.nextFloat()*twoTimesSqrtWeightRange - sqrtWeightRange;
                boolean negative = value < 0;
                value *= value;
                if (negative)
                    value = -value;
                weights[unfixedMappings[i]] = value;
            }
            normaliseWeights(weights);
            report = false;
        } else {
            float[] newWeights = computeHighestScoringPosition();
            for (int i=0; i<unfixedMappings.length; i++) {
                weights[unfixedMappings[i]] = newWeights[i];
            }
            report = true;
        }
        sequence++;
        return report;
    }
    
    private void normaliseWeights(float[] weights) {
        float sum = 0;
        for (int i=0; i<unfixedMappings.length; i++) {
            sum += Math.abs(weights[unfixedMappings[i]]);
        }
        float multiplier = weightRange/sum;
        for (int i=0; i<unfixedMappings.length; i++) {
            weights[unfixedMappings[i]] *= multiplier;
        }
    }
    
    private void normalise(float[] fs) {
        float sum = 0;
        for (int i=0; i<fs.length; i++) {
            sum += Math.abs(fs[i]);
        }
        float multiplier = weightRange/sum;
        for (int i=0; i<fs.length; i++) {
            fs[i] *= multiplier;
        }
    }

    final float peturbDistance = weightRange/1000f;
    final float halfPeturbDistance = peturbDistance/2;
    private void peturb(float[] fs) {
        //final float peturbDistance = weightRange/1000f;
        //final float halfPeturbDistance = peturbDistance/2;
        
        for (int i=0; i<fs.length; i++) {
            float p = rand.nextFloat()*peturbDistance - halfPeturbDistance;
            fs[i] += p;
        }
        normalise(fs);
    }


    private boolean exploring() {
        return (sequence+1)%20 != 0;
    }

    private float[] computeHighestScoringPosition() {
        float[] scores = new float[dataPoints.size()];
        float[][] positions = new float[dataPoints.size()][];
        IntStream.range(0, dataPoints.size())
            .parallel()
            .forEach(i -> {
                positions[i] = dataPoints.get(i).weights;
                peturb(positions[i]);
                scores[i] = valueOf(positions[i]);
            });

        float largest = Float.NEGATIVE_INFINITY;
        float smallest = Float.POSITIVE_INFINITY;
        int largestIndex = -1;
        for (int i=0; i<scores.length; i++) {
            if (scores[i] >= largest) {
                largest = scores[i];
                largestIndex = i;
            }
            if (scores[i] < smallest) {
                smallest = scores[i];
            }
        }
        System.out.println(smallest + " | " + largest);
        try{
        System.out.println("Choose datapoint " + Arrays.toString(positions[largestIndex]) + " with value " + scores[largestIndex]);
        }catch(ArrayIndexOutOfBoundsException e) {
            System.out.println(Arrays.toString(scores));
            System.out.println(largestIndex);
            System.out.println(largest);
            throw e;
        }
        return positions[largestIndex];
    }

    /*private float[] computeHighestScoringPosition() {
        final int mapSize = pow(unfixedDim, nIntervals);
        
        float[] scoreMap = new float[mapSize];
        float[][] positions = new float[mapSize][];
        IntStream.range(0, mapSize)
            .parallel()
            .forEach(i -> {
                positions[i] = position(i);
                scoreMap[i] = valueOf(positions[i]);
            });
        
        float largest = Float.NEGATIVE_INFINITY;
        int largestIndex = -1;
        for (int i=0; i<scoreMap.length; i++) {
            if (scoreMap[i] >= largest) {
                largest = scoreMap[i];
                largestIndex = i;
            }
        }
        //System.out.println(dataPoints);
        //System.out.println("Score Map: " + Arrays.toString(scoreMap));
        //System.out.println(Arrays.deepToString(positions));
        
        return positions[largestIndex];
    }*/

    /*final float[] position(int index) {
        final float intervalSize = 2*weightRange/nIntervals;

        float[] position = new float[unfixedDim];
        for (int i=0; i<unfixedDim; i++) {
            float offset = rand.nextFloat()*intervalSize;
            int slot = index%nIntervals;
            index /= nIntervals;
            position[i] = slot*intervalSize + offset;
        }
        return position;
    }*/
    
    /**
     * computes a^n for n >= 0.
     */
    public static int pow(int a, int n) {
        int r = 1;
        int e = a;
        while (n>0) {
            if (n%2 == 1) {
                r *= e;
            }
            e *= e;
            n /= 2;
        }
        return r;
    }

    private class DataPoint {
        public final float[] weights;
        public final float mean;
        public final float sd;
        
        public DataPoint(float[] copiedWeights, float[] results) {
            this.weights = copiedWeights;
            this.mean = results[0];
            this.sd = results[1];
        }
        
        public float score() {
            float value = mean;
            //if (value < 1) value -= 500;
            //if (value < 2) value -= 100;
            //else if (value < 5) value -= 20;
            //else if (value < 10) value -= 5;
            //else if (value > 100) value += 100;
            return value;
        }
        
        public String toString() {
            return mean + "|"+sd+"|"+score();
        }
    }
    
    public float valueOf(float[] currentPosition) {
        double totalValue = dataPoints
                .parallelStream()
                .mapToDouble(dataPoint -> computeValue(currentPosition, dataPoint))
                .sum();
        double normalizer = dataPoints
                .parallelStream()
                .mapToDouble(dataPoint -> individualSize(currentPosition, dataPoint))
                .sum();
        float value = (float)(totalValue/normalizer);
        return normalizer == 0 ? 0 : value;
    }

    private double individualSize(float[] currentPosition, DataPoint dataPoint) {
        return computeSize(currentPosition, dataPoint.weights);
    }

    private double computeValue(float[] currentPosition, DataPoint dataPoint) {
        double size = computeSize(currentPosition, dataPoint.weights);
        return size*dataPoint.score();
    }

    private double computeSize(float[] vector1, float[] vector2) {
        //return dotProduct(vector1, vector2)/10000;
        
        double distance = computeScaledDistance(vector1, vector2);
        double val = distance/weightRange; // e^-(x^4)
        val *= val;
        val *= val;
        val = Math.exp(-val);
        if (val < 0.1f) val = 0;
        return val;
    }
    
    private double dotProduct(float[] vector1, float[] vector2) {
        double sum = 0;
        for (int i=0; i<vector1.length; ++i) {
            sum += vector1[i]*vector2[i];
        }
        return sum;
    }

    private double computeDistance(float[] vector1, float[] vector2) {
        float sumSquares = 0;
        for (int i=0; i<vector1.length; i++) {
            float di = vector1[i] - vector2[i];
            sumSquares += di*di;
        }
        return Math.sqrt(sumSquares);
    }

    private double computeScaledDistance(float[] vector1, float[] vector2) {
        float sumSquares = 0;
        for (int i=0; i<vector1.length; i++) {
            float ri = (Math.abs(vector1[i]) + Math.abs(vector2[i]))/2 + 0.1f;
            float di = vector1[i] - vector2[i];
            
            di = di*di;
            if (vector1[i] > 0 != vector2[i] > 0) {
                di += 1000;
            }
            
            sumSquares += di / Math.pow(ri, 0.2f);
            //System.out.println(di + " " + (di / Math.pow(ri, 0.2f)));
        }
        return Math.sqrt(sumSquares);
    }

    private void setFixedWeights(float[] weights) {
        for (Map.Entry<Integer, Float> entry : fixedWeights.entrySet()) {
            int key = entry.getKey();
            float value = entry.getValue();
            weights[key] = value;
        }
    }
}
