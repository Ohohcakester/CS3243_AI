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

    //implement this function to have a working system
    public int[] pickMove(State s, int[][] legalMoves) {
        int piece = s.getNextPiece();
        int orient = chooseOrient(piece);
        int position = State.COLS-1;
        
        if (piece != 1 || rand.nextInt(3) != 0) {
            current+=width(lastOrient, lastPiece);
            if (current >= posLimit(orient, piece)) {
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
            s.makeMove(p.pickMove(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
}
