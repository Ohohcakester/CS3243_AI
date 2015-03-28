package weightadjuster;
import main.State;
import players.WeightedHeuristicPlayer;

/**
 * A configure, extensible weighted heuristic player.
 * Override the functions "configure" and "initialiseWeights" with your own.
 */
public class PartialGamePlayer {    
    
    public static void play(WeightedHeuristicPlayer p, float[] results, int tries) {
        long sum = 0;
        long sumSquare = 0;
        final int CONST_ADD = 200;
        final int INITIAL_HEIGHT = 5;
        final int STOP_HEIGHT = 2;
        int losses = 0;
        
        for (int i=0; i<tries; i++) {
            int maxHeight = 0;
            int score = 0;
            State s = new State();
            s.makeMove(p.findBest(s,s.legalMoves()));
            while(!s.hasLost() && p.maxHeight(s) < INITIAL_HEIGHT) {
                maxHeight = Math.max(maxHeight,p.maxHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
            }
            while(!s.hasLost() && p.maxHeight(s) > STOP_HEIGHT) {
                maxHeight = Math.max(maxHeight,p.maxHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
            }
            if (s.hasLost()) {
                score = 0;
                ++losses;
            } else {
                score = State.ROWS + CONST_ADD - INITIAL_HEIGHT - maxHeight;
            }
            sum += score;
            sumSquare += score * score;
        }
        float stdDev = (float)Math.sqrt((float)(tries*sumSquare - sum*sum)/tries/tries);
        results[0] = (float)sum/tries;
        results[1] = stdDev;
        //System.out.println("Losses" + (double)losses / tries * 100 + "%");
    }

}
