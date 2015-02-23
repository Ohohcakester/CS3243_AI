import java.util.Arrays;
import java.util.Random;


public class OhTestPlayer {
    private Random rand = new Random();
    private int lastPiece;
    private int lastOrient;
    private int lastPos;
    private int current;
    
    public OhTestPlayer() {
        current = 0;
        lastPiece = 0;
    }
    
    
    
    public int[] findBest(State s, int[][] legalMoves) {
        float largest = Float.NEGATIVE_INFINITY;
        int[] bestMove = null;
        for (int[] legalMove : legalMoves) {
            NextState nextState = NextState.generate(s, legalMove);
            float sum = 0;
            
            sum += -100f * FeatureFunctions.totalColumnsHeight(s, nextState);
            sum += -100f * FeatureFunctions.maximumColumnHeight(s, nextState);
            sum += FeatureFunctions.lost(s, nextState) > 0 ? Float.NEGATIVE_INFINITY : 0;
            
            if (sum >= largest) {
                bestMove = legalMove;
                largest = sum;
            }
        }
        
        return bestMove;
    }
    
    
    

    //implement this function to have a working system
    public int[] pickMove(State s, int[][] legalMoves) {
        int piece = s.getNextPiece();
        int orient = chooseOrient(piece);
        int position = State.COLS-1;
        
        if (piece != 1 || rand.nextInt(3) != 0) {
            current+=width(lastOrient, lastPiece);
            if (current > posLimit(orient, piece)) {
                current = 0;
            }
            position = current;
        }
        
        lastPiece = piece;
        lastOrient = orient;
        lastPos = position;
        return new int[]{orient, position};
    }
    
    
    private int chooseOrient(int piece) {
        final int[] o = new int[]{0,0,0,0,0,1,1};
        return o[piece];
    }
    
    
    private int posLimit(int orient, int piece) {
        return State.COLS-width(orient, piece);
    }
    
    private static int width(int orient, int piece) {
       final int[][] w = new int[][]{
                new int[]{2},
                new int[]{1,4},
                new int[]{2,3,2,3},
                new int[]{2,3,2,3},
                new int[]{2,3,2,3},
                new int[]{3,2},
                new int[]{3,2}
        };
        return w[piece][orient];
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        OhTestPlayer p = new OhTestPlayer();
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
        State s = new State();
        OhTestPlayer p = new OhTestPlayer();
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }*/
    
}
