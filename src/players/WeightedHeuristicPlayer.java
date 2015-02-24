package players;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import main.TFrame;
import weightadjuster.SmoothingAdjuster;
import weightadjuster.WeightAdjuster;

/**
 * A configure, extensible weighted heuristic player.
 * Override the functions "configure" and "initialiseWeights" with your own.
 */
public class WeightedHeuristicPlayer {
    protected float[] weights;
    protected Feature[] features;
    
    public WeightedHeuristicPlayer() {
        configure();
        initialiseWeights();
    }
    
    public int dim() {
        return features.length;
    }
    
    protected void configure() {
        features = new Feature[]{
                (s,n)->FeatureFunctions.lost(s,n),
                (s,n)->FeatureFunctions.maximumColumnHeight(s,n),
                (s,n)->FeatureFunctions.totalHoles(s,n),
                (s,n)->FeatureFunctions.totalColumnsHeight(s,n)
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        weights = new float[]{-99999.0f, -4, -12.78658f, -21.596615f};
    }
    
    private float heuristic(State state, int[] legalMove) {
        NextState nextState = NextState.generate(state, legalMove);
        float sum = 0;
        for (int i=0; i<weights.length; i++) {
            sum += weights[i] * features[i].compute(state, nextState);
        }
        return sum;
    }
    
    
    public int[] findBest(State s, int[][] legalMoves) {
        float[] scores = new float[legalMoves.length];
        DoubleStream stream = IntStream.range(0, legalMoves.length)
                .parallel()
                .mapToDouble(i -> scores[i] = heuristic(s, legalMoves[i]));
        stream.max();

        float largest = Float.NEGATIVE_INFINITY;
        int best = -1;
        for (int i=0; i<scores.length; i++) {
            if (scores[i] >= largest) {
                best = i;
                largest = scores[i];
            }
        }
        return legalMoves[best];
    }
    
    public void play(float[] results) {
        final int tries = 200;
        int sum = 0;
        int sumSquare = 0;
        
        for (int i=0; i<tries; i++) {
            State s = new State();
            while(!s.hasLost()) {
                s.makeMove(findBest(s,s.legalMoves()));
            }
            int cleared = s.getRowsCleared();
            sum += cleared;
            sumSquare += cleared*cleared;
        }
        
        float stdDev = (float)Math.sqrt((float)(tries*sumSquare - sum*sum)/tries/tries);
        results[0] = (float)sum/tries;
        results[1] = stdDev;
    }
    
    public void learn(WeightAdjuster adjuster) {
        float[] results = new float[2];
        int iteration = 0;
        while(true) {
            play(results);
            String adjusterReport = adjuster.adjust(results, weights);
            if (adjusterReport != null) {
                report(iteration, results, weights, adjusterReport);
            }
            iteration++;
        }
    }

    
    private void report(int iteration, float[] results, float[] weights, String report) {
        System.out.println("===========================================");
        System.out.println("Iteration " + iteration +
                " | Mean Score = " + results[0] + 
                " | SD = " + results[1]);
        System.out.println("Weights = " + Arrays.toString(weights));
        System.out.println(report);
    }

    
    public static void main(String[] args) {
        int choice = 1; // 0 to watch, 1 to learn.
        
        switch(choice) {
            case 0:
                watch();break;
            case 1:
                learn();break;
        }
    }
    
    public static void watch() {
        State s = new State();
        new TFrame(s);
        WeightedHeuristicPlayer p = new WeightedHeuristicPlayer();
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
    public static void learn() {
        WeightedHeuristicPlayer p = new WeightedHeuristicPlayer();
        
        WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        adjuster.fixValue(0, -99999f);
        p.learn(adjuster);
    }
}

interface Feature {
    float compute(State s, NextState n);
}
