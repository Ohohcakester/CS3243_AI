package main;

public class PredeterminedState extends State {
    int[] pieces;
    int pieceIndex;
    
    public PredeterminedState(int[] pieces) {
        this.pieces = pieces.clone();
        pieceIndex = 0;
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
