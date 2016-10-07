import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
     * The heart of the Q-learning algorithm, the QTable contains the table
     * which maps states, actions and their Q values. This class has elaborate
     * documentation, and should be the focus of the students' body of work
     * for the purposes of this tutorial.
     *
     * @author A.Liapis (Original author), A. Hartzen (2013 modifications) 
     */
    public class QTable {
        /**
         * for creating random numbers
         */
        Random randomGenerator;
        /**
         * the table variable stores the Q-table, where the state is saved
         * directly as the actual map. Each map state has an array of Q values
         * for all the actions available for that state.
         */
        HashMap<String, float[]> table;
        /**
         * the actionRange variable determines the number of actions available
         * at any map state, and therefore the number of Q values in each entry
         * of the Q-table.
         */
        int actionRange;

        // E-GREEDY Q-LEARNING SPECIFIC VARIABLES
        /**
         * for e-greedy Q-learning, when taking an action a random number is
         * checked against the explorationChance variable: if the number is
         * below the explorationChance, then exploration takes place picking
         * an action at random. Note that the explorationChance is not a final
         * because it is customary that the exploration chance changes as the
         * training goes on.
         */
        float explorationChance=0.4f;
        /**
         * the discount factor is saved as the gammaValue variable. The
         * discount factor determines the importance of future rewards.
         * If the gammaValue is 0 then the AI will only consider immediate
         * rewards, while with a gammaValue near 1 (but below 1) the AI will
         * try to maximize the long-term reward even if it is many moves away.
         */
        float gammaValue=0.9f;
        /**
         * the learningRate determines how new information affects accumulated
         * information from previous instances. If the learningRate is 1, then
         * the new information completely overrides any previous information.
         * Note that the learningRate is not a final because it is
         * customary that the learningRate changes as the
         * training goes on.
         */
        float learningRate=0.15f;

        //PREVIOUS STATE AND ACTION VARIABLES
        /**
         * Since in Q-learning the updates to the Q values are made ONE STEP
         * LATE, the state of the world when the action resulting in the reward
         * was made must be stored.
         */
        char[] prevState;
        /**
         * Since in Q-learning the updates to the Q values are made ONE STEP
         * LATE, the index of the action which resulted in the reward must be
         * stored.
         */
        int prevAction;

        /**
         * Q table constructor, initiates variables.
         * @param the number of actions available at any map state
         */
        QTable(int actionRange){
            randomGenerator = new Random();
            this.actionRange=actionRange;
            table = new HashMap<String, float[]>();
        }

        /**
         * For this example, the getNextAction function uses an e-greedy
         * approach, having exploration happen if the exploration chance
         * is rolled.
         *
         * @param the current map (state)
         * @return the action to be taken by the calling program
         */
        int getNextAction(char[] map){
            prevState=(char[])map.clone();
            if(randomGenerator.nextFloat()<explorationChance){
                prevAction=explore();
            } else {
                prevAction=getBestAction(map);
            }
            return prevAction;
        }

        /**
         * The getBestAction function uses a greedy approach for finding
         * the best action to take. Note that if all Q values for the current
         * state are equal (such as all 0 if the state has never been visited
         * before), then getBestAction will always choose the same action.
         * If such an action is invalid, this may lead to a deadlock as the
         * map state never changes: for situations like these, exploration
         * can get the algorithm out of this deadlock.
         *
         * @param the current map (state)
         * @return the action with the highest Q value
         */
        int getBestAction(char[] map){
            return 0;
        }

        /**
         * The explore function is called for e-greedy algorithms.
         * It can choose an action at random from all available,
         * or can put more weight towards actions that have not been taken
         * as often as the others (most unknown).
         *
         * @return index of action to take
         */
        int explore(){
			return 0;
        }

        /**
         * The updateQvalue is the heart of the Q-learning algorithm. Based on
         * the reward gained by taking the action prevAction while being in the
         * state prevState, the updateQvalue must update the Q value of that
         * {prevState, prevAction} entry in the Q table. In order to do that,
         * the Q value of the best action of the current map state must also
         * be calculated.
         *
         * @param reward at the current map state
         * @param the current map state (for finding the best action of the
         * current map state)
         */
        void updateQvalue(int reward, char[] map){

        }

		/**
		 * This helper function is used for entering the map state into the
		 * HashMap
		 * @param map
		 * @return String used as a key for the HashMap
		 */
		String getMapString(char[] map){ 
			String result="";
			for(int x=0;x<map.length;x++){
				result+=""+map[x];
				if(x>0 && x%3==0){ result+="\n"; }
			}
			return result;
		}
        /**
         * The getActionsQValues function returns an array of Q values for
         * all the actions available at any state. Note that if the current
         * map state does not already exist in the Q table (never visited
         * before), then it is initiated with Q values of 0 for all of the
         * available actions.
         *
         * @param the current map (state)
         * @return an array of Q values for all the actions available at any state
         */
        float[] getActionsQValues(char[] map){
            float[] actions = getValues(map);
            if(actions==null){
                float[] initialActions = new float[actionRange];
                for(int i=0;i<actionRange;i++) initialActions[i]=0.f;
                table.put(getMapString(map), initialActions);
                return initialActions;
            }
			return actions;
        }
        /**
         * printQtable is included for debugging purposes and uses the
         * action labels used in the maze class (even though the Qtable
         * is written so that it can more generic).
         */
        void printQtable(){
            Iterator iterator = table.keySet().iterator();
            while (iterator.hasNext()) {
               char[] key = (char[])iterator.next();
               float[] values = getValues(key);

               System.out.print(key[0]+""+key[1]+""+key[2]);
               System.out.println("  UP   RIGHT  DOWN  LEFT" );
               System.out.print(key[3]+""+key[4]+""+key[5]);
               System.out.println(": " + values[0]+"   "+values[1]+"   "+values[2]+"   "+values[3]);
               System.out.println(key[6]+""+key[7]+""+key[8]);
            }
        }

        /**
         * Helper function to find the Q-values of a given map state.
         *
         * @param the current map (state)
         * @return the Q-values stored of the Qtable entry of the map state, otherwise null if it is not found
         */
        float[] getValues(char[] map){
            String mapKey = getMapString(map);
            if(table.containsKey(mapKey)){
                return table.get(mapKey);
            }
            return null;
        }
    };