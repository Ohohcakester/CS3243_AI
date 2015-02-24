package weightadjuster;

public class OhAdjuster implements WeightAdjuster {

    @Override
    public void adjust(float[] results, float[] weights) {
        copy(weights, new float[]{-9999f, -1f, -1f, -1f});
    }

    private void copy(float[] to, float[] from) {
        for (int i=0; i<to.length; i++) {
            to[i] = from[i];
        }
    }
}
