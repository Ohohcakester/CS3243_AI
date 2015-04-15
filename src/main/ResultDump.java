package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class ResultDump {

    public static final String PATH = "ResultDump/";
    public static final String RESULT_PREFIX = "result";
    
    public static final Random rand = new Random();
    

    private static String randomResultFile() {
        int randFileNo = rand.nextInt(2147483647);
        return RESULT_PREFIX + randFileNo + ".txt";
    }

    public static void saveResults(String results) {
        makeDirs(PATH);
        String auxiliaryFile = randomResultFile();
        System.out.println("Saving results to " + auxiliaryFile);

        try {
            PrintWriter pw = new PrintWriter(PATH + auxiliaryFile);
            pw.print(results);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        

    private static boolean makeDirs(String path) {
        return (new File(path)).mkdirs();
    }
}
