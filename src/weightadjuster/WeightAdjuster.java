package weightadjuster;

/**
 * An interface for learning algorithms. We didn't use it eventually.
 * Not in use. 
 */
public interface WeightAdjuster {
    /**
     * @return a string if it has something to report.
     */
    public String adjust(float[] results, float[] weights);
    
    /**
     * Indicates that a weight should not be changed by the adjuster.
     * @param index the index of the feature weight to fix.
     * @param value The value to fix the weight to.
     */
    public void fixValue(int index, float value);
}