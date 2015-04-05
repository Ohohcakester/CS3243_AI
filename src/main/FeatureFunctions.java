package main;

import java.util.Arrays;

import players.Feature;

/**
 * FeatureFunctions is only for Feature Functions that are well defined.
 * (The name must be descriptive)
 * i.e. once you implement it, you will never change it.
 * That's because changing the functions will affect already implemented algos!!!
 */
public class FeatureFunctions {

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
     * Returns the (maximum column height) ^ 3.
     */
    public static float maxHeightCubed(NextState nextState) {
        float maxHeight = maxHeight(nextState);

        return (float) Math.pow(maxHeight, 3);
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
        int numFilledCells = 0;

        int field[][] = nextState.field;
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (field[i][j] != 0) {
                    numFilledCells++;
                }
            }
        }

        return numFilledCells;
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
        int numEmptyCells = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; j++) {
            for (int i = 0; i < top[j]; i++) {
                if (field[i][j] == 0) {
                    numEmptyCells++;
                }
            }
        }

        return numEmptyCells;
    }

    /**
     * Returns the number of holes.
     */
    public static float numHoles(NextState nextState) {
        int numHoles = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; j++) {
            for (int i = 0; i < top[j]; i++) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    numHoles++;
                }
            }
        }

        return numHoles;
    }
    /**
     * Returns the number of holes, to a given power.
     */
    public static float numHolesPow(NextState nextState, int power) {
        float numHoles = numHoles(nextState);

        return (float) Math.pow(numHoles, power);
    }

    /**
     * Returns the sum of empty cell distances from the top.
     */
    public static float sumEmptyCellDistanceFromTop(NextState nextState) {
        int sumDistance = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; j++) {
            for (int i = 0; i < top[j]; i++) {
                if (field[i][j] == 0) {
                    sumDistance += top[j] - i;
                }
            }
        }

        return sumDistance;
    }

    /**
     * Returns the sum of hole distances from the top.
     */
    public static float sumHoleDistanceFromTop(NextState nextState) {
        int sumDistance = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; j++) {
            for (int i = 0; i < top[j]; i++) {
                if (field[i][j] == 0 && field[i + 1][j] != 0) {
                    sumDistance += top[j] - i;
                }
            }
        }

        return sumDistance;
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
    public static float numColumnsWithEmptyCell(NextState nextState) {
        int total = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int j = 0; j < State.COLS; j++) {
            for (int i = 0; i < top[j]; i++) {
                if (field[i][j] == 0) {
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
    public static float numRowsWithEmptyCell(NextState nextState) {
        int total = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (i >= top[j]) {
                    continue;
                }

                if (field[i][j] == 0) {
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
    public static float numRowsWithMoreThanOneEmptyCell(NextState nextState) {
        int total = 0;

        int field[][] = nextState.field;
        int top[] = nextState.top;
        for (int i = 0; i < State.ROWS; i++) {
            int numHoles = 0;

            for (int j = 0; j < State.COLS; j++) {
                if (i >= top[j]) {
                    continue;
                }

                if (field[i][j] == 0) {
                    numHoles++;
                }
            }

            if (numHoles > 1) {
                total++;
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
    public static float WeightedFilledCells(NextState nextState) {
        int weightedFilledCells = 0;
        int field[][] = nextState.getField();

        for (int i = 1; i <= State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] != 0) {
                    weightedFilledCells += i;
                }
            }
        }

        return weightedFilledCells;
    }
}
