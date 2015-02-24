package players;
import java.util.Arrays;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import main.TFrame;
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
    
    protected void configure() {
        features = new Feature[]{
                (s,n)->FeatureFunctions.lost(s,n),
                (s,n)->FeatureFunctions.maximumColumnHeight(s,n),
                (s,n)->FeatureFunctions.totalColumnsHeight(s,n),
                (s,n)->FeatureFunctions.totalHoles(s,n)
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        weights[0] = -999999.99f;
        weights[1] = -2f;
        weights[2] = -2f;
        weights[3] = -2f;
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
        float largest = Float.NEGATIVE_INFINITY;
        int[] bestMove = null;
        for (int[] legalMove : legalMoves) {
            float h = heuristic(s, legalMove);
            if (h >= largest) {
                bestMove = legalMove;
                largest = h;
            }
        }
        
        return bestMove;
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
    
    public void learn(WeightAdjuster adjuster, int reportInterval) {
        float[] results = new float[2];
        int iteration = 0;
        while(true) {
            play(results);
            adjuster.adjust(results, weights);
            if (iteration%reportInterval == 0) {
                report(iteration, results, weights);
            }
            iteration++;
        }
    }
    
    private void report(int iteration, float[] results, float[] weights) {
        System.out.println("===========================================");
        System.out.println("Iteration " + iteration +
                " | Mean Score = " + results[0] + 
                " | SD = " + results[1]);
        System.out.println("Weights = " + Arrays.toString(weights));
    }

    
    public static void main(String[] args) {
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
    
    /*public static void main(String[] args) {
        int sum = 0;
        int sumSquare = 0;
        int tries = 200;
        
        for (int i=0; i<tries; i++) {
            State s = new State();
            WeightedHeuristicPlayer p = new WeightedHeuristicPlayer();
            while(!s.hasLost()) {
                s.makeMove(p.findBest(s,s.legalMoves()));
            }
            sum += s.getRowsCleared();
            sumSquare += s.getRowsCleared()*s.getRowsCleared();
        }
        double stdDev = Math.sqrt((float)(tries*sumSquare - sum*sum)/tries/tries);
        System.out.println("Average rows cleared: " + (sum/tries));
        System.out.println("Standard Dev: " + stdDev);
    }*/
}

interface Feature {
    float compute(State s, NextState n);
}
