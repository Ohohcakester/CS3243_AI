import java.util.Arrays;


public class NextState {
    
    public final int[][] field;
    public final int[] top;
    public final boolean lost;
    public final int cleared;
    public final int turn;
    private final int rowsCleared;

    /**
     * Copy constructor
     */
    private NextState(int[][] field, int[] top, boolean lost, int cleared, int turn, int rowsCleared) {
        this.field = field;
        this.top = top;
        this.lost = lost;
        this.cleared = cleared;
        this.turn = turn;
        this.rowsCleared = rowsCleared;
    }

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
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
        int orient = move[0];
        int slot = move[1];
        int nextPiece = s.getNextPiece();

        int ROWS = State.ROWS;
        int COLS = State.COLS;
        int[][][] pBottom = State.getpBottom();
        int[][][] pTop = State.getpTop();
        int[][] pWidth = State.getpWidth();
        int[][] pHeight = State.getpHeight();

        int[][] field = s.getField();
        int[][] newField = new int[ROWS][COLS];
        for (int y=0; y<ROWS; ++y) {
            for (int x=0; x<COLS; ++x) {
                newField[y][x] = field[y][x];
            }
        }
        field = newField;
        int[] top = Arrays.copyOf(s.getTop(), s.getTop().length);
        
        boolean lost = s.hasLost();
        int cleared = s.getRowsCleared();
        int turn = s.getTurnNumber();
        int rowsCleared = 0;

        
        
        turn++;
        //height if the first column makes contact
        int height = top[slot]-pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for(int c = 1; c < pWidth[nextPiece][orient];c++) {
            height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
        }
        
        //check if game ended
        if(height+pHeight[nextPiece][orient] >= ROWS) {
            lost = true;
            return new NextState(field, top, lost, cleared, turn, 0);
        }

        
        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
            
            //from bottom to top of brick
            for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
                field[h][i+slot] = turn;
            }
        }
        
        //adjust top
        for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot+c]=height+pTop[nextPiece][orient][c];
        }
        
        //check for full rows - starting at the top
        for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for(int c = 0; c < COLS; c++) {
                if(field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if(full) {
                rowsCleared++;
                cleared++;
                //for each column
                for(int c = 0; c < COLS; c++) {

                    //slide down all bricks
                    for(int i = r; i < top[c]; i++) {
                        field[i][c] = field[i+1][c];
                    }
                    //lower the top
                    top[c]--;
                    while(top[c]>=1 && field[top[c]-1][c]==0)   top[c]--;
                }
            }
        }
    
        return new NextState(field, top, lost, cleared, turn, rowsCleared);
    }
    
}
