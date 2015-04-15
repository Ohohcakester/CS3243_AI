package main;

import java.util.Arrays;
import java.util.Random;

public class PredeterminedState extends State {
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
