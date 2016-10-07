import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Q Learning sample class <br/>
 * <b>The goal of this code sample is for the character @ to reach the goal area G</b> <br/>
 * compile using "javac QLearning.java" <br/>
 * test using "java QLearning" <br/>
 *
 * @author A.Liapis (Original author), A. Hartzen (2013 modifications) 
 */
public class QLearning {    
    // --- variables
    protected final char[] startingmap={ '@',' ','#',
                                         ' ','#','G',
                                         ' ',' ',' ' };
    protected char[] map;

    // --- movement constants
    public static final int UP=0;
    public static final int RIGHT=1;
    public static final int DOWN=2;
    public static final int LEFT=3;

    QLearning(){
        resetMaze();
    }

    public char[] getMap(){
        return (char[])map.clone();
    }

    public void resetMaze(){
        map = (char[])startingmap.clone();
    }

    public int getActionRange(){
        return 4;
    }

    /**
     * Returns the map state which results from an initial map state after an
     * action is applied. In case the action is invalid, the returned map is the
     * same as the initial one (no move).
     * @param action taken by the avatar ('@')
     * @param current map before the action is taken
     * @return resulting map after the action is taken
     */
    public char[] getNextState(int action, char[] map){
        char[] nextMap = (char[])map.clone();
        // get location of '@'
        int avatarIndex = getAvatarIndex(map);
        if(avatarIndex==-1){
            return nextMap; // no effect
        }
        int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
        if(nextAvatarIndex>=0 && nextAvatarIndex<map.length){
            if(nextMap[nextAvatarIndex]!='#'){
                // change the map
                nextMap[avatarIndex]=' ';
                nextMap[nextAvatarIndex]='@';
            }
        }
        return nextMap;
    }

    public char[] getNextState(int action){
        char[] nextMap = (char[])map.clone();
        // get location of '@'
        int avatarIndex = getAvatarIndex(map);
        if(avatarIndex==-1){
            return nextMap; // no effect
        }
        int nextAvatarIndex = getNextAvatarIndex(action, avatarIndex);
        if(nextAvatarIndex>=0 && nextAvatarIndex<map.length){
            if(nextMap[nextAvatarIndex]!='#'){
                // change the map
                nextMap[avatarIndex]=' ';
                nextMap[nextAvatarIndex]='@';
            }
        }
        return nextMap;
    }

    public void goToNextState(int action){
        map=getNextState(action);
    }

    public boolean isValidMove(int action){
        char[] nextMap=getNextState(action);
        return (nextMap==map);
    }

    public boolean isValidMove(int action, char[] map){
        char[] nextMap=getNextState(action, map);
        return (nextMap==map);
    }

    public int getAvatarIndex(){
        int avatarIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='@'){ avatarIndex=i; }
        }
        return avatarIndex;
    }

    public int getAvatarIndex(char[] map){
        int avatarIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='@'){ avatarIndex=i; }
        }
        return avatarIndex;
    }

    public boolean isGoalReached(){
        int goalIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='G'){ goalIndex=i; }
        }
        return (goalIndex==-1);
    }

    public boolean isGoalReached(char[] map){
        int goalIndex = -1;
        for(int i=0;i<map.length;i++){
            if(map[i]=='G'){ goalIndex=i; }
        }
        return (goalIndex==-1);
    }

    public int getNextAvatarIndex(int action, int currentAvatarIndex){
        int x = currentAvatarIndex%3;
        int y = currentAvatarIndex/3;
        if(action==UP){
            y--;
        }
        if(action==RIGHT){
            x++;
        } else if(action==DOWN){
            y++;
        } else if(action==LEFT){
            x--;
        }
        if(x<0 || y<0 || x>=3 || y>=3){
            return currentAvatarIndex; // no move
        }
        return x+3*y;
    }

    public void printMap(){
        for(int i=0;i<map.length;i++){
            if(i%3==0){
                System.out.println("+-+-+-+");
            }
            System.out.print("|"+map[i]);
            if(i%3==2){
                System.out.println("|");
            }
        }
        System.out.println("+-+-+-+");
    }

    public void printMap(char[] map){
        for(int i=0;i<map.length;i++){
            if(i%3==0){
                System.out.println("+-+-+-+");
            }
            System.out.print("|"+map[i]);
            if(i%3==2){
                System.out.println("|");
            }
        }
        System.out.println("+-+-+-+");
    }

    public String getMoveName(int action){
        String result = "ERROR";
        if(action==UP){
            result="UP";
        } else if(action==RIGHT){
            result="RIGHT";
        } else if(action==DOWN){
            result="DOWN";
        } else if(action==LEFT){
            result="LEFT";
        }
        return result;
    }

    void runLearningLoop() throws Exception {
        QTable q = new QTable(getActionRange());
        int moveCounter=0;

        while(true){
            // PRINT MAP
            System.out.println("MOVE "+moveCounter);
            printMap();
            // CHECK IF WON, THEN RESET
            if(isGoalReached()){
                System.out.println("GOAL REACHED IN "+moveCounter+" MOVES!");
                resetMaze();
                moveCounter=0;
                return;
            }

            // DETERMINE ACTION
            int action = q.getNextAction(getMap());
            System.out.println("MOVING: "+getMoveName(action));
            goToNextState(action);
            moveCounter++;

            // REWARDS AND ADJUSTMENT OF WEIGHTS SHOULD TAKE PLACE HERE


            // COMMENT THE SLEEP FUNCTION IF YOU NEED FAST TRAINING WITHOUT
            // NEEDING TO ACTUALLY SEE IT PROGRESS
            //Thread.sleep(1000);
        }
    }


    /**
     * Q-learning maze-solving testing method
     * @param args
     */
    public static void main(String s[]) {
        QLearning app = new QLearning();
        try{
            app.runLearningLoop();
        } catch (Exception e){
            System.out.println("Thread.sleep interrupted!");
        }
    }
};
