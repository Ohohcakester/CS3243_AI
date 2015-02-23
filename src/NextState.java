
public class NextState {
    
    public final int[][] field;
    public final int[] top;
    public final int nextPiece;
    public final boolean lost;
    public final int cleared;
    public final int turn;

    /**
     * Copy constructor
     * @param s
     */
    public NextState(State s) {
        this.field = null;
        this.top = null;
        this.nextPiece = 0;
        this.lost = false;
        this.cleared = 0;
        this.turn = 0;
    }

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
    }
    public int getNextPiece() {
        return nextPiece;
    }
    
    public boolean hasLost() {
        return lost;
    }
    
    public int getRowsCleared() {
        return cleared;
    }
    
    public int getTurnNumber() {
        return turn;
    }
    
    /**
     * @return a new CurrentState object.
     */
    public static NextState generate(State s, int[] move) {
        s.getNextPiece();
        return null;
    }
    
}
