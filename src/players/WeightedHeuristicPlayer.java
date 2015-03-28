package players;
import java.util.Arrays;
import java.util.stream.IntStream;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import main.TFrame;
import weightadjuster.GeneticAlgorithmAdjuster;
import weightadjuster.GeneticAlgorithmAdjuster2;
import weightadjuster.PartialGamePlayer;
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
                (n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.maxHeight(n),
                (n)->FeatureFunctions.numHoles(n),
                (n)->FeatureFunctions.sumHeight(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.numRowsCleared(n),
                (n)->FeatureFunctions.numFilledCells(n),
                //(n)->FeatureFunctions.minMaximumColumnHeight(n),
                //(n)->FeatureFunctions.minMaxTotalHoles(n),
                (n)->FeatureFunctions.maxHeightDifference(n),
                FeatureFunctions.variableHeightMinimaxInt(
                        (h) -> State.ROWS-h,
                        FeatureFunctions.negHeightRegion(1)
                        ),
                FeatureFunctions.minimaxInt(2, FeatureFunctions.negHeightRegion(10)),
                FeatureFunctions.minimaxInt(2, FeatureFunctions.negHeightRegion(7))
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        //weights = new float[]{-99999.0f, -5f, -5f};
        //weights = new float[]{-99999.0f, -0.0f, -72.27131f, -0.39263827f, -18.150364f, 1.9908575f, -4.523054f, 2.6717715f}; // <-- good weights.
        weights = new float[]{-99999.0f, -0.0f, -80.05821f, 0.2864133f, -16.635815f, -0.0488357f, -2.9707198f, -1f, 100f, 100f, 10f}; // <-- better weights.
        //weights = new float[]{-99999.0f, -1, -4, -95};
    }
    
    public float playWithWeights(float[] weights, int times) {
        this.weights = new float[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = weights[i];
        }
        float total = 0;
        float[] results = new float[2];
        for (int i=0; i<times; ++i) {
            play(results);
            total += results[0];
        }
        return (total/times);
    }
    
    public float playPartialWithWeights(float[] weights, int times) {
        this.weights = new float[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = weights[i];
        }
        float total = 0;
        float[] results = new float[2];
        for (int i=0; i<1; ++i) {
            PartialGamePlayer.play(this, results, times);
            total += results[0];
        }
        return (total/times);
    }
    
    private float heuristic(State state, int[] legalMove) {
        NextState nextState = NextState.generate(state, legalMove);
        float sum = 0;
        for (int i=0; i<features.length; i++) {
            sum += weights[i] * features[i].compute(nextState);
        }
        //System.out.println(Arrays.toString(legalMove) + " = " + sum);
        return sum;
    }

    public void switchToMinimax(int levels) {
        features = new Feature[]{FeatureFunctions.minimax(levels, weightedFeature(features, weights))};
        weights = new float[]{1};
    }
    
    public static Feature weightedFeature(Feature[] features, float[] weights) {
        return (ns) -> {
            float sum = 0;
            for (int i=0; i<features.length; ++i) {
                sum += features[i].compute(ns)*weights[i];
            }
            return sum;
        };              
    }
    
    public int[] findBest(State s, int[][] legalMoves) {
        float[] scores = new float[legalMoves.length];
        IntStream.range(0, legalMoves.length)
            .parallel()
            .forEach(i -> scores[i] = heuristic(s, legalMoves[i]));
        //System.out.println("----");
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
        final int tries = 3;
        long sum = 0;
        long sumSquare = 0;
        
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
        String adjusterReport = null;
        while(true) {
            play(results);
            if (adjusterReport != null) {
                report(iteration, results, weights, adjusterReport);
            }
            adjusterReport = adjuster.adjust(results, weights);
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
        int choice = 0; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new WeightedHeuristicPlayer();
        WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        adjuster.fixValue(0, -99999f);
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
        
        switch(choice) {
            case 0:
                watch(p);break;
            case 1:
                learn(p, adjuster);break;
        }
    }
    
    public static void watch(WeightedHeuristicPlayer p) {
        final int REPORT_INTERVAL = 1000;
        State s = new State();
        new TFrame(s);

        int counter = REPORT_INTERVAL;
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            counter--;
            if (counter <= 0) {
                System.out.println("CURRENT SCORE: " + s.getRowsCleared());
                counter = REPORT_INTERVAL;
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
    /**
     * watch, but without UI. much faster
     */
    public static void checkScore(WeightedHeuristicPlayer p) {
        final int REPORT_INTERVAL = 1000;
        State s = new State();
        //new TFrame(s);

        int counter = REPORT_INTERVAL;
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            counter--;
            if (counter <= 0) {
                System.out.println("CURRENT SCORE: " + s.getRowsCleared());
                counter = REPORT_INTERVAL;
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
    /*public void learnWithGeneticAlgorithm(GeneticAlgorithmAdjuster adjuster) {
        adjuster.adjust();
    }*/
    
    public static void learn(GeneticAlgorithmAdjuster2 adjuster) {
        adjuster.adjust();
        //p.learnWithGeneticAlgorithm(adjuster);
    }
    
    public static void learn(GeneticAlgorithmAdjuster adjuster) {
        adjuster.adjust();
        //p.learnWithGeneticAlgorithm(adjuster);
    }
    
    public static void learn(WeightedHeuristicPlayer p, WeightAdjuster adjuster) {
        p.learn(adjuster);
    }
}
