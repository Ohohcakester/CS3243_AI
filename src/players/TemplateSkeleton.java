package players;
import main.State;
import main.TFrame;


public class TemplateSkeleton {

    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        
        return 0;
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        TemplateSkeleton p = new TemplateSkeleton();
        while(!s.hasLost()) {
            s.makeMove(0, p.pickMove(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }
    
}