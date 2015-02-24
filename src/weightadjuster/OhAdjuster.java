package weightadjuster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Not a very good weight adjuster.
 * */
public class OhAdjuster implements WeightAdjuster {
    
    float currentBest;
    float bestMean;
    float bestSd;
    float[] bestWeights;
    float[] lastWeights;
    HashMap<Integer, Float> fixedWeights;
    Random rand = new Random();

    public OhAdjuster() {
        fixedWeights = new HashMap<>();
    }
    
    @Override
    public String adjust(float[] results, float[] weights) {
        boolean newHighScore = tryUpdateBest(results);
        setWeights(results, weights);
        setFixedWeights(weights);
        lastWeights = Arrays.copyOf(weights, weights.length);
        return newHighScore ? ("New High Score: " + currentBest) : null;
    }
    
    private void setWeights(float[] results, float[] weights) {
        for (int i=0; i<weights.length; i++) {
            weights[i] = rand.nextFloat()*10f- 5f;
        }
    }

    private void setFixedWeights(float[] weights) {
        for (Map.Entry<Integer, Float> entry : fixedWeights.entrySet()) {
            int key = entry.getKey();
            float value = entry.getValue();
            weights[key] = value;
        }
    }
    
    @Override
    public void fixValue(int index, float value) {
        fixedWeights.put(index, value);
    }
    
    private float computeResult(float[] results) {
        return results[0]*2f - results[1];
    }
    
    private boolean tryUpdateBest(float[] results) {
        float result = computeResult(results);
        if (result > currentBest) {
            bestMean = results[0];
            bestSd = results[1];
            bestWeights = lastWeights;
            currentBest = result;
            return true;
        }
        return false;
    }
}
