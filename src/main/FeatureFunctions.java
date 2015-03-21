package main;

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
	 * returns the height of the tallest "skyscrapers"
	 */
	public static float maximumColumnHeight(NextState nextState) {
		int maximumHeight = Integer.MIN_VALUE;
		int top[] = nextState.getTop();
		for (int x:top) {
			if (x > maximumHeight) {
				maximumHeight = x;
			}
		}
		return maximumHeight;
	}

	/**
	 * returns the total height of all "skyscrapers"
	 */
	public static float totalColumnsHeight(NextState nextState) {
		int totalHeight = 0;
		int top[] = nextState.getTop();
		for (int x:top) {
			totalHeight += x;
		}
		return totalHeight;
	}


	/**
	 * returns value 1 if lost, 0 if not lost
	 */
	public static float lost(NextState nextState) {
		return nextState.hasLost() ? 1: 0;
	}

    /**
     * returns the total number of holes
     */
    public static float totalHoles(NextState nextState) {
        int totalHoles = 0;
        int field[][] = nextState.getField();
        int top[] = nextState.getTop();
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (field[i][j] == 0 && i < top[j]) {
                    ++totalHoles;
                }
            }
        }
        return totalHoles;
    }


    /**
     * returns the total number of holes
     */
    public static float totalHolePieces(NextState nextState) {
        int totalHoles = 0;
        int field[][] = nextState.getField();
        int top[] = nextState.getTop();
        for (int j = 0; j < State.COLS; ++j) {
            boolean last = false;
            for (int i = 0; i < top[j]; ++i) {
                if (field[i][j] == 0) {
                    if (!last) {
                        ++totalHoles;
                    }
                    last = true;
                } else {
                    last = false;
                }
            }
        }
        return totalHoles;
    }

	/**
	 * returns the 'bumpiness of the top layer of 'skyscrapers'
	 * minimize to ensure the top layer of the grid is as flat as possible to prevent deep 'wells'
	 */
	public static float bumpiness(NextState nextState) {
		int bumpValue = 0;
		int field[][] = nextState.getField();
		int top[] = nextState.getTop();

		for(int i=0; i<State.COLS-1; i++) {
			bumpValue += Math.abs(top[i] - top[i+1]);
		}
		return bumpValue;
	}

	/**
	 * returns number of completed lines 
	 * maximize to ensure continuity of the game
	 */
	public static float completedLines(NextState nextState) {
		int numCompletedLines = 0;
		int field[][] = nextState.getField();

		for (int i = 0; i < State.ROWS; ++i) {
			boolean flag = true;
			for (int j = 0; j < State.COLS; ++j) {
				if (field[i][j] == 0) {
					flag = false;
				}
			}
			if(flag == true) {
				numCompletedLines++;
			}
		}
		return numCompletedLines;
	}
	
	/**
	 * returns number of completed cells in the field
	 **/
	public static float totalFilledCells(NextState nextState) {
		int filledCells = 0;
		int field[][] = nextState.getField();
	
		for(int i = 0; i < State.ROWS; i++) {
			for(int j = 0; j < State.COLS; j++) {
				if(field[i][j] != 0) {
					filledCells++;
				}
			}
		}
	
		return filledCells;
	}
    
	/**
	 * return the min max value of highest column height
	 */
	public static float minMaximumColumnHeight(NextState nextState) {
	    float worstPiece = Float.POSITIVE_INFINITY;
	    for (int i = 0; i < State.N_PIECES; ++i) {
	        float bestMove = Float.NEGATIVE_INFINITY;
	        for (int[] j:NextState.legalMoves[i]) {
	            NextState ns = NextState.generate(nextState,i,j);
	            float maxColumnHeight = maximumColumnHeight(ns);
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
            for (int[] j:NextState.legalMoves[i]) {
                NextState ns = NextState.generate(nextState,i,j);
                float maxColumnHeight = totalHoles(ns);
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
	
	/*
	 * return the difference of highest top and lowest top
	 */
	public static float differenceHigh(NextState nextState) {
	    float highestColumn = Float.NEGATIVE_INFINITY;
	    float lowestColumn = Float.POSITIVE_INFINITY;
	    int top[] = nextState.getTop();
        for (int x:top) {
            if (x > highestColumn) {
                highestColumn = x;
            }
            if (x < lowestColumn) {
                lowestColumn = x;
            }
        }
        return highestColumn - lowestColumn;
	}
	

    
    
    private static final float minimaxRec(NextState ns, Feature feature, float alpha, float beta, int depth) {
        if (ns.lost == true) {
            return -99999f;
        }
        if (depth <= 0) {
            return feature.compute(ns);
        }
        
        // MIN PLAYER
        for (int i = 0; i < State.N_PIECES; ++i) {
            // MAX PLAYER
            float newAlpha = alpha;
            int[][] legalMoves = NextState.legalMoves[i];
            for (int j=0; j<legalMoves.length; ++j) {
                NextState nns = NextState.generate(ns,i,legalMoves[j]);
                float score = minimaxRec(nns, feature, newAlpha, beta, depth-1);
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
            // MIN PLAYER
            float beta = Float.POSITIVE_INFINITY;
            for (int i = 0; i < State.N_PIECES; ++i) {
                // MAX PLAYER
                float newAlpha = Float.NEGATIVE_INFINITY;
                int[][] legalMoves = NextState.legalMoves[i];
                for (int j=0; j<legalMoves.length; ++j) {
                    NextState ns = NextState.generate(nextState,i,legalMoves[j]);
                    float score = minimaxRec(ns, feature, newAlpha, beta, depth-1);
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
    
}

