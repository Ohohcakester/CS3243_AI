package main;
import java.util.Arrays;


public class NextState {
    
    public final int[][] field;
    public final int[] top;
    public final boolean lost;
    public final int cleared;
    public final int turn;
    public final int rowsCleared;

    public static final int[][][] legalMoves = generateLegalMoves();
    public static final int[] minimaxPieceOrdering = generateMinimaxPieceOrdering();

    public static final int[] generateMinimaxPieceOrdering() {
        int[] minimaxPieceOrdering = new int[State.N_PIECES];
        
        // Worst to best:
        // 5 6 2 3 4 0 1
        minimaxPieceOrdering = new int[]{0,5,6,2,3,4,1};
        
        return minimaxPieceOrdering;
    }
    
    public static final int[][][] generateLegalMoves() {
        int[][][] legalMoves = new int[State.N_PIECES][][];
        //for each piece type
        for(int i = 0; i < State.N_PIECES; i++) {
            //figure number of legal moves
            int n = 0;
            for(int j = 0; j < State.pOrients[i]; j++) {
                //number of locations in this orientation
                n += State.COLS+1-State.pWidth[i][j];
            }
            //allocate space
            legalMoves[i] = new int[n][2];
            //for each orientation
            n = 0;
            for(int j = 0; j < State.pOrients[i]; j++) {
                //for each slot
                for(int k = 0; k < State.COLS+1-State.pWidth[i][j];k++) {
                    legalMoves[i][n][State.ORIENT] = j;
                    legalMoves[i][n][State.SLOT] = k;
                    n++;
                }
            }
        }
        return legalMoves;
    }
    
    /**
     * (Shallow) Copy constructor
     */
    private NextState(int[][] field, int[] top, boolean lost, int cleared, int turn, int rowsCleared) {
        this.field = field;
        this.top = top;
        this.lost = lost;
        this.cleared = cleared;
        this.turn = turn;
        this.rowsCleared = rowsCleared;
    }

    /**
     * @return a new CurrentState object.
     */
    public static NextState generate(NextState s, int piece, int[] move) {
        int nextPiece = piece;
        return makeMove(nextPiece, move, s.field, s.top, s.lost, s.cleared, s.turn);
    }
    
    /**
     * @return a new CurrentState object.
     */
    public static NextState generate(State s, int[] move) {
        int nextPiece = s.getNextPiece();
        int[][] field = s.getField();
        int[] top = s.getTop();
        boolean lost = s.hasLost();
        int cleared = s.getRowsCleared();
        int turn = s.getTurnNumber();
        
        return makeMove(nextPiece, move, field, top, lost, cleared, turn);
    }

    private static NextState makeMove(int nextPiece, int[] move, int[][] field,
            int[] top, boolean lost, int cleared, int turn) {
        int rowsCleared = 0;
        int orient = move[0];
        int slot = move[1];

        int ROWS = State.ROWS;
        int COLS = State.COLS;
        int[][][] pBottom = State.getpBottom();
        int[][][] pTop = State.getpTop();
        int[][] pWidth = State.getpWidth();
        int[][] pHeight = State.getpHeight();

        {
            top = Arrays.copyOf(top, top.length);
            int[][] newField = new int[ROWS][COLS];
            for (int y=0; y<newField.length; ++y) {
                newField[y] = Arrays.copyOf(field[y], field[y].length);
            }
            field = newField;
        }
        
        
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
