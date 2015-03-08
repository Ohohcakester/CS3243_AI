package main;

/**
 * FeatureFunctions is only for Feature Functions that are well defined.
 * (The name must be descriptive)
 * i.e. once you implement it, you will never change it.
 * That's because changing the functions will affect already implemented algos!!!
 */
public class FeatureFunctions {

	public static float exampleFeature(State s, NextState nextState) {
		return 4;
	}

	/**
	 * returns the height of the tallest "skyscrapers"
	 */
	public static float maximumColumnHeight(State state, NextState nextState) {
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
	public static float totalColumnsHeight(State state, NextState nextState) {
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
	public static float lost(State state, NextState nextState) {
		return nextState.hasLost() ? 1: 0;
	}

	/**
	 * returns the total number of holes
	 */
	public static float totalHoles(State state, NextState nextState) {
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
	 * returns the 'bumpiness of the top layer of 'skyscrapers'
	 * minimize to ensure the top layer of the grid is as flat as possible to prevent deep 'wells'
	 */
	public static float bumpiness(State state, NextState nextState) {
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
	public static float completedLines(State state, NextState nextState) {
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
	public static float totalFilledCells(State state, NextState nextState) {
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
}
