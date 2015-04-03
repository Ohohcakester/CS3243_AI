package main;

import java.util.ArrayList;
import java.util.Arrays;

public class Sequence implements Comparable<Sequence> {
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