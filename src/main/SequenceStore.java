package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

/**
 * Format: Score / Sequence, unsorted.
 * 65525 216414371236757415273521
 * 2153 10124621041520450120320121504105230125306
 * 412 0123621046466213502121546210
 * 
 * @author Oh
 *
 */
public class SequenceStore {
    
    public static final String SEPARATOR_ROW = "\n";
    
    public static final String PATH = "SequenceData\\";
    public static final String FILE_MAIN = "main.txt";
    public static final String FILE_AUX_PREFIX = "aux";
    public static final Random rand = new Random();
    public final String auxiliaryFile = randomAuxiliaryFile();

    private final TreeSet<Sequence> sequenceSet;
    
    private SequenceStore() {
        sequenceSet = new TreeSet<>();
    }
    
    public static SequenceStore empty() {
        return new SequenceStore();
    }
    
    public static SequenceStore load() {
        SequenceStore store = new SequenceStore();
        store.loadSequences();
        return store;
    }
    
    private static String randomAuxiliaryFile() {
        int randFileNo = rand.nextInt(2147483647);
        return FILE_AUX_PREFIX + randFileNo + ".txt";
    }
    
    public void addSequence(int score, int[] pieces) {
        sequenceSet.add(new Sequence(score, pieces));
    }
    
    public void saveSequences() {
        System.out.println("Saving sequences to " + auxiliaryFile);
        
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
    }
    
    
    public void loadSequences() {
        sequenceSet.clear();
        loadSequencesWithoutClearing();
    }
    
    public void loadSequencesWithoutClearing() {
        try {
            FileReader fr = new FileReader(PATH + FILE_MAIN);
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
        HashSet<String> lines = new HashSet<>();
        
        File dir = new File(PATH);
        File[] files = dir.listFiles((file, name) -> name.endsWith(".txt"));
        for (File file : files) {
            try {
                System.out.println("Read file " + file.getName());
                FileReader fr = new FileReader(file.getPath());
                BufferedReader br = new BufferedReader(fr);
                String s = br.readLine();
                while (s != null) {
                    lines.add(s);
                    s = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            PrintWriter pw = new PrintWriter(PATH + FILE_MAIN);
            for (String line : lines) {
                pw.print(line);
                pw.print(SEPARATOR_ROW);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (File file : files) {
            if (!file.getName().equals(FILE_MAIN)) {
                System.out.println("Delete file " + file.getName());
                file.delete();
            }
        }
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



class Sequence implements Comparable<Sequence> {
    public final int score;
    public final int[] pieces;
    
    public Sequence(int score, int[] pieces) {
        this.score = score;
        this.pieces = pieces;
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

        pieces = new int[pieceList.size()];
        for (int i=0; i<pieces.length; ++i) {
            pieces[i] = pieceList.get(i);
        }
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
    public int compareTo(Sequence o) {
        int cmp = score - o.score;
        if (cmp != 0) return -cmp;
        
        int i = 0;
        while (i < pieces.length && i < o.pieces.length) {
            cmp = pieces[i] - o.pieces[i];
            if (cmp != 0) return -cmp;
            i++;
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
    
}