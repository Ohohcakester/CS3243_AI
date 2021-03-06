package weightadjuster;
import main.PredeterminedState;
import main.Sequence;
import main.State;
import players.WeightedHeuristicPlayer;

/**
 * Utility function to play partial games (maybe using a sequence from the sequence database).
 */
public class PartialGamePlayer {
    private static final int CONST_ADD = 300;
    private static final int INITIAL_HEIGHT = 5;
    private static final int STOP_HEIGHT = 2;

    public static void play(WeightedHeuristicPlayer p, float[] results, int tries, Sequence sequence) {
        long sum = 0;
        long sumSquare = 0;
        int losses = 0;
        
        for (int i=0; i<tries; i++) {
            int maxTotalHeight = 0;
            int score = 0;

            State s;
            if (sequence == null) {
                s = new State();
            } else {
                s = new PredeterminedState(sequence.pieces);
            }
            
            int count = 0;
            s.makeMove(p.findBest(s,s.legalMoves()));
            while(!s.hasLost() && WeightedHeuristicPlayer.maxHeight(s) < INITIAL_HEIGHT) {
                maxTotalHeight = Math.max(maxTotalHeight,WeightedHeuristicPlayer.totalColumnHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
                count++;
            }
            while(!s.hasLost() && WeightedHeuristicPlayer.maxHeight(s) > STOP_HEIGHT) {
                maxTotalHeight = Math.max(maxTotalHeight,WeightedHeuristicPlayer.totalColumnHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
                count++;
            }
            if (s.hasLost()) {
                score = 0;
                ++losses;
            } else {
                score = State.ROWS + CONST_ADD - maxTotalHeight;
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
