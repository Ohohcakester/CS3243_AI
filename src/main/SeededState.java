package main;

import java.util.Random;

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
