package players;

import main.FeatureFunctions;
import main.NextState;
import main.State;
import main.TFrame;
import weightadjuster.SmoothingAdjuster;

public class JonathanPlayer extends WeightedHeuristicPlayer {

    protected void configure() {
        features = new Feature[]{
                (s,n)->FeatureFunctions.lost(s,n),
                (s,n)->FeatureFunctions.maximumColumnHeight(s,n),
                (s,n)->FeatureFunctions.totalHoles(s,n),
                (s,n)->FeatureFunctions.totalColumnsHeight(s,n),
                (s,n)->FeatureFunctions.bumpiness(s, n),
                (s,n)->FeatureFunctions.completedLines(s, n),
                (s,n)->FeatureFunctions.totalFilledCells(s, n)
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        weights = new float[]{-99999.0f, -50.376028f, -66.54607f, 2.692179f, 0.0f, 0.0f, 0.0f};/*
        weights[0] = -99999.0f;
        weights[1] = -50.0f;
        weights[2] = -25.0f;
        weights[3] = 0f;
        weights[4] = 0f;
        weights[5] = 0f;
        weights[6] = 0f;*/
    }


    public static void main(String[] args) {
        BlahPlayer p = new BlahPlayer();
        SmoothingAdjuster adj = new SmoothingAdjuster(p.dim());
        
        watch(p);
        
        adj.fixValue(0, -99999f);
        //learn(p, adj);
    }
    
    public static void watch(WeightedHeuristicPlayer p) {
        State s = new State();
        new TFrame(s);
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
}
