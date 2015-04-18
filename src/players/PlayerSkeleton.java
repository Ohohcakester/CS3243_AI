package players;
import main.State;
import main.TFrame;

/**
 * WARNING: Requires Java Version 8 to run.
 * If this does not compile, please upgrade to the latest version of Java.
 */
public class PlayerSkeleton {
    
    private OhPlayer p;
    
    public PlayerSkeleton() {
        p = new OhPlayer();

        // Uncomment this to switch to 1-level minimax.
        //p.switchToMinimax(1);
    }

    //implement this function to have a working system
    public int[] pickMove(State s, int[][] legalMoves) {
        return p.findBest(s, legalMoves);
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        OhPlayer p = new OhPlayer();
        
        // Uncomment this to switch to 1-level minimax.
        //p.switchToMinimax(1);
        
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
}
