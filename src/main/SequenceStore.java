package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
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