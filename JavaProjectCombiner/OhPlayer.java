import java.util.Map;
import java.util.Arrays;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.IOException;


class PlayerSkeleton {

    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        
        return 0;
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        WeightedHeuristicPlayer p = new OhPlayer();
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


class FeatureFunctions {

    public static float exampleFeature(NextState nextState) {
        return 4;
    }

    /**
     * Returns 1 if state is lost, 0 otherwise.
     */
    public static float lost(NextState nextState) {
        return nextState.lost ? 1 : 0;
    }

    /**
     * Returns the number of rows cleared.
     * Maximize to ensure game continuity.
     */
    public static float numRowsCleared(NextState nextState) {
        return nextState.cleared;
    }

    /**
     * Returns the maximum column height.
     */
    public static float maxHeight(NextState nextState) {
        int maxHeight = Integer.MIN_VALUE;

        int top[] = nextState.top;
        for (int x : top) {
            if (x > maxHeight) {
                maxHeight = x;
            }
        }

        return maxHeight;
    }

    /**
     * Returns the (maximum column height) ^ 3
     */
    public static float maxHeightCube(NextState nextState) {
        int maximumHeight = Integer.MIN_VALUE;
        int top[] = nextState.top;

        for (int x : top) {
            if (x > maximumHeight) {
                maximumHeight = x;
            }
        }

        return (float)Math.pow(maximumHeight,3);
    }

    /**
     * Returns the sum of all column heights.
     */
    public static float sumHeight(NextState nextState) {
        int sumHeight = 0;

        int top[] = nextState.top;
        for (int x : top) {
            sumHeight += x;
        }

        return sumHeight;
    }

    /**
     * Returns the difference between the maximum and minimum column heights.
     */
    public static float maxHeightDifference(NextState nextState) {
        int maxHeight = Integer.MIN_VALUE;
        int minHeight = Integer.MAX_VALUE;

        int top[] = nextState.top;
        for (int x : top) {
            if (x > maxHeight) {
                maxHeight = x;
            }

            if (x < minHeight) {
                minHeight = x;
            }
        }

        return maxHeight - minHeight;
    }

    /**
     * Returns the bumpiness of the column heights.
     * Minimize to ensure the top as flat as possible to avoid deep wells.
     */
    public static float bumpiness(NextState nextState) {
        int bumpiness = 0;

        int top[] = nextState.top;
        for (int i = 0; i < State.COLS - 1; i++) {
            bumpiness += Math.abs(top[i] - top[i + 1]);
        }

        return bumpiness;
    }

    /**
     * Starts from the top of the first column, and draws a line tracing the "shape"
     * of the top row to the top of the last column.
     */
    public static float topPerimeter(NextState nextState) {
        int perimeter = 0;
        int[][] field = nextState.field;
        int top[] = nextState.top;

        int rows = State.ROWS;
        int y = top[0];
        int x = 0;

        //System.out.println(Arrays.deepToString(field) + "\n" + y);
        boolean lastIsUpRight = true;
        boolean tl, tr, bl, br;
        while (x < State.COLS) {
            tl = (y < rows && x <= 0 || field[y][x-1] != 0);
            tr = (y < rows && field[y][x] != 0);
            bl = (y <= 0 || x <= 0 || field[y-1][x-1] != 0);
            br = (y <= 0 || field[y-1][x] != 0);
            //System.out.println(tl + " | " + tr +  " | " + bl + " | " + br);
            
            if (tr) {
                if (br) {
                    if (tl) {
                        /* @@
                        \  ?@ */
                        x--;
                        lastIsUpRight = false;
                    } else {
                        /* _@
                        \  ?@ */
                        y++;
                        lastIsUpRight = true;
                    }
                } else {
                    if (tl) {
                        if (bl) {
                            /* @@
                            \  @_ */
                            y--;
                            lastIsUpRight = false;
                        } else {
                            /* @@
                            \  __ */
                            x--;
                            lastIsUpRight = false;
                        }
                    } else {
                        if (bl) {
                            /* _@
                            \  @_ */
                            if (lastIsUpRight) {
                                y++;
                                lastIsUpRight = true;
                            } else {
                                y--;
                                lastIsUpRight = false;
                            }
                        } else {
                            /* _@
                            \  __ */
                            y++;
                            lastIsUpRight = true;
                        }
                    }
                }
            } else {
                if (br) {
                    if (tl) {
                        if (bl) {
                            /* @_
                            \  @@ */
                            x++;
                            lastIsUpRight = true;
                        } else {
                            /* @_
                            \  _@ */
                            if (lastIsUpRight) {
                                x--;
                                lastIsUpRight = false;
                            } else {
                                x++;
                                lastIsUpRight = true;
                            }
                        }
                    } else {
                        /* __
                        \  ?@ */
                        x++;
                        lastIsUpRight = true;
                    }
                } else {
                    if (bl) {
                        /* ?_
                        \  @_ */
                        y--;
                        lastIsUpRight = false;
                    } else {
                        /* ?_
                        \  __ */
                        x--;
                        lastIsUpRight = false;
                    }
                }
            }
            perimeter++;
        }
        return perimeter;
    }

    /**
     * Returns the number of filled cells.
     */
    public static float numFilledCells(NextState nextState) {
        int filledCells = 0;
        int field[][] = nextState.field;

        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] != 0) {
                    ++filledCells;
                }
            }
        }

        return filledCells;
    }
    
    /**
     * Returns the sum of all filled cell heights.
     */
    public static float sumFilledCellHeight(NextState nextState) {
        int sumFilledCellHeight = 0;

        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (field[i][j] != 0) {
                    sumFilledCellHeight += i;
                }
            }
        }

        return sumFilledCellHeight;
    }

    /**
     * Returns the number of empty cells.
     */
    public static float numEmptyCells(NextState nextState) {
        int emptyCells = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;

        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    ++emptyCells;
                }
            }
        }

        return emptyCells;
    }

    /**
     * Returns the number of holes.
     */
    public static float numHoles(NextState nextState) {
        int holes = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;

        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    ++holes;
                }
            }
        }

        return holes;
    }
    
    /**
     * Returns the number of holes, to a given power.
     */
    public static float numHolesPow(NextState nextState, int power) {
        int holes = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;

        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    ++holes;
                }
            }
        }

        return (float)Math.pow(holes,power);
    }

    /**
     * Returns the sum of empty cell distances from the top.
     */
    public static float sumEmptyCellDistanceFromTop(NextState nextState) {
        int sumDistance = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    sumDistance += top[j] - i;
                }
            }
        }
        return sumDistance;
    }

    /**
     * Returns the sum of hole distances from the top.
     */
    public static float sumHoleDistanceFromTop(NextState ns) {
        int total = 0;

        int field[][] = ns.field;
        int top[] = ns.top;
        for (int j = 0; j < State.COLS; ++j) {
            boolean last = false;
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    if (!last) {
                        total += top[j];
                    }
                    last = true;
                } else {
                    last = false;
                }
            }
        }
        return total;
    }

    /**
     * return the min max value of highest column height
     */
    public static float minMaximumColumnHeight(NextState nextState) {
        float worstPiece = Float.POSITIVE_INFINITY;
        for (int i = 0; i < State.N_PIECES; ++i) {
            float bestMove = Float.NEGATIVE_INFINITY;
            for (int[] j : NextState.legalMoves[i]) {
                NextState ns = NextState.generate(nextState, i, j);
                float maxColumnHeight = maxHeight(ns);
                if (maxColumnHeight > bestMove) {
                    bestMove = maxColumnHeight;
                }
            }
            if (bestMove < worstPiece) {
                worstPiece = bestMove;
            }
        }
        return worstPiece;
    }

    /**
     * return the min max value of the total number of holes
     */
    public static float minMaxTotalHoles(NextState nextState) {
        float worstPiece = Float.POSITIVE_INFINITY;
        for (int i = 0; i < State.N_PIECES; ++i) {
            float bestMove = Float.NEGATIVE_INFINITY;
            for (int[] j : NextState.legalMoves[i]) {
                NextState ns = NextState.generate(nextState, i, j);
                float maxColumnHeight = numEmptyCells(ns);
                if (maxColumnHeight > bestMove) {
                    bestMove = maxColumnHeight;
                }
            }
            if (bestMove < worstPiece) {
                worstPiece = bestMove;
            }
        }
        return worstPiece;
    }

    /**
     * Counts the number of holes and pits by comparing with the heights of the neighbouring columns.
     * Only gives score to columns and pits of height at least 3.
     * Examples:
     * 
     * |_|_|@|@|                   |@|_|_|_|
     * |@|@|@|@|                   |@|_|_|@|
     * |@|@|_|@| <-- Hole Column   |@|@|_|@|
     * |@|@|_|@|     Height 4      |@|@|_|@|  <--- Pit Column
     * |@|@|_|@|                   |@|@|_|@|       Height 5
     * |@|@|_|@|                   |@|@|_|@|
     * |@|@|@|@|                   |@|@|@|@|
     * 
     * Columns are given quadratically increasing scores according to the height.
     *   Height   Score
     *     1        0
     *     2        0
     *     3        1
     *     4        3
     *     5        6
     *     6       10
     */
    public static float holeAndPitColumns(NextState ns) {
        // int maxHeight = maxHeight(ns);
        int total = 0;

        int field[][] = ns.field;
        int top[] = ns.top;
        for (int j = 0; j < State.COLS; ++j) {
            int count = 0;
            int height = top[j];
            if (j > 0 && top[j - 1] > height)
                height = top[j - 1];
            if (j + 1 < State.COLS && top[j + 1] < height)
                height = top[j + 1];

            for (int i = 0; i < height; ++i) {
                if (field[i][j] == 0) {
                    ++count;
                    if (count > 2) {
                        total += count - 2;
                    }
                } else {
                    count = 0;
                }
            }
        }
        return total;
    }
    
    /**
     * Counts the number of holes and pits by comparing with the heights of the neighbouring columns, whichever is minimum.
     * Only gives score to columns and pits of height at least 3.
     * Examples:
     * 
     * |_|_|@|@|                   |@|_|_|_|
     * |@|@|@|@|                   |@|_|_|@|
     * |@|@|_|@| <-- Hole Column   |@|@|_|@|
     * |@|@|_|@|     Height 4      |@|@|_|@|  <--- Pit Column
     * |@|@|_|@|                   |@|@|_|@|       Height 5
     * |@|@|_|@|                   |@|@|_|@|
     * |@|@|@|@|                   |@|@|@|@|
     * 
     * Columns are given quadratically increasing scores according to the height.
     *   Height   Score
     *     1        0
     *     2        0
     *     3        1
     *     4        3
     *     5        6
     *     6       10
     */
    public static float holeAndPitColumnsMin(NextState ns) {
        // int maxHeight = maxHeight(ns);
        int total = 0;

        int field[][] = ns.field;
        int top[] = ns.top;
        for (int j = 0; j < State.COLS; ++j) {
            int count = 0;
            int height = Integer.MAX_VALUE;
            if (j > 0 && top[j - 1] > height)
                height = Math.min(height,top[j - 1]);
            if (j + 1 < State.COLS && top[j + 1] < height)
                height = Math.min(height,top[j + 1]);
            if (height == Integer.MAX_VALUE) {
                height = top[j];
            } else {
                height = Math.max(height,top[j]);
            }

            for (int i = 0; i < height; ++i) {
                if (field[i][j] == 0) {
                    ++count;
                    if (count > 2) {
                        total += count - 2;
                    }
                } else {
                    count = 0;
                }
            }
        }
        return total;
    }
    
    /**
     * Returns the number of columns that has at least one empty cell.
     * Minimize this value.
     */
    public static float numColumnsThatHasHole(NextState ns) {
        int total = 0;
        int field[][] = ns.field;
        int top[] = ns.top;
        
        for (int col=0; col<State.COLS ; col++) {
            for (int row=0; row<top[col]-1; row++) {
                if (field[row][col] == 0) {
                    total++;
                    break;
                }
            }
        }
        return total;
    }

    /**
     * Returns the number of rows that has at least one empty cell. 
     * Minimize this value.
     */
    public static float numRowsThatHasHole(NextState ns) {
        int total = 0;
        int field[][] = ns.field;
        int top[] = ns.top;
        
        for (int row=0; row<State.ROWS ; row++) {
            for(int col=0; col<State.COLS; col++) {
                if(row >= top[col]) {
                    continue;
                }
                
                if(field[row][col] == 0) {
                    total++;
                    break;
                }
            }
        }
        return total;
    }

    /**
     * Returns the number of rows that has more than one empty cell.
     * Minimize this value.
     */
    public static float numRowsWithMoreThanOneEmptyCell(NextState ns) {
        int total = 0;
        int field[][] = ns.field;
        int top[] = ns.top;
        
        for (int row=0; row<State.ROWS ; row++) {
            int numHoles = 0;
            for(int col=0; col<State.COLS; col++) {
                if(row >= top[col]) {
                    continue;
                }
                
                if(field[row][col] == 0) {
                    ++numHoles;
                    break;
                }
            }
            if (numHoles > 1) {
                ++total;
            }
        }
        return total;
    }

    /**
     * For every holes in position (i,j) and blocks in position (k,j) where k>=i
     * Then we add rowScore[k] to the result
     * for all holes length x at row-k, add ceil(x/2) to rowScore[k] 
     */
    public static float holeCoverEmptyCells(NextState ns) {
        int result = 0;
        int field[][] = ns.field;
        int top[] = ns.top;
        
        int rowScore[] = new int[State.ROWS];
        
        for (int i = 0; i < State.ROWS; ++i) {
            rowScore[i] = 0;
            int consecutiveHoles = 0;
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] == 0) {
                    if (j == 0 || field[i][j-1] != 0) {
                        consecutiveHoles = 1;
                    } else {
                        ++consecutiveHoles;
                    }
                } else {
                    rowScore[i] += (consecutiveHoles + 1) / 2;
                }
            }
        }

        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    for (int k = i; k < top[j]; ++k) {
                        if (field[k][j] != 0) {
                            result += rowScore[k];
                        }
                    }
                }
            }
        }

        return result;
    }

    private static final float LOSE_SCORE = -9999999f;

    private static final float minimaxRec(NextState ns, Feature feature,
            float alpha, float beta, int depth) {
        if (ns.lost == true) {
            return LOSE_SCORE - depth;
        }
        if (depth <= 0) {
            return feature.compute(ns);
        }

        // MIN PLAYER
        for (int i : NextState.minimaxPieceOrdering) {
            // for (int i = 0; i < State.N_PIECES; ++i) {
            // MAX PLAYER
            float newAlpha = alpha;
            int[][] legalMoves = NextState.legalMoves[i];
            for (int j = 0; j < legalMoves.length; ++j) {
                NextState nns = NextState.generate(ns, i, legalMoves[j]);
                float score = minimaxRec(nns, feature, newAlpha, beta,
                        depth - 1);
                if (score > newAlpha) {
                    newAlpha = score;
                    if (newAlpha >= beta) {
                        break;
                    }
                }
            }
            if (newAlpha < beta) {
                beta = newAlpha;
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return beta;

    }

    public static Feature minimax(int depth, Feature feature) {
        return (NextState nextState) -> {
            if (nextState.lost == true) {
                return LOSE_SCORE - depth;
            }
            if (depth <= 0) {
                return feature.compute(nextState);
            }

            // MIN PLAYER
            float beta = Float.POSITIVE_INFINITY;
            for (int i : NextState.minimaxPieceOrdering) {
                // for (int i = 0; i < State.N_PIECES; ++i) {
                // MAX PLAYER
                float newAlpha = Float.NEGATIVE_INFINITY;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j = 0; j < legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState, i,
                            legalMoves[j]);
                    float score = minimaxRec(ns, feature, newAlpha, beta,
                            depth - 1);
                    if (score > newAlpha) {
                        newAlpha = score;
                        if (newAlpha >= beta) {
                            break;
                        }
                    }
                }
                if (newAlpha < beta) {
                    beta = newAlpha;
                }
            }
            return beta;
        };
    }

    public interface IntegerFeature {
        int compute(NextState n);
    }

    private static final int LOSE_SCORE_INT = -9999999;

    private static final int minimaxRecInt(NextState ns,
            IntegerFeature feature, int alpha, int beta, int depth) {
        if (ns.lost == true) {
            return LOSE_SCORE_INT - depth;
        }
        if (depth <= 0) {
            return feature.compute(ns);
        }

        // MIN PLAYER
        for (int i : NextState.minimaxPieceOrdering) {
            // for (int i = 0; i < State.N_PIECES; ++i) {
            // MAX PLAYER
            int newAlpha = alpha;
            int[][] legalMoves = NextState.legalMoves[i];
            for (int j = 0; j < legalMoves.length; ++j) {
                NextState nns = NextState.generate(ns, i, legalMoves[j]);
                int score = minimaxRecInt(nns, feature, newAlpha, beta,
                        depth - 1);
                if (score > newAlpha) {
                    newAlpha = score;
                    if (newAlpha >= beta) {
                        break;
                    }
                }
            }
            if (newAlpha < beta) {
                beta = newAlpha;
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return beta;

    }

    /**
     * Minimax that uses an integer feature instead of a Feature.
     */
    public static Feature minimaxInt(int depth, IntegerFeature feature) {
        return (NextState nextState) -> {
            if (nextState.lost == true) {
                return LOSE_SCORE - depth;
            }
            if (depth <= 0) {
                return feature.compute(nextState);
            }

            // MIN PLAYER
            int beta = Integer.MAX_VALUE;
            for (int i : NextState.minimaxPieceOrdering) {
                // for (int i = 0; i < State.N_PIECES; ++i) {
                // MAX PLAYER
                int newAlpha = Integer.MIN_VALUE;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j = 0; j < legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState, i,
                            legalMoves[j]);
                    int score = minimaxRecInt(ns, feature, newAlpha, beta,
                            depth - 1);
                    if (score > newAlpha) {
                        newAlpha = score;
                        if (newAlpha >= beta) {
                            break;
                        }
                    }
                }
                if (newAlpha < beta) {
                    beta = newAlpha;
                }
            }
            return beta;
        };
    }

    /**
     * A minimax that deepens the search tree as you get closer to dying. (Based on heightTransform(height))
     */
    public static Feature variableHeightMinimaxInt(FunctionInt heightTransform,
            IntegerFeature feature) {
        Feature[] minimaxes = new Feature[] { minimaxInt(6, feature),
                minimaxInt(4, feature), minimaxInt(3, feature) };
        return (NextState ns) -> {
            int choice = heightTransform.apply(Math.round(maxHeight(ns)));
            // System.out.println(height(ns) + "|" + choice);
            if (choice >= minimaxes.length) {
                return 0;
            }
            if (choice >= minimaxes.length)
                choice = minimaxes.length - 1;
            return minimaxes[choice].compute(ns);
        };
    }

    public interface FunctionInt {
        public int apply(int input);
    }

    /**
     * We divide the playing field vertically into "height regions" of height = regionHeight each.
     * This feature returns which region it is in.
     * e.g. regionHeight = 6:
     * 
     * match height with:
     *  | 0 => return 0
     *  | 1 to 6 => return 1
     *  | 7 - 12 => return 2
     *  | 13 - 18 => return 3
     *  |...
     */
    public static IntegerFeature negHeightRegion(int regionHeight) {
        return (NextState nextState) -> {
            int maximumHeight = Integer.MIN_VALUE;
            int top[] = nextState.top;
            for (int x : top) {
                if (x > maximumHeight) {
                    maximumHeight = x;
                }
            }

            int heightRegion = ((maximumHeight - 1) / regionHeight) + 1;
            return -heightRegion;
        };
    }
    
    /**
     * Returns the number of filled cells weighted by height
     */
    public static float weightedFilledCells(NextState nextState) {
        int weightedFilledCells = 0;
        int field[][] = nextState.field;

        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] != 0) {
                    weightedFilledCells += i;
                }
            }
        }

        return weightedFilledCells;
    }
    
    /**
     * Returns the number of holes.
     * A row transition occurs when an empty cell is adjacent to a filled cell on the same row and vice versa
     */
    public static float rowTransitions(NextState nextState) {
        int rt = 0;

        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS-1; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                   rt++;
                }
            }
        }

        return rt;
    }

     /**
     * Returns the number of column transitions.
     * A column transition occurs when an empty cell is adjacent to a filled cell on the same column and vice versa
     */
    public static float colTransitions(NextState nextState) {
        int ct = 0;

        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS-1; j++) {
                if (field[i][j] == 0 && field[i][j + 1] != 0) {
                   ct++;
                }
            }
        }

        return ct;
    }
}


class FeatureFunctions2 {
    /**
     * Return weighted filled cells. cells at row-i costs i.
     */
    public static float weightedFilledCells(NextState nextState) {
        int filledCells = 0;
        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] != 0) {
                    filledCells += (i + 1);
                }
            }
        }
        return filledCells;
    }
    
    /**
     *  The depth of the deepest hole (a width-1 hole with filled spots on both sides)
     */
    public static float deepestOneHole(NextState nextState) {
        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                int left = (j - 1 < 0 ? 1 : field[i][j-1]);
                int right = (j + 1 >= State.COLS ? 1 : field[i][j+1]);
                if (field[i][j] == 0 && left == 1 && right == 1) {
                    return State.ROWS - i;
                }
            }
        }
        return 0;
    }
    
    /**
     *  The total depth of all the holes on the game board
     */
    public static float sumOfAllHoles(NextState nextState) {
        int holesSum = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    holesSum += (State.ROWS - i);
                }
            }
        }
        return holesSum;
    }
    
    /**
     * Horizontal Roughness - The number of times a spot alternates between an empty and a filled status, going by rows
     */
    public static float horizontalRoughness(NextState nextState) {
        int horizontalRoughness = 0;
        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 1; j < State.COLS; ++j) {
                if (field[i][j] != field[i][j-1]) {
                    ++horizontalRoughness;
                }
            }
        }
        return horizontalRoughness;
    }
    
    /**
     * Vertical Roughness - The number of times a spot alternates between an empty and a filled status, going by columns
     */
    public static float verticalRoughness(NextState nextState) {
        int verticalRoughness = 0;
        int field[][] = nextState.field;
        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 1; i < State.ROWS; ++i) {
                if (field[i][j] != field[i-1][j]) {
                    ++verticalRoughness;
                }
            }
        }
        return verticalRoughness;
    }
    
    /**
     * The number of holes that are 3 or more blocks deep
     */
    public static float wellCount(NextState nextState) {
        int numberOfWells = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i + 2 < top[j]; ++i) {
                if (field[i][j] == 0 && (i == 0 || field[i-1][j] == 1)) {
                    if (field[i+1][j] == 0 && field[i+2][j] == 0) {
                        ++numberOfWells;
                    }
                }
            }
        }
        return numberOfWells;
    }
    
    /**
     * Weighted empty cells
     */
    public static float weightedEmptyCells(NextState nextState) {
        int emptyCells = 0;
        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; ++j) {
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    emptyCells += State.ROWS - i;
                }
            }
        }
        return emptyCells;
    }
    
    /**
     * The height of the highest hole on the board
     */
    public static float highestHole(NextState nextState) {
        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int i = State.ROWS - 1; i >= 0; --i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] == 0 && i < top[j]) {
                    return i;
                }
            }
        }
        return 0;
    }
    
    /**
     * Number of rows cleared by current move
     */
    public static float clearLines(NextState nextState) {
        return nextState.rowsCleared;
    }
    
    /**
     * (h2 – h1) + |h2 – h3| + |h3 – h4| + |h4 – h5| + |h5 – h6| + |h6 – h7| + |h7 – h8| + |h8 – h9| + (h9 – h10)
     */
    public static float surfaceSmoothness(NextState nextState) {
        int surfaceSmoothness = 0;
        int top[] = nextState.top;
        surfaceSmoothness += top[1] - top[0];
        surfaceSmoothness += top[State.COLS-1] - top[State.COLS-2];
        for (int i = 1; i < State.COLS - 2; ++i) {
            surfaceSmoothness += Math.abs(top[i] - top[i+1]);
        }
        return surfaceSmoothness;
    }
    
    /**
     * for every well of depth w, sum 1 + 2 + 3 + ... + w
     */
    public static float sumSquareWells(NextState nextState) {
        int sum = 0;
        int top[] = nextState.top;
        int field[][] = nextState.field;
        for (int j = 0; j < State.COLS; ++j) {
            int wellDepth = 0;
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    ++wellDepth;
                    sum += wellDepth;
                } else {
                    wellDepth = 0;
                }
            }
        }
        return sum;
    }
    
    /**
     * compute the lowest and highest position of new piece. compute the total
     */
    public static float totalHeightNewPiece(NextState nextState) {
        int fieldBeforeCleared[][] = nextState.fieldBeforeCleared;
        if (nextState.lost) {
        	return State.ROWS;
        }
        int minHeightPiece = Integer.MAX_VALUE;
        int maxHeightPiece = Integer.MIN_VALUE;
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (fieldBeforeCleared[i][j] == nextState.turn) {
                    minHeightPiece = Math.min(minHeightPiece,i);
                    maxHeightPiece = Math.max(maxHeightPiece,i);
                }
            }
        }
        return maxHeightPiece + minHeightPiece;
    }
    
    /**
     * return the landing height of the new piece
     */
    public static float landingHeight(NextState nextState) {
    	int fieldBeforeCleared[][] = nextState.fieldBeforeCleared;
    	if (nextState.lost) {
        	return State.ROWS;
        }
    	for (int i = 0; i < State.ROWS; ++i) {
    		for (int j = 0; j < State.COLS; ++j) {
    			if (fieldBeforeCleared[i][j] == nextState.turn) {
    				return i;
    			}
    		}
    	}
    	return 0;
    }
}


class NextState {
    
    public final int[][] field;
    public final int[] top;
    public final boolean lost;
    public final int cleared;
    public final int turn;
    public final int rowsCleared;
    public final int[][] fieldBeforeCleared;

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
    private NextState(int[][] field, int[] top, boolean lost, int cleared, int turn, int rowsCleared, int[][] beforeCleared) {
        this.field = field;
        this.top = top;
        this.lost = lost;
        this.cleared = cleared;
        this.turn = turn;
        this.rowsCleared = rowsCleared;
        this.fieldBeforeCleared = beforeCleared;
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
            return new NextState(field, top, lost, cleared, turn, 0, field);
        }

        
        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
            
            //from bottom to top of brick
            for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
                field[h][i+slot] = turn;
            }
        }
        
        int[][] fieldBeforeCleared = null;
        
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
                if (fieldBeforeCleared == null) { // make a copy of field.
                    fieldBeforeCleared = new int[ROWS][COLS];
                    for (int y=0; y<field.length; ++y) {
                        fieldBeforeCleared[y] = Arrays.copyOf(field[y], field[y].length);
                    }
                }
                
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
        
        if (fieldBeforeCleared == null) {
            fieldBeforeCleared = field;
        }
    
        return new NextState(field, top, lost, cleared, turn, rowsCleared, fieldBeforeCleared);
    }
    
}


class PredeterminedState extends State {
    private final int[] pieces;
    private int pieceIndex;
    
    private static Random rand = new Random();
    
    public PredeterminedState(int[] pieces) {
        this.pieces = pieces;
        pieceIndex = 0;
        if (pieceIndex >= pieces.length) {
            //nextPiece = randomPiece();
        } else {
            nextPiece = pieces[pieceIndex++];
        }
    }
    
    private int randomPiece() {
        return (int)(Math.random()*N_PIECES);
    }
    
    public void makeMove(int[] move) {
        super.makeMove(move);
        if (pieceIndex >= pieces.length) {
            //nextPiece = randomPiece();
        } else {
            nextPiece = pieces[pieceIndex++];
        }
    }
}


class ResultDump {

    public static final String PATH = "ResultDump/";
    public static final String RESULT_PREFIX = "result";
    
    public static final Random rand = new Random();
    

    private static String randomResultFile() {
        int randFileNo = rand.nextInt(2147483647);
        return RESULT_PREFIX + randFileNo + ".txt";
    }

    public static void saveResults(String results) {
        makeDirs(PATH);
        String auxiliaryFile = randomResultFile();
        System.out.println("Saving results to " + auxiliaryFile);

        try {
            PrintWriter pw = new PrintWriter(PATH + auxiliaryFile);
            pw.print(results);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        

    private static boolean makeDirs(String path) {
        return (new File(path)).mkdirs();
    }
}


class Sequence implements Comparable<Sequence> {
    private static final int SHORT_LIMIT = 30;
    
    public final int score;
    public final int[] pieces;
    
    public Sequence(int gameScore, int[] pieces) {
        this.score = computeScore(gameScore, pieces.length);
        this.pieces = pieces;
    }
    
    public Sequence(int gameScore, ArrayList<Integer> pieceList) {
        this.score = computeScore(gameScore, pieceList.size());
        this.pieces = toArray(pieceList);
    }
    
    private int computeScore(int gameScore, int seqLength) {
        return gameScore / (int)(Math.sqrt(seqLength));
    }
    
    public Sequence(String encoded) {
        int space = encoded.indexOf(' ');
        score = Integer.parseInt(encoded.substring(0, space));

        ArrayList<Integer> pieceList = new ArrayList<>();
        for (int i = space+1; i < encoded.length(); ++i) {
            int piece = pieceNo(encoded.charAt(i));
            if (piece != -1) {
                pieceList.add(piece);
            }
        }

        pieces = toArray(pieceList);
    }
    
    private static int[] toArray(ArrayList<Integer> pieceList) {
        int[] pieces = new int[pieceList.size()];
        for (int i=0; i<pieces.length; ++i) {
            pieces[i] = pieceList.get(i);
        }
        return pieces;
    }
    
    private static int pieceNo(char c) {
        int piece =  c - '0';
        if (piece >= 0 && piece < State.N_PIECES) {
            return piece;
        } else {
            return -1;
        }
    }

    @Override
    /**
     * Higher Score first (left)
     * Higher pieces length last (right)
     */
    public int compareTo(Sequence o) {
        int cmp = o.score - score; // reverse comparator
        if (cmp != 0) return cmp;
        
        cmp = pieces.length - o.pieces.length;
        if (cmp != 0) return cmp;
        
        // same length.
        for (int i=0; i<pieces.length; ++i) {
            cmp = pieces[i] - o.pieces[i];
            if (cmp != 0) return cmp;
        }
        return 0;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(pieces);
        result = prime * result + score;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sequence other = (Sequence) obj;
        if (!Arrays.equals(pieces, other.pieces))
            return false;
        if (score != other.score)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(score);
        sb.append(" ");
        for (int piece : pieces) {
            sb.append(piece);
        }
        return sb.toString();
    }
    
    public String toStringShort() {
        String s = toString();
        if (s.length() > SHORT_LIMIT) {
            return s.substring(0, SHORT_LIMIT) + "...";
        }
        return s;
    }
    
}

class SequenceStore {
    
    public static final String SEPARATOR_ROW = "\n";
    
    public static final String PATH = "SequenceData/";
    public static final String FILE_MAIN = "main.txt";
    public static final String FILE_MAIN_TRIM = "main_trim.txt";
    public static final String FILE_AUX_PREFIX = "aux";
    public static final Random rand = new Random();
    public final String auxiliaryFile = randomAuxiliaryFile();

    private final TreeSet<Sequence> sequenceSet;
    private final int SEQUENCE_LIMIT = 500;
    
    private double[] probabilities = new double[0];
    private double probabilitySum;
    float lastHardBias = -1;
    private boolean sequenceSetChanged;
    
    private SequenceStore() {
        sequenceSet = new TreeSet<>();
        dirty();
    }
    
    private void dirty() {
        sequenceSetChanged = true;
    }
    
    public static SequenceStore empty() {
        return new SequenceStore();
    }
    
    public static SequenceStore load() {
        SequenceStore store = new SequenceStore();
        store.loadSequences(false);
        return store;
    }
    
    public static SequenceStore loadTrimmed() {
        SequenceStore store = new SequenceStore();
        store.loadSequences(true);
        return store;
    }
    
    private static String randomAuxiliaryFile() {
        int randFileNo = rand.nextInt(2147483647);
        return FILE_AUX_PREFIX + randFileNo + ".txt";
    }
    
    public void addSequence(int gameScore, int[] pieces) {
        addSequence(new Sequence(gameScore, pieces));
    }
    
    public void addSequence(int gameScore, ArrayList<Integer> pieces) {
        addSequence(new Sequence(gameScore, pieces));
    }
    
    public void addSequence(Sequence sequence) {
        sequenceSet.add(sequence);
        trimExcessSequences();
        dirty();
    }
    
    private void trimExcessSequences() {
        while (sequenceSet.size() > SEQUENCE_LIMIT) {
            sequenceSet.pollLast();
        }
    }
    
    public Sequence getRandomSequence(float hardBias) {
        if (sequenceSet.isEmpty()) return null;
        
        regenerateProbabilityArray(hardBias);
        
        double pos = rand.nextDouble()*probabilitySum;
        Iterator<Sequence> itr = sequenceSet.iterator();
        int maxIndex = probabilities.length-1;
        
        int index = 0;
        while (index < maxIndex && pos >= probabilities[index]) {
            pos -= probabilities[index];
            index++;
            itr.next();
        }
        return itr.next();
    }

    public void regenerateProbabilityArray(float hardBias) {
        if (!sequenceSetChanged && hardBias == lastHardBias) return;

        probabilitySum = 0;
        int size = sequenceSet.size();
        if (probabilities.length != size) {
            probabilities = new double[size];
        }
        
        for (int i=0; i<probabilities.length; ++i) {
            double prob = probability(hardBias, (float)i/size);
            probabilities[i] = prob;
            probabilitySum += prob;
        }
        
        lastHardBias = hardBias;
        sequenceSetChanged = false;
    }
    
    /**
     * x = 0 is the probability of the first element in the array
     * x = 1 is the probability of the last element in the array
     * 
     * y = e^(-(1/(1-a)-1)*(x)^2)
     * => y = e^(-a/(1-a)) * (x)^2
     * 
     * a = hardBias
     * x = position
     */
    public double probability(float a, float x) {
        return Math.exp(-a/(1-a)*x*x);
    }
    
    
    public void saveSequences() {
        System.out.println("Saving sequences to " + auxiliaryFile);

        String backupFile = PATH + auxiliaryFile + ".backup";
        
        try {
            PrintWriter pw = new PrintWriter(backupFile);
            for (Sequence seq : sequenceSet) {
                pw.print(seq);
                pw.print(SEPARATOR_ROW);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter pw = new PrintWriter(PATH + auxiliaryFile);
            for (Sequence seq : sequenceSet) {
                pw.print(seq);
                pw.print(SEPARATOR_ROW);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File file = new File(backupFile);
        file.delete();
    }
    
    
    public void loadSequences(boolean trimmed) {
        sequenceSet.clear();
        loadSequencesWithoutClearing(trimmed);
    }
    
    public void loadSequencesWithoutClearing(boolean trimmed) {
        dirty();
        String fileName;
        if (trimmed) fileName = PATH + FILE_MAIN_TRIM;
        else fileName = PATH + FILE_MAIN;
        
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();
            while (s != null) {
                sequenceSet.add(new Sequence(s));
                s = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
        
    public static void combine() {
        TreeSet<TempSequence> lines = new TreeSet<>();
        
        File dir = new File(PATH);
        File[] files = dir.listFiles((file, name) -> name.endsWith(".txt"));
        for (File file : files) {
            try {
                System.out.println("Read file " + file.getName());
                FileReader fr = new FileReader(file.getPath());
                BufferedReader br = new BufferedReader(fr);
                String s = br.readLine();
                while (s != null) {
                    lines.add(new TempSequence(s));
                    s = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            PrintWriter pw = new PrintWriter(PATH + FILE_MAIN);
            for (TempSequence line : lines) {
                pw.print(line);
                pw.print(SEPARATOR_ROW);
            }
            pw.close();
            System.out.println("Written to file " + FILE_MAIN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter pw = new PrintWriter(PATH + FILE_MAIN_TRIM);
            int trimLimit = 200;
            for (TempSequence line : lines) {
                pw.print(line);
                pw.print(SEPARATOR_ROW);
                
                if (--trimLimit <= 0) break;
            }
            pw.close();
            System.out.println("Written to file " + FILE_MAIN_TRIM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (File file : files) {
            String getName = file.getName();
            if (!getName.equals(FILE_MAIN) && !getName.equals(FILE_MAIN_TRIM)) {
                System.out.println("Delete file " + file.getName());
                file.delete();
            }
        }
    }
    
    @Override
    public String toString() {
        String line1 = "SequenceStore: save to file " + auxiliaryFile + " | " +
                    sequenceSet.size() + " sequences loaded from main.";
        String line2 = "First item : ";
        if (sequenceSet.isEmpty()) line2 += "null";
        else line2 += sequenceSet.first().toStringShort();

        return line1 + "\n" + line2;
    }
    
    private static boolean makeDirs(String path) {
        return (new File(path)).mkdirs();
    }
    
    
    /**
     * Run this to combine everything into main.txt
     */
    public static void main(String[] args) {
        SequenceStore.combine();
    }

    
    /**
     * Dangerous. Do not run. Adds fake data to database.
     */
    public static void test2() {
        if ("".length()==0) throw new UnsupportedOperationException("DO NOT RUN THIS"); // Guard against stupid programmers who run this function.
        
        SequenceStore store = SequenceStore.load();
        System.out.println(store.sequenceSet);
        store.saveSequences();
        store = SequenceStore.load();
        System.out.println(store.sequenceSet);
        store.saveSequences();
    }
    
    /**
     * Dangerous. Do not run. Adds fake data to database.
     */
    public static void test1() {
        if ("".length()==0) throw new UnsupportedOperationException("DO NOT RUN THIS"); // Guard against stupid programmers who run this function.
        
        int[] pieces1 = new int[]{0,1,2,4,1,0,5,6,0,6,3};
        int[] pieces2 = new int[]{0,1,2,5,6,0,3,4,1,0,5,6,0,6,3};
        int[] pieces3 = new int[]{0,1,2,2,1,1,5,0,6,0,3,4,1,0,5,6,0,3};
        int[] pieces4 = new int[]{1};
        int score1 = 01241;
        int score2 = 0;
        int score3 = 341849468;
        int score4 = 381431;
        
        SequenceStore store = SequenceStore.empty();
        store.addSequence(score1, pieces1);
        store.addSequence(score2, pieces2);
        store.addSequence(score3, pieces3);
        store.addSequence(score4, pieces4);
        store.addSequence(score2, pieces1);
        store.addSequence(score3, pieces2);
        store.addSequence(score4, pieces3);
        
        store.saveSequences();
    }
    
    
}


class TempSequence implements Comparable<TempSequence> {
    public final int score;
    public final String seqStr;
    
    public TempSequence(String encoded) {
        int space = encoded.indexOf(' ');
        score = Integer.parseInt(encoded.substring(0, space));
        seqStr = encoded.substring(space+1);
    }

    @Override
    public int compareTo(TempSequence o) {
        int cmp = o.score - score;
        if (cmp != 0) return cmp;
        
        cmp = seqStr.length() - o.seqStr.length();
        if (cmp != 0) return cmp;
        
        return seqStr.compareTo(o.seqStr);
    }
    
    @Override
    public String toString() {
        return score + " " + seqStr;
    }
}

class BlahPlayer extends WeightedHeuristicPlayer {

    protected void configure() {
        features = new Feature[]{
                (n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.maxHeight(n),
                (n)->FeatureFunctions.numEmptyCells(n),
                (n)->FeatureFunctions.sumHeight(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.numRowsCleared(n),
                (n)->FeatureFunctions.numFilledCells(n)
                //nextStateFeatureFunction(n)->FeatureFunctions.totalFilledCells(n)),
                //good
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        weights[0] = -99999.0f;
        weights[1] = -1.0f;
        weights[2] = -50.0f;
        weights[3] = -30.0f;
        weights[4] = -50.0f;
        weights[5] = 50.0f;
        weights[6] = -25.0f;
    }
    
    private static Feature nextStateFeatureFunction(Feature feature) {
        
        return (nextState) -> {
            int[] action = new int[]{0,3};
            NextState nextNextState = nextState.generate(nextState, 0, action);
            NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
            return feature.compute(nextNextNextState);
        };
    }

    private static Feature good = (nextState) -> {

        int[] action = new int[]{0,3};
        NextState nextNextState = nextState.generate(nextState, 0, action);
        NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
        return 4;
        
    };
    
    private static float good(NextState nextState) {
        
        
        int[] action = new int[]{0,3};
        NextState nextNextState = nextState.generate(nextState, 0, action);
        NextState nextNextNextState = nextNextState.generate(nextNextState, 0, action);
        return 4;
    }


    public static void main(String[] args) {
        BlahPlayer p = new BlahPlayer();
        SmoothingAdjuster adj = new SmoothingAdjuster(p.dim());
        
        adj.fixValue(0, -99999f);
        learn(p, adj);
    }
    
}


interface Feature {
    float compute(NextState n);
}


class JonathanPlayer extends WeightedHeuristicPlayer {


    protected void configure() {
        features = new Feature[]{
                        (n)->FeatureFunctions.lost(n),
                        (n)->FeatureFunctions.numFilledCells(n),
                        (n)->FeatureFunctions2.weightedFilledCells(n),
                        (n)->FeatureFunctions.maxHeight(n),
                        (n)->FeatureFunctions.numEmptyCells(n),
                        (n)->FeatureFunctions2.clearLines(n),
                        (n)->FeatureFunctions.maxHeightDifference(n),
                        (n)->FeatureFunctions2.deepestOneHole(n),
                        (n)->FeatureFunctions2.sumOfAllHoles(n),
                        (n)->FeatureFunctions2.horizontalRoughness(n),
                        (n)->FeatureFunctions2.verticalRoughness(n),
                        (n)->FeatureFunctions2.wellCount(n),
                        (n)->FeatureFunctions2.weightedEmptyCells(n),
                        (n)->FeatureFunctions2.highestHole(n),
                        (n)->FeatureFunctions2.surfaceSmoothness(n),
                        (n)->FeatureFunctions2.sumSquareWells(n),
                        (n)->FeatureFunctions2.totalHeightNewPiece(n),
                        (n)->FeatureFunctions2.landingHeight(n),
                        
                        (n)->FeatureFunctions.bumpiness(n),
                        (n)->FeatureFunctions.topPerimeter(n),
                        (n)->FeatureFunctions.holeAndPitColumns(n),
                        (n)->FeatureFunctions.holeAndPitColumnsMin(n),
        };
    }
    
    protected void initialiseWeights() {
        weights = new float[features.length];
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -5, -30, 0};
        //weights = new float[]{-99999.0f, -12, -30, 20, -2, -26, 0, -15, -5, -10};
        //weights = new float[]{-99999.0f, 299.2644f, -241.19064f, -269.57117f, -271.10962f, -175.32292f, 149.07938f, -269.22433f, -184.39595f, -274.9451f, -233.19809f, -293.61053f};
        //weights = new float[]{-9999999.0f, -40.0f, -500.0f, -0.01f, -40.0f, 20.0f, 1.0f, 0.01f, -20.0f, 3.0f, -500.0f, -40.0f, 0.05f, -40.0f, -100.0f, 0f};
        
        //weights = new float[]{-99999.0f, -0.0f, -72.27131f, -0.39263827f, -18.150364f, 1.9908575f, -4.523054f, 2.6717715f}; // <-- good weights.
        //weights = new float[]{-99999.0f, -0.0f, -80.05821f, 0.2864133f, -16.635815f, -0.0488357f, -2.9707198f, -1f, -1f, -1f}; // <-- good weights.
        //weights = new float[]{-99999.0f, -1, -4, -95};
    }

    /**
     * return the min max value of feature
     */
    public static Feature minMax(Feature feature) {
        return (NextState nextState) -> {
            float worstPiece = Float.POSITIVE_INFINITY;
            for (int i = 0; i < State.N_PIECES; ++i) {
                float bestMove = Float.NEGATIVE_INFINITY;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j=0; j<legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState,i,legalMoves[j]);
                    float score = feature.compute(ns);
                    if (score > bestMove) {
                        bestMove = score;
                        if (bestMove >= worstPiece) {
                            j = legalMoves.length; // break;
                            //break;
                        }
                    }
                }
                if (bestMove < worstPiece) {
                    worstPiece = bestMove;
                }
            }
            return worstPiece;
        };
    }


    
    public static void main(String[] args) {
        int choice = 1; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new JonathanPlayer();
        //WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        GeneticAlgorithmSD adjuster = new GeneticAlgorithmSD(p, p.dim(), 20);
        adjuster.fixValue(0, -9999999f);
        /*adjuster.fixSign(0,-1);
        adjuster.fixSign(1,-1);
        adjuster.fixSign(2,-1);
        //adjuster.fixSign(3,-1);
        adjuster.fixSign(4,+1);
        adjuster.fixSign(5,-1);
        adjuster.fixSign(6,-1);
        adjuster.fixSign(7,-1);
        adjuster.fixSign(8,-1);
        //adjuster.fixSign(9,-1);
        //adjuster.fixSign(10,-1);
        adjuster.fixSign(11,-1);*/
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
        
        //p.configure();
        //p.switchToMinimax(2);
        int[] sequence = new int[]{6, 2, 3, 2, 6, 3, 6, 3, 3, 3, 4, 1, 6, 2, 3, 2, 5, 0, 6, 4, 3, 5, 2, 5, 2, 4, 3, 6, 5, 0, 4, 2, 2, 5, 0, 0, 4, 6, 0, 0, 6, 6, 6, 4, 6, 4, 4, 3, 3, 0, 5, 3, 6, 0, 3, 2, 4, 1, 4, 3, 2, 6, 1, 5, 3, 2, 2, 2, 6, 6, 4, 5, 6, 1, 4, 4, 4, 0, 6, 6, 1, 1, 6, 1, 5, 3, 5, 0, 3, 0, 5, 6, 2, 4, 4, 0, 5, 0, 2, 6, 0, 6, 2, 2, 0, 1, 3, 0, 3, 1, 1, 5, 5, 5, 5, 2};
        switch(choice) {
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                learn(adjuster);break;
            case 2:
                record(p); break;
            case 3:
                watchWithPredeterminedState(p,sequence); break;
        }
    }
}


public class OhPlayer extends WeightedHeuristicPlayer {

    protected void configure() {
        features = new Feature[]{
                (n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.sumHeight(n), //
                // (n)->FeatureFunctions.numRowsCleared(n), //
                //(n)->FeatureFunctions.maxHeightDifference(n), //
                (n)->FeatureFunctions.numHoles(n),
                (n)->FeatureFunctions.sumHoleDistanceFromTop(n),
                (n)->FeatureFunctions.holeAndPitColumns(n),
                (n)->FeatureFunctions.holeAndPitColumnsMin(n),
                (n)->FeatureFunctions.topPerimeter(n),
                //(n)->FeatureFunctions.maxHeight(n),     //
                //(n)->FeatureFunctions.maxHeightPow(n, 3), //
                (n)->FeatureFunctions.sumEmptyCellDistanceFromTop(n),
                //(n)->FeatureFunctions.numFilledCells(n), //
                //(n)->FeatureFunctions.numRowsWithMoreThanOneEmptyCell(n), ////
                //(n)->FeatureFunctions.holeCoverEmptyCells(n), ////
                (n)->FeatureFunctions2.weightedFilledCells(n), //////
                (n)->FeatureFunctions2.deepestOneHole(n),
                (n)->FeatureFunctions2.sumOfAllHoles(n),
                (n)->FeatureFunctions2.horizontalRoughness(n),
                (n)->FeatureFunctions2.verticalRoughness(n),
                (n)->FeatureFunctions2.wellCount(n),
                (n)->FeatureFunctions2.weightedEmptyCells(n),
                (n)->FeatureFunctions2.highestHole(n),
                (n)->FeatureFunctions2.surfaceSmoothness(n),
                //(n)->FeatureFunctions2.totalHeightNewPiece(n), // messes up sequence database
                (n)->FeatureFunctions2.sumSquareWells(n),
                (n)->FeatureFunctions2.landingHeight(n)
                

                /*(n)->FeatureFunctions.lost(n),
                (n)->FeatureFunctions.numRowsCleared(n),
                (n)->FeatureFunctions2.totalHeightNewPiece(n),
                (n)->FeatureFunctions.sumHeight(n),
                (n)->FeatureFunctions.sumHoleDistanceFromTop(n),
                (n)->FeatureFunctions.holeAndPitColumns(n),
                (n)->FeatureFunctions.holeAndPitColumnsMin(n),
                (n)->FeatureFunctions2.deepestOneHole(n),
                (n)->FeatureFunctions2.highestHole(n),
                (n)->FeatureFunctions2.wellCount(n),
                (n)->FeatureFunctions2.sumSquareWells(n),
                (n)->FeatureFunctions.numHoles(n),
                (n)->FeatureFunctions2.weightedEmptyCells(n),
                (n)->FeatureFunctions2.horizontalRoughness(n),
                (n)->FeatureFunctions2.verticalRoughness(n),
                (n)->FeatureFunctions.bumpiness(n),
                (n)->FeatureFunctions.topPerimeter(n)*/
                
        };
    }
    
    /**
     * Genetic Algorithm Results
     * Features: lost, bumpiness, sumHeight, numRowsCleared, maxHeightDifference,
     *           numHoles, sumHoleDistanceFromTop, holeAndPitColumns, topPerimeter
                (n) -> FeatureFunctions.lost(n),
                (n) -> FeatureFunctions.bumpiness(n),
                (n) -> FeatureFunctions.sumHeight(n),
                (n) -> FeatureFunctions.numRowsCleared(n),
                (n) -> FeatureFunctions.maxHeightDifference(n),
                (n) -> FeatureFunctions.numHoles(n),
                (n) -> FeatureFunctions.sumHoleDistanceFromTop(n),
                (n) -> FeatureFunctions.holeAndPitColumns(n),
                (n) -> FeatureFunctions.topPerimeter(n)
     *  Hi-Score: 1561.0 | [2.0, -5.0, -150.0, 0.1, 0.5, -2.0, -80.0, -40.0, -80.0]
     *  State #17. Score = 1236.5   [-0.02, -20.0, -150.0, -2.0, 2.0, -2.0, -10.0, -5.0, -20.0]
     *  Hi-Score: 2340.5 | [-80.0, -5.0, 0.01, 0.02, 0.05, -20.0, -5.0, -2.0, -5.0]
     *  Hi-Score: 2259.0 | [-2.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  Hi-Score: 2525.0 | [-2.0, -100.0, -2.0, -0.01, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  Hi-Score: 4496.5 | [-0.01, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -3.0]
     *  
     *  
     *  
                (n) -> FeatureFunctions.lost(n),
                (n) -> FeatureFunctions.bumpiness(n),
                (n) -> FeatureFunctions.sumHeight(n),
                (n) -> FeatureFunctions.numRowsCleared(n),
                (n) -> FeatureFunctions.maxHeightDifference(n),
                (n) -> FeatureFunctions.numHoles(n),
                (n) -> FeatureFunctions.sumHoleDistanceFromTop(n),
                (n) -> FeatureFunctions.holeAndPitColumns(n),
                (n) -> FeatureFunctions.holeAndPitColumnsMin(n),
                (n) -> FeatureFunctions.topPerimeter(n)
     *  Hi-Score: 8299.5 | [-99999.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, 0.01, 0.5]
     *  Hi-Score: 7358.25 | [-99999.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -0.1, 2.0]
     *  Score =   6870.500 [  -100.00,    -2.00,    -0.50,     5.00,  -500.00,   -40.00,   -70.00,     0.01,    -3.00]
     *  Hi-Score: 12067.75 | [-99999.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -0.1, -3.0]
     *  Hi-Score: 13924.0 | [-99999.0, -100.0, -2.0, -0.01, 5.0, -500.0, -40.0, -70.0, -40.0, -3.0]
     *  Hi-Score: 18838.75 | [-99999.0, -100.0, -2.0, -0.01, 5.0, -500.0, -40.0, -70.0, -40.0, -1.0]
     *  Hi-Score: 24683.75 | [-99999.0, -100.0, -2.0, -0.5, 5.0, -500.0, -40.0, -70.0, -40.0, -3.0]
     *
     *  new float[]{-99999.0f, -100.0f, -2.0f, -0.5f, 5.0f, -500.0f, -40.0f, -70.0f, -40.0f, -3.0f};
     */
    protected void initialiseWeights() {
        weights = new float[features.length];

        weights = new float[]{-9999999f, -1835f, 1886f, 1881f, -1353f, -1668f, -1782f, -693f, 1304f, -77f, 873f, -1623f, -1990f, -805f, 752f, 901f, -1421f, 632f, -7f, -10f};
    };
    
    /**
     * return the min max value of feature
     */
    public static Feature minMax(Feature feature) {
        return (NextState nextState) -> {
            float worstPiece = Float.POSITIVE_INFINITY;
            for (int i = 0; i < State.N_PIECES; ++i) {
                float bestMove = Float.NEGATIVE_INFINITY;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j=0; j<legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState,i,legalMoves[j]);
                    float score = feature.compute(ns);
                    if (score > bestMove) {
                        bestMove = score;
                        if (bestMove >= worstPiece) {
                            j = legalMoves.length; // break;
                            //break;
                        }
                    }
                }
                if (bestMove < worstPiece) {
                    worstPiece = bestMove;
                }
            }
            return worstPiece;
        };
    }


    
    public static void main(String[] args) {
        int choice = 1; // 0 to watch, 1 to learn.

        WeightedHeuristicPlayer p = new OhPlayer();
        //WeightAdjuster adjuster = new SmoothingAdjuster(p.dim());
        //adjuster.fixValue(1, -0f);
        //adjuster.fixValue(7, -0f);
        //adjuster.fixValue(2, -5f);
        //adjuster.fixValue(3, -1f);
        //adjuster.fixValue(4, -1f);
        //adjuster.fixValue(5, 1000f);
        //adjuster.fixValue(6, 0f);
       
        //p.switchToMinimax(1);
        switch(choice) {
            case -1:
                checkScore(p);break;
            case 0:
                watch(p);break;
            case 1:
                GeneticAlgorithmAdjuster adjuster = new GeneticAlgorithmSD(p, p.dim(), 20);
                adjuster.fixValue(0, -9999999f);
                learn(adjuster);break;
                //learn(p, adjuster);break;
            case 2:
                record(p);break;
            case 3:
                int[] pState = new int[0];
                System.out.println(pState.length);
                watchWithPredeterminedState(p, pState);break;
        }
    }
}


class OhTestPlayer {
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

			sum += -1f * FeatureFunctions.sumHeight(nextState);
			//sum += -20f * FeatureFunctions.maximumColumnHeight(nextState);
			sum += -3f * FeatureFunctions.numEmptyCells(nextState);
			sum += FeatureFunctions.lost(nextState) > 0 ? Float.NEGATIVE_INFINITY : 0;
			sum += -1f * FeatureFunctions.bumpiness(nextState);
			sum += 1000f * FeatureFunctions.numRowsCleared(nextState);

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

	/*public static void main(String[] args) {
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
    }*/

	public static void main(String[] args) {
		int sum = 0;
		int sumSquare = 0;
		int tries = 100;
		//ArrayList<Integer> list = new ArrayList<>();

		for (int i=0; i<tries; i++) {
			State s = new State();
			OhTestPlayer p = new OhTestPlayer();

			while(!s.hasLost()) {
				s.makeMove(p.findBest(s,s.legalMoves()));
				//s.draw();
				//s.drawNext(0,0);
			}
			sum += s.getRowsCleared();
			//list.add(s.getRowsCleared());
			sumSquare += s.getRowsCleared()*s.getRowsCleared();
		}
		double stdDev = Math.sqrt((float)(tries*sumSquare - sum*sum)/tries/tries);

		//System.out.println(list);
		System.out.println("Average rows cleared: " + (sum/tries));
		System.out.println("Standard Dev: " + stdDev);
	}    

}


class TemplateSkeleton {

    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        
        return 0;
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        TemplateSkeleton p = new TemplateSkeleton();
        while(!s.hasLost()) {
            s.makeMove(0, p.pickMove(s,s.legalMoves()));
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


class WeightedHeuristicPlayer {
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
        float[] resultArray = new float[times];

        IntStream.range(0, times)
            .parallel()
            .forEach(i -> {
                float[] results = new float[2];
                play(results, 1);
                resultArray[i] = results[0];
            });
        for (float result : resultArray) {
            total += result;
        }
        return (total/times);
    }
    
    public float playWithWeights(float[] weights, int times, SequenceStore store) {
        if (store == null) return playWithWeights(weights, times);
        
        this.weights = new float[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = weights[i];
        }
        long total = 0;
        int[] resultArray = new int[times];
        Sequence[] sequenceArray = new Sequence[times];
    
        IntStream.range(0, times)
            .parallel()
            .forEach(i -> {
                playRecord(resultArray, sequenceArray, i);
            });
        for (float result : resultArray) {
            total += result;
        }
        
        for (Sequence seq : sequenceArray) {
            if (seq != null) {
                store.addSequence(seq);
            }
        }
        
        return ((float)total/times);
    }

    
    public float playWithWeightsMin(float[] weights, int times, SequenceStore store) {
        if (store == null) return playWithWeights(weights, times);
        
        this.weights = new float[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = weights[i];
        }
        int[] result = new int[1];
        result[0] = Integer.MAX_VALUE;
        Sequence[] sequenceArray = new Sequence[times];
    
        IntStream.range(0, times)
            .parallel()
            .forEach(i -> {
                playRecordMin(result, sequenceArray, i);
            });
        
        for (Sequence seq : sequenceArray) {
            if (seq != null) {
                store.addSequence(seq);
            }
        }
        return result[0];
        //return ((float)total/times);
    }

    public float playPartialWithWeights(float[] weights, int times) {
        return playPartialWithWeights(weights, times, null);
    }
    
    public float playPartialWithWeights(float[] weights, int times, Sequence[] sequences) {
        this.weights = new float[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            this.weights[i] = weights[i];
        }

        float total = 0;
        float[] resultArray = new float[times];

        IntStream.range(0, times)
            .parallel()
            .forEach(i -> {
                float[] results = new float[2];
                Sequence sequence = sequences == null ? null : sequences[i];
                PartialGamePlayer.play(this, results, 1, sequence);
                resultArray[i] = results[0];
            });
        for (float result : resultArray) {
            total += result;
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
    
    public void play(float[] results, int tries) {
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
    
    public void playRecord(int[] resultArray, Sequence[] sequenceArray, int index) {
        ArrayList<Integer> pieces = new ArrayList<>();
        
        State s = new State();
        while(!s.hasLost()) {
            if (maxHeight(s) == 0) {
                pieces.clear();
            }
            pieces.add(s.getNextPiece());
            s.makeMove(findBest(s,s.legalMoves()));
        }
        int cleared = s.getRowsCleared();
        
        sequenceArray[index] = new Sequence(cleared, pieces);
        resultArray[index] = cleared;
    }
    
    public void playRecordMin(int[] result, Sequence[] sequenceArray, int index) {
        ArrayList<Integer> pieces = new ArrayList<>();
        
        State s = new State();
        while(!s.hasLost()) {
            if (maxHeight(s) == 0) {
                pieces.clear();
            }
            pieces.add(s.getNextPiece());
            s.makeMove(findBest(s,s.legalMoves()));
            if (s.getRowsCleared() >= result[0]) {
                //System.out.println("Terminate game " + index + " @ " + s.getRowsCleared());
                return; // first failure terminate.
            }
        }
        int cleared = s.getRowsCleared();

        //System.out.println("Clear game " + index);
        sequenceArray[index] = new Sequence(cleared, pieces);
        if (cleared < result[0])
            result[0] = cleared;
    }
    
    public void learn(WeightAdjuster adjuster) {
        float[] results = new float[2];
        int iteration = 0;
        String adjusterReport = null;
        while(true) {
            play(results, 3);
            if (adjusterReport != null) {
                report(iteration, results, weights, adjusterReport);
            }
            adjusterReport = adjuster.adjust(results, weights);
            iteration++;
        }
    }
    
    public static int maxHeight(State s) {
        int top[] = s.getTop();
        int maxHeight = 0;
        for (int i = 0; i < State.COLS; ++i) {
            maxHeight = Math.max(maxHeight, top[i]);
        }
        return maxHeight;
    }

    
    public static int[] toArray(ArrayList<Integer> pieceList) {
        int[] pieces = new int[pieceList.size()];
        for (int i=0; i<pieces.length; ++i) {
            pieces[i] = pieceList.get(i);
        }
        return pieces;
    }
    
    public static void record(WeightedHeuristicPlayer p) {
        record(p, null);
    }
    
    public static void record(WeightedHeuristicPlayer p, SequenceStore store) {
        ArrayList<Integer> pieces = new ArrayList<>();
        
        State s = new State();
        while(!s.hasLost()) {
            if (maxHeight(s) == 0) {
                //System.out.println("CLEAR SCREEN!");
                pieces.clear();
            }
            System.out.print(" " + s.getNextPiece());
            pieces.add(s.getNextPiece());
            s.makeMove(p.findBest(s,s.legalMoves()));
        }
        
        if (store == null) {
            System.out.println();
            System.out.println("PIECES = " + pieces.toString());
            //System.out.println(pieces.size() + " | " + count);
            System.out.println("You have completed "+s.getRowsCleared()+" rows.");
        } else {
            store.addSequence(s.getRowsCleared(), toArray(pieces));
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
        s = new PredeterminedState(new int[0]);
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
    
    public static void watchWithPredeterminedState(WeightedHeuristicPlayer p, int[] sequence) {
        final int REPORT_INTERVAL = 1000;
        PredeterminedState s = new PredeterminedState(sequence);
        new TFrame(s);

        int counter = REPORT_INTERVAL;
        while(!s.hasLost()) {
            System.out.print(" " + s.getNextPiece());
            s.makeMove(p.findBest(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(10);
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
        final int REPORT_INTERVAL = 3000;
        State s = new State();
        //new TFrame(s);

        int counter = REPORT_INTERVAL;
        while(!s.hasLost()) {
            s.makeMove(p.findBest(s,s.legalMoves()));
            
            counter--;
            if (counter <= 0) {
                System.out.println("CURRENT SCORE: " + s.getRowsCleared());
                counter = REPORT_INTERVAL;
            }
            
            /*int[] top = s.getTop();
            boolean heightZero = true;
            for (int t : top) {
                if (t != 0) {
                    heightZero = false;
                    break;
                }
            }
            if (heightZero) System.out.println("ALL CLEAR!");*/
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


class GeneticAlgorithmAdjuster {
    protected static Random rand = new Random();
    protected int dim;
    protected int realDim;
    protected float[][] states;
    protected float[] scores;
    protected WeightedHeuristicPlayer w;
    protected PartialGamePlayer p;
    protected int stateNumber;
    protected final int INITIAL_GOOD_STATES = 0;
    protected int PRINT_INTERVAL = 10;
    
    protected float total = 0;
    
    protected final int MUTATION_BITS = 2;
    protected double mutationProbability = 0.2;
    //protected double mutationProbability = 0.4;
    protected HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    
    //protected float[] highScoreWeights;
    //protected float highScore;
    protected int STALE_HEAP_THRESHOLD = 500; // terminate after 25 iterations of no improvement
    protected WeightHeap bestHeap = new WeightHeap(10);
    
    //protected static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    //protected static float[] conversionTable = new float[]{0.01f, 0.05f, 0.3f, 1.5f, 4f, 10f, 30f, 60f, 90f, 140, 200, 300, 500, 1000, 1500, 6000};
    //protected static float[] conversionTable = new float[]{0.01f, 0.05f, 0.3f, 1.5f, 4f, 10f, 30f, 60f, 90f, 140, 200, 250,300,350,400,500,700,850, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 4000, 5000, 6000};
    protected static final int WORD_SIZE = 12;
    protected static final int HALF_TABLE_SIZE = pow2(WORD_SIZE-1);
    private static final int MUTATE_LENGTH_MAX = 6;
    
    private static int pow2(int n) {
        int v = 1;
        for (; n > 0; --n) v *= 2;
        return v;
    }
    
    public GeneticAlgorithmAdjuster(WeightedHeuristicPlayer w, int _dim, int N) {
        dim = _dim;
        realDim = dim;
        stateNumber = N;
        this.w = w;
        states = new float[stateNumber][];
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
        scores = new float[stateNumber];
    }
    
    public void fixValue(int position, float value) {
        fixedValue.put(position, value);
        --dim;
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
    }
    
    float[] generateRealWeights(float[] state) {
        float[] realWeights = new float[realDim];
        int index = 0;
        for (int i = 0; i < realDim; ++i) {
            if (fixedValue.containsKey(i)) {
                realWeights[i] = fixedValue.get(i);
            } else {
                realWeights[i] = state[index++];
            }
        }
        return realWeights;
    }
    
    protected float[] generateState(float[] realWeights) {
        float[] state = new float[dim];
        int index = 0;
        for (int i = 0; i < realDim; ++i) {
            if (!fixedValue.containsKey(i)) {
                state[index++] = realWeights[i];
            }
        }
        return realWeights;
    }
    
    protected float[] randomStateFromHeap() {
        return generateState(bestHeap.getRandomWeights());
    }
    
    protected float[] generateRandomState(int length) {
        boolean[] encoded = new boolean[length*WORD_SIZE];
        for (int i=0; i<encoded.length; ++i) {
            encoded[i] = rand.nextBoolean();
        }
        return decode(encoded);
    }
    
    protected void generateRandomStates() {
        generateMostlyRandomStates();
    }
    
    private void generateTotallyRandomStates() {
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = generateRandomState(dim);
        }
    }
    
    private void generateMostlyRandomStates() {
        for (int i=0; i<INITIAL_GOOD_STATES; ++i) {
            states[i] = generateInitialGoodState();
        }
        for (int i = INITIAL_GOOD_STATES; i < stateNumber; ++i) {
            states[i] = generateRandomState(dim);
        }
    }
    
    private float[] generateInitialGoodState() {
        float[][] goodWeights = new float[][] {
                new float[]{-1627f, 1817f, 1706f, -1361f, -1299f, -1786f, -706f, 1331f, -92f, 303f, -1602f, -1963f, -865f, -1516f, 910f, -2001f, 654f, 0f, -19f},
                new float[]{-1917f, 1887f, 1714f, -1366f, -1069f, -1782f, -700f, 1331f, -79f, 304f, -1624f, -1971f, -794f, -1519f, 931f, -1996f, 654f, -7f, -13f},
                new float[]{-1923f, 1890f, 1705f, -1353f, -1061f, -1794f, -709f, 1326f, -78f, 307f, -1621f, -1965f, -806f, 549f, 903f, -1990f, 655f, -13f, -5f},
                new float[]{-1919f, 1882f, 1704f, -1369f, -1497f, -1783f, -704f, 1323f, -79f, 292f, -1612f, -1982f, -804f, 582f, 896f, -1981f, 671f, -6f, 4f},
                new float[]{-1929f, 1902f, 1701f, -1362f, -1039f, -1796f, -696f, 1343f, -79f, 435f, -1612f, -1963f, -832f, 581f, 899f, -1924f, 668f, 2f, 6f},
                new float[]{-1919f, 1883f, 1703f, -1360f, -1067f, -1777f, -702f, 1326f, -78f, 306f, -1614f, -1966f, -801f, 545f, 907f, -1992f, 644f, -6f, -13f},
                new float[]{-1923f, 1891f, 1706f, -1353f, -1308f, -1779f, -696f, 1328f, -88f, 428f, -1619f, -1976f, -826f, 580f, 895f, -1932f, 642f, -8f, -3f},
                new float[]{-1626f, 1805f, 1699f, -1361f, -1300f, -1796f, -707f, 1338f, -88f, 305f, -1614f, -1969f, -865f, -1514f, 896f, -2010f, 646f, -2f, -16f},
                new float[]{-1916f, 1886f, 1699f, -1368f, -1069f, -1781f, -691f, 1339f, -92f, 308f, -1618f, -1978f, -808f, -1518f, 932f, -1990f, 652f, 4f, -13f},
                new float[]{-1930f, 1878f, 1700f, -1356f, -1072f, -1780f, -702f, 1333f, -94f, 297f, -1618f, -1965f, -793f, 544f, 889f, -1992f, 653f, -10f, -8f},
                new float[]{-1913f, 1889f, 1707f, -1353f, -1485f, -1783f, -711f, 1329f, -92f, 292f, -1613f, -1981f, -808f, 579f, 903f, -1981f, 688f, -11f, -7f},
                new float[]{-1915f, 1900f, 1711f, -1349f, -1043f, -1792f, -704f, 1344f, -88f, 435f, -1631f, -1965f, -843f, 577f, 903f, -1941f, 670f, -8f, -2f},
                new float[]{-1927f, 1891f, 1710f, -1369f, -1063f, -1795f, -716f, 1328f, -77f, 297f, -1627f, -1959f, -803f, 542f, 906f, -2003f, 639f, -15f, -11f},
                new float[]{-1926f, 1887f, 1698f, -1360f, -1289f, -1794f, -692f, 1333f, -78f, 441f, -1625f, -1976f, -832f, 576f, 891f, -1937f, 640f, -13f, -2f}
        };
        int choice = rand.nextInt(goodWeights.length);
        float[] weights = goodWeights[choice];
        if (weights.length != dim) {
            generateRandomState(dim);
            //throw new UnsupportedOperationException("ERROR!!! WRONG DIM!");
        }
        return weights;
    }
    
    protected void selection() {
        float totalScore = 0;
        while (totalScore == 0) {
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                float result = w.playPartialWithWeights(realWeights,50);
                //float result = w.playWithWeights(realWeights, 15);
                scores[i] = result;
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
        }
        
        Integer[] ranked = new Integer[scores.length];
        for (int i=0; i<ranked.length; ++i) ranked[i] = i;
        Arrays.sort(ranked, (a, b) -> {
            float cmp = scores[b] - scores[a];
            if (cmp < 0) return -1;
            else if (cmp == 0) return 0;
            else return 1;
        });
        float[] probability = new float[scores.length];
        int totalProb = 0;
        for (int i=0; i<probability.length; ++i) {
            probability[i] = scores.length-i;
            totalProb += scores.length-i;
        }
        for (int i=0; i<probability.length; ++i) {
            probability[i] /= totalProb;
        }
        for (int i=0; i<ranked.length; ++i) {
            scores[ranked[i]] = probability[i];
            //System.out.println(i + " | " + ranked[i] + " | " + scores[ranked[i]] + " | " + probability[i]);
        }
        //states[ranked[ranked.length-1]] = generateRandomState(states[0].length);
        states[ranked[ranked.length-1]] = randomStateFromHeap();
        
        
        
        /*for (int i = 0; i < states.length; ++i) {
            scores[i] /= totalScore;
        }*/
        float[][] newStates = new float[stateNumber][];
        for (int i = 0; i < states.length; ++i) {
            double random = rand.nextDouble();
            for (int j = 0; j < states.length; ++j) {
                random -= scores[j];
                if (random < 0) {
                    newStates[i] = states[j];
                    break;
                }
            }
        }
        states = newStates;
    }
    
    protected void crossover() {
        for (int i = 0; i + 1 < states.length; i += 2) {
            boolean[] firstBitString = encode(states[i]);
            boolean[] secondBitString = encode(states[i+1]);
            int position = rand.nextInt(firstBitString.length);
            int position2 = rand.nextInt(firstBitString.length);
            if (position2 < position) {
                int temp = position2;
                position2 = position;
                position = temp;
            }
            
            for (int j = position; j < position2; ++j) {
                boolean temp = firstBitString[j];
                firstBitString[j] = secondBitString[j];
                secondBitString[j] = temp;
               
            }
            states[i] = decode(firstBitString);
            states[i+1] = decode(secondBitString);
        }
    }
    
    protected boolean maybeReset(int iteration) {
        if (bestHeap.consecutiveRejects <= STALE_HEAP_THRESHOLD) return false;
        
        String message = "Best scores heap is stale. " + bestHeap.consecutiveRejects +
                " consecutive rejects. RESETTING STATES AND SAVING...";
        System.out.print(message);
        message += "\nTerminated at iteration " + iteration + ".\nScores:\n" + bestHeap.toString() + "\n";
        ResultDump.saveResults(message);
        generateTotallyRandomStates();
        bestHeap = new WeightHeap(bestHeap.size());
        return true;
    }
    
    
    protected int mutationBits() {
        return MUTATION_BITS;
    }
    
    protected void mutation() {
        int mutationBits = mutationBits();
        for (int i = 0; i < states.length; ++i) {
            boolean[] bitString = encode(states[i]);
            for (int j=0; j<mutationBits; ++j) {
                int mutateLength = rand.nextInt(MUTATE_LENGTH_MAX);
                int position = rand.nextInt(bitString.length-mutateLength);
                double prob = rand.nextDouble();
                if (prob < mutationProbability) {
                    //System.out.println("MUTATION!");
                    for (int k=0; k<mutateLength; ++k)
                        bitString[position+k] ^= true;
                }
            }
            states[i] = decode(bitString);
        }
    }

    public static boolean[] binToGray(boolean[] bin) {
        boolean[] newBin = Arrays.copyOf(bin, bin.length);
        int words = newBin.length/WORD_SIZE;
        for (int i=words-1; i>=0; --i) {
            int offset = i*WORD_SIZE;
            for (int j=0; j<WORD_SIZE-1; ++j) {
                newBin[offset+j] = newBin[offset+j] ^ newBin[offset+j+1];
            }
        }
        return newBin;
    }
    
    public static boolean[] grayToBin(boolean[] bin) {
        boolean[] newBin = Arrays.copyOf(bin, bin.length);
        int words = newBin.length/WORD_SIZE;
        for (int i=words-1; i>=0; --i) {
            int offset = i*WORD_SIZE;
            for (int j=WORD_SIZE-2; j>=0; --j) {
                newBin[offset+j] = newBin[offset+j] ^ newBin[offset+j+1];
            }
        }
        return newBin;
    }
    
    public static String bitString(boolean[] encoded) {
        StringBuilder sb = new StringBuilder(encoded.length);
        for (boolean b : encoded) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public static int encodeFloat(float f) {
        return (int)(f + HALF_TABLE_SIZE);
        
        /*boolean neg = f < 0;
        if (neg) f = -f;
        
        int index = 0;
        while (index+1 < conversionTable.length && f > conversionTable[index]) {
            index++;
        }
        if (neg) return conversionTable.length-index-1;
        else return conversionTable.length+index;*/
    }

    public static float decodeFloat(int v) {
        return v - HALF_TABLE_SIZE;
        /*boolean neg = v < HALF_TABLE_SIZE;
        if (neg) v = HALF_TABLE_SIZE - v - 1;
        else v -= HALF_TABLE_SIZE;
        if (neg) return -conversionTable[v];
        else return conversionTable[v];*/
    }

    public static boolean[] encode(float[] array) {
        boolean[] encoded = new boolean[array.length*WORD_SIZE];
        for (int i=0; i<array.length; ++i) {
            int offset = i*WORD_SIZE;
            int v = encodeFloat(array[i]);
            int index = 0;
            while (v > 0) {
                encoded[offset+index] = v%2 == 1;
                v /= 2;
                index++;
            }
        }
        //return encoded;
        return binToGray(encoded);
    }

    public static float[] decode(boolean[] encoded) {
        encoded = grayToBin(encoded);
        float[] decoded = new float[encoded.length/WORD_SIZE];
        for (int i=0; i<decoded.length; ++i) {
            int offset = i*WORD_SIZE;
            int v = 0;
            for (int j = WORD_SIZE-1; j>=0; --j) {
                v *= 2;
                if (encoded[offset+j]) v++;
            }
            decoded[i] = decodeFloat(v);
        }
        return decoded;
    }
    
    protected float playWithWeights(float[] realWeights, int times) {
        return w.playWithWeights(realWeights, 2);
    }
    
    public void adjust() {
        generateRandomStates();
        int iteration = Integer.MAX_VALUE;
        for (int i = 0; i < iteration; ++i) {
            System.out.println("Iteration " + i);
            selection();
            crossover();
            mutation();
            
            if ((i+1)%PRINT_INTERVAL == 0) {
                //System.out.println("Iteration " + i);
                float total = 0;
                for (int j = 0; j < stateNumber; ++j) {
                    float[] realWeights = generateRealWeights(states[j]);
                    
                    float result = printAndReturnResult(j, realWeights);
                    
                    total += result;
                    
                    bestHeap.tryInsert(result, realWeights);
                    /*if (result > highScore) {
                        highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
                        highScore = result;
                    }*/
                    
                }
                printTotalAndHighScore(total);
            }
        }
    }

    protected void printTotalAndHighScore(float total) {
        System.out.println("Average Score: " + (total/stateNumber));
        System.out.println(bestHeap);
        //System.out.println("Hi-Score: " + highScore + " | " + Arrays.toString(highScoreWeights));
    }

    protected float printAndReturnResult(int j, float[] realWeights) {
        float result = playWithWeights(realWeights, 2);
        float resultPartial = w.playPartialWithWeights(realWeights, 10);

        printResults(j, result, resultPartial);
        return result;
    }

    protected void printResults(int stateNo, float result, float resultPartial) {
        System.out.printf("State #%2d. Score = %10.3f - %8.3f [",stateNo,result,resultPartial);
        for (int k = 0; k < states[stateNo].length; ++k) {
            System.out.printf("%9.2f", states[stateNo][k]);
            if (k + 1 < states[stateNo].length) {
                System.out.printf(",");
            } else {
                System.out.println("]");
            }
        }
    }
    
}


class WeightHeap {
    private float[] scores;
    private float[][] weights;
    private int size;
    private static Random rand = new Random();
    public int consecutiveRejects;

    public WeightHeap(int size) {
        this.size = size;
        weights = new float[size][];
        scores = new float[size];
        consecutiveRejects = 0;
    }
    
    public void tryInsert(float score, float[] weights) {
        if (score < scores[0]) {
            //System.out.println(consecutiveRejects);
            consecutiveRejects++;
            return;
        }
        
        consecutiveRejects = 0;
        this.scores[0] = score;
        this.weights[0] = Arrays.copyOf(weights, weights.length);
        bubbleDown();
    }
    
    private void bubbleDown() {
        int current = 0;
        while(true) {
            int left = 2*current+1;
            if (left >= size)
                break;
            int target = left;
            int right = 2*current+2;
            if (right < size)
                target = minValue(left, right);
            if (minValue(current, target) == current) {
                break; // stop bubbling down.
            } else {
                // swap weights
                float[] tempF = weights[current];
                weights[current] = weights[target];
                weights[target] = tempF;
                // swap scores
                float tempI = scores[current];
                scores[current] = scores[target];
                scores[target] = tempI;
                
                current = target;
            }
        }
    }
    
    private int minValue(int i1, int i2) {
        if (scores[i1] <= scores[i2])
            return i1;
        return i2;
    }
    
    public int size() {
        return size;
    }
    
    @Override
    public String toString() {
        String sep = "";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<size; ++i) {
            String scoreString = scores[i] + " - ";
            sb.append(sep);
            sb.append(scoreString);
            for (int j=scoreString.length(); j<12; ++j) sb.append(" ");
            sb.append(Arrays.toString(weights[i]));

            sep = "\n";
        }
        
        return sb.toString();
    }
    
    
    public float[] getRandomWeights() {
        int nWeights = 0;
        for (int i=0; i<weights.length; ++i) {
            if (weights[i] != null) nWeights++;
        }
        if (nWeights == 0) throw new UnsupportedOperationException("Heap empty???");
        
        int count = rand.nextInt(nWeights);
        for (int i=0; i<weights.length; ++i) {
            if (weights[i] != null) {
                if (count > 0) {
                    count--;
                } else{
                    return Arrays.copyOf(weights[i], weights[i].length);
                }
            }
        }
        throw new UnsupportedOperationException("Not supposed to reach here.");
    }
    
    private float[] popMin() {
        float[] weight = weights[0];
        scores[0] = Integer.MAX_VALUE;
        bubbleDown();
        return weight;
    }
}


class GeneticAlgorithmAdjuster2 {
    private static Random rand = new Random();
    private int dim;
    private int realDim;
    private float[][] states;
    private float[] scores;
    private WeightedHeuristicPlayer w;
    private int stateNumber;
    //private double mutationProbability = 0.1;
    private double mutationProbability = 0.1;
    private HashMap<Integer,Float> fixedValue = new HashMap<>(); 
    private HashMap<Integer,Float> fixedSign = new HashMap<>();
    
    private float[] highScoreWeights;
    private float highScore;

    private static float[] conversionTable = new float[]{0.01f, 0.02f, 0.05f, 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f, 20f, 40f, 70f, 100f, 500f, 8000};
    private static final int WORD_SIZE = 5;
    
    public GeneticAlgorithmAdjuster2(WeightedHeuristicPlayer w, int _dim, int N) {
        dim = _dim;
        realDim = dim;
        stateNumber = N;
        this.w = w;
        states = new float[stateNumber][];
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
        scores = new float[stateNumber];
    }
    
    private float randomWeight(int position) {
        float res = (float)Math.pow(rand.nextInt(16), 5) / 100;
        if (fixedSign.containsKey(position)) {
            res = res * fixedSign.get(position);
        } else if (rand.nextDouble() < 0.5) {
            res = res * -1.0f;
        }
        return res;
    }
    
    public void fixValue(int position, float value) {
        fixedValue.put(position, value);
        --dim;
        for (int i = 0; i < stateNumber; ++i) {
            states[i] = new float[dim];
        }
    }
    
    public void fixSign(int position, float value) {
        fixedSign.put(position,value);
    }
    
    float[] generateRealWeights(float[] state) {
        float[] realWeights = new float[realDim];
        int index = 0;
        for (int i = 0; i < realDim; ++i) {
            if (fixedValue.containsKey(i)) {
                realWeights[i] = fixedValue.get(i);
            } else {
                realWeights[i] = state[index++];
            }
        }
        return realWeights;
    }
    
    private void generateRandomStates() {
        for (int i = 0; i < stateNumber; ++i) {
            for (int j = 0; j < dim; ++j) {
                states[i][j] = randomWeight(j);
            }
        }
    }
    
    private void selection() {
        float totalScore = 0;
        while (totalScore == 0) {
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                float result = w.playWithWeights(realWeights, 6);
                scores[i] = result;
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
        }
        
        boolean[] chosen = new boolean[states.length];
        for (int i = 0; i < states.length; ++i) {
            chosen[i] = false;
        }
        float[][] newStates = new float[states.length][];
        
        for (int i = 0; i < states.length; ++i) {
            if (i >= states.length / 2) {
                newStates[i] = newStates[i-states.length / 2].clone();
                continue;
            }
            int maxIndex = -1;
            for (int j = 0; j < states.length; ++j) {
                if (!chosen[j] && (maxIndex == -1 || scores[j] > scores[maxIndex])) {
                    maxIndex = j;
                }
            }
            chosen[maxIndex] = true;
            newStates[i] = states[maxIndex].clone();
        }
        
        states = newStates;
    }
    
    private void crossover() {
        for (int i = states.length / 2; i + 1 < states.length; i += 2) {
            int position = rand.nextInt(states[i].length);
            for (int j = 0; j < position; ++j) {
                float temp = states[i][j];
                states[i][j] = states[i+1][j];
                states[i+1][j] = temp;
            }
        }
    }
    
    private void mutation() {
        for (int i = states.length / 2; i < states.length; ++i) {
            int position = rand.nextInt(states[i].length);
            double prob = rand.nextDouble();
            if (prob < mutationProbability) {
                System.out.println("MUTATION!");
                states[i][position] = randomWeight(position);
            }
        }
    }
    
    public void adjust() {
        generateRandomStates();
        int iteration = Integer.MAX_VALUE;
        int interval = 1;
        for (int i = 0; i < iteration; ++i) {
            //System.out.println("Iteration " + i);
            selection();
            crossover();
            mutation();
            
            if (i%interval == 0) {
                System.out.println("Iteration " + i);
                float total = 0;
                for (int j = 0; j < stateNumber; ++j) {
                    float[] realWeights = generateRealWeights(states[j]);
                    float result = w.playWithWeights(realWeights, 2);
                    System.out.println(
                            //bitString(encode(states[j])) + "    " +
                            "State #" + j + ". Score = " + result + "   " +
                            Arrays.toString(states[j]));
                            //Arrays.toString(states[j]));
                    total += result;
                    
                    if (result > highScore) {
                        highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
                        highScore = result;
                    }
                    
                }
                System.out.println("Average Score: " + (total/stateNumber));
                System.out.println("Hi-Score: " + highScore + " | " + Arrays.toString(highScoreWeights));
            }
        }
    }
    
}


class GeneticAlgorithmSD extends GeneticAlgorithmAdjuster {
    //private static int SAVE_INTERVAL = 1;
    private static final double DATABASE_SEQUENCE_PROBABILTY = 1f;
    private static final float HARD_BIAS = 0.15f; // HARD_BIAS of 1 is 100% hardest sequence. HARD_BIAS of 0 is even distribution.
    
    private static final int PARTIAL_TRIES = 50;
    private static final int REAL_TRIES = 8;
    private int selectionIteration;
    private int realInterval = 1;
    
    private static final float WEIGHT_REAL = 0.8f; // between 0 and 1.

    private boolean readyToPrint;
    private boolean printed;

    protected float lastAverage;
    
    private Sequence[] sequences;
    
    private SequenceStore store;
    
    public GeneticAlgorithmSD(WeightedHeuristicPlayer w, int dim, int N) {
        super(w, dim, N);
        //store = SequenceStore.empty();
        store = SequenceStore.loadTrimmed();
        sequences = new Sequence[PARTIAL_TRIES];
        System.out.println(store);
        
        PRINT_INTERVAL = 1;
    }
    
    @Override
    protected void selection() {
        selectionIteration++;
        boolean playReal = (selectionIteration%realInterval == 0);
        
        float totalScore = 0;
        while (totalScore == 0) {

            for (int i=0; i<PARTIAL_TRIES; ++i) {
                if (rand.nextFloat() <= DATABASE_SEQUENCE_PROBABILTY) {
                    sequences[i] = store.getRandomSequence(HARD_BIAS);
                } else {
                    sequences[i] = null;
                }
            }
            
            for (int i = 0; i < states.length; ++i) {
                float[] realWeights = generateRealWeights(states[i]);
                
                if (playReal) {
                    float resultP = w.playPartialWithWeights(realWeights, PARTIAL_TRIES, sequences);
                    float resultR = w.playWithWeightsMin(realWeights, REAL_TRIES, store);
                    scores[i] = weightedMean(resultP, resultR);
                    if (resultR > 100000) {
                        //System.out.println(resultR + " attained from " + Arrays.toString(realWeights));
                    }
                    if (readyToPrint) {
                        analyseResults(i, resultR, resultP, scores[i], realWeights);
                        printed = true;
                    }
                } else {
                    float resultP = w.playPartialWithWeights(realWeights, PARTIAL_TRIES, sequences);
                    scores[i] = resultP;
                }
                //System.out.println(resultR + " | " + resultP + " | " + scores[i]);
                //System.out.println(i + " " + scores[i]);
                totalScore += scores[i];
            }
            if (totalScore == 0) {
                generateRandomStates();
            }
            
            if (playReal) {
                lastAverage = totalScore/states.length;
            }
            
            if (printed) {
                printTotalAndHighScore(totalScore);
            } else {
                System.out.println((selectionIteration%realInterval == 0) + " | " + totalScore/states.length);
            }

            readyToPrint = false;
        }
        
        Integer[] ranked = new Integer[scores.length];
        for (int i=0; i<ranked.length; ++i) ranked[i] = i;
        Arrays.sort(ranked, (a, b) -> {
            float cmp = scores[b] - scores[a];
            if (cmp < 0) return -1;
            else if (cmp == 0) return 0;
            else return 1;
        });
        float[] probability = new float[scores.length];
        int totalProb = 0;
        for (int i=0; i<probability.length; ++i) {
            int prob = scores.length-i;
            prob = prob*prob;
            probability[i] = prob;
            totalProb += prob;
        }
        for (int i=0; i<probability.length; ++i) {
            probability[i] /= totalProb;
        }
        for (int i=0; i<ranked.length; ++i) {
            scores[ranked[i]] = probability[i];
            //System.out.println(i + " | " + ranked[i] + " | " + scores[ranked[i]] + " | " + probability[i]);
        }
        //states[ranked[ranked.length-1]] = generateRandomState(states[0].length);
        states[ranked[ranked.length-1]] = randomStateFromHeap();
        
        
        
        /*for (int i = 0; i < states.length; ++i) {
            scores[i] /= totalScore;
        }*/
        float[][] newStates = new float[stateNumber][];
        for (int i = 0; i < states.length; ++i) {
            double random = rand.nextDouble();
            for (int j = 0; j < states.length; ++j) {
                random -= scores[j];
                if (random < 0) {
                    newStates[i] = states[j];
                    break;
                }
            }
        }
        states = newStates;
    }
    
    /**
     * Both resultP and resultR must be nonnegative!!!
     */
    private float weightedMean(float resultP, float resultR) {
        
        // Note: both must be nonnegative!
        
        double p = Math.pow(resultP, 1 - WEIGHT_REAL);
        double r = Math.pow(resultR, WEIGHT_REAL);

        // Currently: Geometric Mean
        return (float)(p*r);
        
        
        /*// Harmonic Mean
        float sum = resultP + resultR;
        float product = resultP*resultR;
        
        if (sum == 0)
            return 0;
        return product/sum;*/
    }
    
    @Override
    protected int mutationBits() {
        int s = (int)lastAverage;
        int bits = 12;
        while (s > 3) {
            s /= 3;
            bits--;
        }
        bits = bits*3/2;
        System.out.println("Mutation bits: " + lastAverage + " => " + Math.max(bits, MUTATION_BITS));
        return Math.max(bits, MUTATION_BITS);
    }


    @Override
    protected float playWithWeights(float[] realWeights, int times) {
        return w.playWithWeights(realWeights, times, store);
    }
    
    @Override
    public void adjust() {
        generateRandomStates();
        printed = false;
        readyToPrint = false;
        
        int resetCount = 0;
        int iteration = Integer.MAX_VALUE;
        for (int i = 0; i < iteration; ++i) {
            if ((i+1)%PRINT_INTERVAL == 0) readyToPrint = true;
            System.out.println("Iteration " + i);
            
            selection();
            crossover();
            mutation();
            
            boolean reset = maybeReset(i);
            if(reset) {i = 0; resetCount++;}
            System.out.println("Resets: " + resetCount + " | Staleness: " + bestHeap.consecutiveRejects + " / " + STALE_HEAP_THRESHOLD);
            
            if (printed) {
                store.saveSequences();
                printed = false;
            }
        }
    }
    
    
    
    private void analyseResults(int stateNo, float result, float resultPartial, float weightedMean, float[] realWeights) {
        System.out.printf("State #%2d. Score = %10.3f - %8.3f - %8.3f [",stateNo,result,resultPartial,weightedMean);
        for (int k = 0; k < states[stateNo].length; ++k) {
            System.out.printf("%9.2f", states[stateNo][k]);
            if (k + 1 < states[stateNo].length) {
                System.out.printf(",");
            } else {
                System.out.println("]");
            }
        }

        bestHeap.tryInsert(result, realWeights);
        /*if (result > highScore) {
            highScoreWeights = Arrays.copyOf(realWeights, realWeights.length);
            highScore = result;
        }*/
    }
    
    
}



class OhAdjuster implements WeightAdjuster {
    
    float currentBest;
    float bestMean;
    float bestSd;
    float[] bestWeights;
    float[] lastWeights;
    HashMap<Integer, Float> fixedWeights;
    Random rand = new Random();

    public OhAdjuster() {
        fixedWeights = new HashMap<>();
    }
    
    @Override
    public String adjust(float[] results, float[] weights) {
        boolean newHighScore = tryUpdateBest(results);
        setWeights(results, weights);
        setFixedWeights(weights);
        lastWeights = Arrays.copyOf(weights, weights.length);
        return newHighScore ? ("New High Score: " + currentBest) : null;
    }
    
    private void setWeights(float[] results, float[] weights) {
        for (int i=0; i<weights.length; i++) {
            weights[i] = rand.nextFloat()*10f- 5f;
        }
    }

    private void setFixedWeights(float[] weights) {
        for (Map.Entry<Integer, Float> entry : fixedWeights.entrySet()) {
            int key = entry.getKey();
            float value = entry.getValue();
            weights[key] = value;
        }
    }
    
    @Override
    public void fixValue(int index, float value) {
        fixedWeights.put(index, value);
    }
    
    private float computeResult(float[] results) {
        return results[0]*2f - results[1];
    }
    
    private boolean tryUpdateBest(float[] results) {
        float result = computeResult(results);
        if (result > currentBest) {
            bestMean = results[0];
            bestSd = results[1];
            bestWeights = lastWeights;
            currentBest = result;
            return true;
        }
        return false;
    }
}


class PartialGamePlayer {
    private static final int CONST_ADD = 200;
    private static final int INITIAL_HEIGHT = 5;
    private static final int STOP_HEIGHT = 2;

    public static void play(WeightedHeuristicPlayer p, float[] results, int tries, Sequence sequence) {
        long sum = 0;
        long sumSquare = 0;
        int losses = 0;
        
        for (int i=0; i<tries; i++) {
            int maxHeight = 0;
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
                maxHeight = Math.max(maxHeight,WeightedHeuristicPlayer.maxHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
                count++;
            }
            while(!s.hasLost() && WeightedHeuristicPlayer.maxHeight(s) > STOP_HEIGHT) {
                maxHeight = Math.max(maxHeight,WeightedHeuristicPlayer.maxHeight(s));
                s.makeMove(p.findBest(s,s.legalMoves()));
                count++;
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


class SmoothingAdjuster implements WeightAdjuster {
    final float weightRange = 100f;
    //final int nIntervals = 12;

    private final int dim;
    private int unfixedDim;
    private int[] unfixedMappings;
    ArrayList<DataPoint> dataPoints;
    private HashMap<Integer, Float> fixedWeights;
    private float[] lastWeights;
    Random rand = new Random();
    
    private int sequence;

    public SmoothingAdjuster(int dim) {
        dataPoints = new ArrayList<>();
        fixedWeights = new HashMap<>();

        this.dim = dim;
        adjustUnfixedMappings();
    }

    @Override
    public String adjust(float[] results, float[] weights) {
        if (lastWeights != null)
            dataPoints.add(new DataPoint(lastWeights, results));
        boolean report = setWeights(results, weights);
        setFixedWeights(weights);
        
        lastWeights = copy(weights);
        return report ? "SUGGESTED!." : null;// "explore.....";
    }
    
    @Override
    public void fixValue(int index, float value) {
        fixedWeights.put(index, value);
        adjustUnfixedMappings();
    }
    
    public void adjustUnfixedMappings() {
        unfixedDim = dim - fixedWeights.size();
        unfixedMappings = new int[unfixedDim];
        int unfixedIndex = 0;
        for (int i=0; i<dim; i++) {
            if (!fixedWeights.containsKey(i)) {
                unfixedMappings[unfixedIndex++] = i;
            }
        }
    }
    
    private float[] copy(float[] weights) {
        float[] unfixedWeights = new float[unfixedDim];
        for (int i=0; i<unfixedDim; i++) {
            unfixedWeights[i] = weights[unfixedMappings[i]];
        }
        return unfixedWeights;
    }

    final float sqrtWeightRange = (float)Math.sqrt(weightRange);
    final float twoTimesSqrtWeightRange = 2*sqrtWeightRange;
    private boolean setWeights(float[] results, float[] weights) {
        
        boolean report;
        if (exploring()) {
            for (int i=0; i<unfixedMappings.length; i++) {
                float value = rand.nextFloat()*twoTimesSqrtWeightRange - sqrtWeightRange;
                boolean negative = value < 0;
                value *= value;
                if (negative)
                    value = -value;
                weights[unfixedMappings[i]] = value;
            }
            normaliseWeights(weights);
            report = false;
        } else {
            float[] newWeights = computeHighestScoringPosition();
            for (int i=0; i<unfixedMappings.length; i++) {
                weights[unfixedMappings[i]] = newWeights[i];
            }
            report = true;
        }
        sequence++;
        return report;
    }
    
    private void normaliseWeights(float[] weights) {
        float sum = 0;
        for (int i=0; i<unfixedMappings.length; i++) {
            sum += Math.abs(weights[unfixedMappings[i]]);
        }
        float multiplier = weightRange/sum;
        for (int i=0; i<unfixedMappings.length; i++) {
            weights[unfixedMappings[i]] *= multiplier;
        }
    }
    
    private void normalise(float[] fs) {
        float sum = 0;
        for (int i=0; i<fs.length; i++) {
            sum += Math.abs(fs[i]);
        }
        float multiplier = weightRange/sum;
        for (int i=0; i<fs.length; i++) {
            fs[i] *= multiplier;
        }
    }

    final float peturbDistance = weightRange/1000f;
    final float halfPeturbDistance = peturbDistance/2;
    private void peturb(float[] fs) {
        //final float peturbDistance = weightRange/1000f;
        //final float halfPeturbDistance = peturbDistance/2;
        
        for (int i=0; i<fs.length; i++) {
            float p = rand.nextFloat()*peturbDistance - halfPeturbDistance;
            fs[i] += p;
        }
        normalise(fs);
    }


    private boolean exploring() {
        return (sequence+1)%20 != 0;
    }

    private float[] computeHighestScoringPosition() {
        float[] scores = new float[dataPoints.size()];
        float[][] positions = new float[dataPoints.size()][];
        IntStream.range(0, dataPoints.size())
            .parallel()
            .forEach(i -> {
                positions[i] = dataPoints.get(i).weights;
                peturb(positions[i]);
                scores[i] = valueOf(positions[i]);
            });

        float largest = Float.NEGATIVE_INFINITY;
        float smallest = Float.POSITIVE_INFINITY;
        int largestIndex = -1;
        for (int i=0; i<scores.length; i++) {
            if (scores[i] >= largest) {
                largest = scores[i];
                largestIndex = i;
            }
            if (scores[i] < smallest) {
                smallest = scores[i];
            }
        }
        System.out.println(smallest + " | " + largest);
        try{
        System.out.println("Choose datapoint " + Arrays.toString(positions[largestIndex]) + " with value " + scores[largestIndex]);
        }catch(ArrayIndexOutOfBoundsException e) {
            System.out.println(Arrays.toString(scores));
            System.out.println(largestIndex);
            System.out.println(largest);
            throw e;
        }
        return positions[largestIndex];
    }

    /*private float[] computeHighestScoringPosition() {
        final int mapSize = pow(unfixedDim, nIntervals);
        
        float[] scoreMap = new float[mapSize];
        float[][] positions = new float[mapSize][];
        IntStream.range(0, mapSize)
            .parallel()
            .forEach(i -> {
                positions[i] = position(i);
                scoreMap[i] = valueOf(positions[i]);
            });
        
        float largest = Float.NEGATIVE_INFINITY;
        int largestIndex = -1;
        for (int i=0; i<scoreMap.length; i++) {
            if (scoreMap[i] >= largest) {
                largest = scoreMap[i];
                largestIndex = i;
            }
        }
        //System.out.println(dataPoints);
        //System.out.println("Score Map: " + Arrays.toString(scoreMap));
        //System.out.println(Arrays.deepToString(positions));
        
        return positions[largestIndex];
    }*/

    /*final float[] position(int index) {
        final float intervalSize = 2*weightRange/nIntervals;

        float[] position = new float[unfixedDim];
        for (int i=0; i<unfixedDim; i++) {
            float offset = rand.nextFloat()*intervalSize;
            int slot = index%nIntervals;
            index /= nIntervals;
            position[i] = slot*intervalSize + offset;
        }
        return position;
    }*/
    
    /**
     * computes a^n for n >= 0.
     */
    public static int pow(int a, int n) {
        int r = 1;
        int e = a;
        while (n>0) {
            if (n%2 == 1) {
                r *= e;
            }
            e *= e;
            n /= 2;
        }
        return r;
    }

    private class DataPoint {
        public final float[] weights;
        public final float mean;
        public final float sd;
        
        public DataPoint(float[] copiedWeights, float[] results) {
            this.weights = copiedWeights;
            this.mean = results[0];
            this.sd = results[1];
        }
        
        public float score() {
            float value = mean;
            //if (value < 1) value -= 500;
            //if (value < 2) value -= 100;
            //else if (value < 5) value -= 20;
            //else if (value < 10) value -= 5;
            //else if (value > 100) value += 100;
            return value;
        }
        
        public String toString() {
            return mean + "|"+sd+"|"+score();
        }
    }
    
    public float valueOf(float[] currentPosition) {
        double totalValue = dataPoints
                .parallelStream()
                .mapToDouble(dataPoint -> computeValue(currentPosition, dataPoint))
                .sum();
        double normalizer = dataPoints
                .parallelStream()
                .mapToDouble(dataPoint -> individualSize(currentPosition, dataPoint))
                .sum();
        float value = (float)(totalValue/normalizer);
        return normalizer == 0 ? 0 : value;
    }

    private double individualSize(float[] currentPosition, DataPoint dataPoint) {
        return computeSize(currentPosition, dataPoint.weights);
    }

    private double computeValue(float[] currentPosition, DataPoint dataPoint) {
        double size = computeSize(currentPosition, dataPoint.weights);
        return size*dataPoint.score();
    }

    private double computeSize(float[] vector1, float[] vector2) {
        double dot = dotProduct(vector1, vector2);
        if (dot < 1) return 1;
        else return dot;
        
        /*double distance = computeScaledDistance(vector1, vector2);
        double val = distance/weightRange; // e^-(x^4)
        val *= val;
        val *= val;
        val = Math.exp(-val);
        if (val < 0.1f) val = 0;
        return val;*/
    }
    
    private double dotProduct(float[] vector1, float[] vector2) {
        double sum = 0;
        for (int i=0; i<vector1.length; ++i) {
            sum += vector1[i]*vector2[i];
        }
        return sum;
    }

    private double computeDistance(float[] vector1, float[] vector2) {
        float sumSquares = 0;
        for (int i=0; i<vector1.length; i++) {
            float di = vector1[i] - vector2[i];
            sumSquares += di*di;
        }
        return Math.sqrt(sumSquares);
    }

    private double computeScaledDistance(float[] vector1, float[] vector2) {
        float sumSquares = 0;
        for (int i=0; i<vector1.length; i++) {
            float ri = (Math.abs(vector1[i]) + Math.abs(vector2[i]))/2 + 0.1f;
            float di = vector1[i] - vector2[i];
            
            di = di*di;
            if (vector1[i] > 0 != vector2[i] > 0) {
                di += 1000;
            }
            
            sumSquares += di / Math.pow(ri, 0.2f);
            //System.out.println(di + " " + (di / Math.pow(ri, 0.2f)));
        }
        return Math.sqrt(sumSquares);
    }

    private void setFixedWeights(float[] weights) {
        for (Map.Entry<Integer, Float> entry : fixedWeights.entrySet()) {
            int key = entry.getKey();
            float value = entry.getValue();
            weights[key] = value;
        }
    }
}


interface WeightAdjuster {
    /**
     * @return a string if it has something to report.
     */
    public String adjust(float[] results, float[] weights);
    
    /**
     * Indicates that a weight should not be changed by the adjuster.
     * @param index the index of the feature weight to fix.
     * @param value The value to fix the weight to.
     */
    public void fixValue(int index, float value);
}

