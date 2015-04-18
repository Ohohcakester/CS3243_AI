package main;

import java.util.Random;

/**
 * A state that generates a fixed sequence of pieces from a random seed.
 * This is used for a fair comparison between states in our Genetic Algorithm.
 * For each of the different set of weights in our GA, we give them the same game.
 */
public class SeededState extends State {
    private Random rand;
    
    public SeededState(int seed) {
        rand = new Random(seed);
        nextPiece = rand.nextInt(N_PIECES);
    }
    
    public void makeMove(int[] move) {
        super.makeMove(move);
        nextPiece = rand.nextInt(N_PIECES);
    }
}
