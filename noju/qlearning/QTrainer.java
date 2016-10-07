package pacman.noju.qlearning;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Node;

public class QTrainer {
	
	private static final float discountFactor = 0.9f;
	private static final float learningRate = 0.2f;
	private static final int RUNS_WITHOUT_NEW_STATES = 1000;
	private QTable table;

	private List<Integer> junctions;
	
	
	public static void main(String[] args)
	{
		//new QTrainer().train(0.7f, 10000);
		new QTrainer().run(40, 1.0f, true);
	}
	
	private void train(float exploit, int testInterval) {
		
		QLearner learner = new QLearner(table);
		
		int loops = 0;
		int runs = 0;
		int interval = testInterval;
		while(true){
			
			float scoreA = runExperiment(new Legacy(), learner, 1.0f, false);
			float scoreB = runExperiment(new Legacy(), learner, 1.0f, false);
			float scoreC = runExperiment(new Legacy(), learner, 1.0f, false);
			float scoreD = runExperiment(new Legacy(), learner, 1.0f, false);
			float scoreE = runExperiment(new Legacy(), learner, 1.0f, false);
			float avg = (scoreA + scoreB + scoreC + scoreD + scoreE) / 5;
			float dev = ((scoreA-avg)*(scoreA-avg) + (scoreB-avg)*(scoreB-avg)
									+ (scoreC-avg)*(scoreC-avg) + (scoreD-avg)*(scoreD-avg)
									+ (scoreE-avg)*(scoreE-avg)) / 4;
			dev = (float) Math.sqrt(dev);
			
			System.out.println("Loop: " + loops + ", runs: " + runs + ", score: " + avg + " [" + scoreA + "," + scoreB + "," + scoreC + "," + scoreD + "," + scoreE + "] " + "dev: " + dev + ", states: " + table.size());
			
			if (loops == 0)
				interval = testInterval / 20;
			else if (loops == 1)
				interval = testInterval / 10;
			else if (loops == 2)
				interval = testInterval / 2;
			else
				interval = testInterval;
			loops++;
			
			exploit = (float) Math.random();
			
			for(int i = 0; i < interval; i++)  {
				runs++;
				runExperiment(new Legacy(), learner, exploit, false);
				//System.out.println("Run: " + i + "\tscore: " + score + "\texploit: " + exploit + "\tstates: " + table.size());
			}	
			
			persist("states.dat");
			
		}
		
	}

	public QTrainer(){
		super();
		loadStates("states_long.dat");
	}
	
	public void run(int runs, float exploit, boolean visual){
		
		QLearner learner = new QLearner(table);
		if (runs != -1){
			for(int i = 0; i < runs; i++)  {
				int score = runExperiment(new Legacy(), learner, exploit, visual);
				System.out.println("Run: " + i + "\tscore: " + score + "\texploit: " + exploit + "\tstates: " + table.size());
			}
		} else {
			int runsWithoutNewState = 0;
			int statesBefore = table.size();
			int i = 0;
			while(true){
				i++;
				int score = runExperiment(new Legacy(), learner, exploit, visual);
				int statesNow = table.size();
				if (statesBefore < statesNow){
					runsWithoutNewState = 0;
					statesBefore = statesNow;
				} else {
					runsWithoutNewState++;
				}
				System.out.println("Run: " + i + "\tscore: " + score + "\texploit: " + exploit + "\tstates: " + table.size()  + "\t Runs without new states: " + runsWithoutNewState);
				
				if (runsWithoutNewState > RUNS_WITHOUT_NEW_STATES){
					break;
				}
			}
		}
		System.out.println("Traning finished.");
		//persist("states.dat");
		
	}

	public int runExperiment(Controller<EnumMap<GHOST,MOVE>> ghostController, QLearner learner, float exploit, boolean visual) {

		Game game=new Game(0);
		
		if (table.isEmpty())
			table.put(new QState(game), generateMoveMap());

		GameView gv=null;
		
		int delay = 0;
		if(visual){
			gv=new GameView(game).showGame();
			delay = 8;
		}
		
		int lastLevel = -1;
		int lastScore = 0;
		int lastReward = 0;
		Game lastGame = game;
		MOVE lastMove = lastGame.getPacmanLastMoveMade();
		QState lastState = new QState(game);
		
		while(!game.gameOver())
		{
			
			// Load junctions if new level
			int level = game.getCurrentLevel();
			
			if (junctions == null || lastLevel != level)
				junctions = getJunctions(game);
			
			lastLevel = level;
			
			// Update Q-table
			int pacman = game.getPacmanCurrentNodeIndex();
			boolean inJunction = false;
			if (junctions.contains(pacman)){
				
				int reward = game.getScore() - lastScore;
				QState state = new QState(game.copy());
				
				updateQTable(state, reward, learningRate, discountFactor, lastState, lastMove, lastReward);
				
				lastReward = reward;
				lastState = state;
				lastScore = game.getScore();
				inJunction = true;
				
			}
			
			// Advance game
			MOVE move = learner.bestMove(game.copy(), exploit, inJunction);
			lastMove = move;
	        game.advanceGame(move,ghostController.getMove(game.copy(),-1));
	        
	        // Repaint
	        if(visual){
	        	gv.repaint();
	        
		        try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	        
		}
		
		return game.getScore();
	}
	
	private HashMap<MOVE, Float> generateMoveMap() {
		
		HashMap<MOVE, Float> map = new HashMap<MOVE, Float>();
		
		map.put(MOVE.UP, 0f);
		map.put(MOVE.RIGHT, 0f);
		map.put(MOVE.LEFT, 0f);
		map.put(MOVE.DOWN, 0f);
		
		return map;
		
	}

	private float max(HashMap<MOVE, Float> map) {
		
		float bestValue = Integer.MIN_VALUE;
		for(MOVE move : map.keySet()){
			
			if (map.get(move) > bestValue)
				bestValue = map.get(move);
			
		}
		
		return bestValue;
		
	}

	private void updateQTable(QState state, int reward, float learningRate, float discountFactor, QState lastState, MOVE lastMove, int lastReward) {
		
		if (!table.containsKey(state))
			table.put(state, generateMoveMap());
		
		float inverseLearningRate = 1f-learningRate;
		float oldValue = table.get(lastState).get(lastMove);
		float futureEstimate = max(table.get(state));
		
		float learnedValue = (float)reward + discountFactor * futureEstimate;
		
		float q = (inverseLearningRate * oldValue) + (learningRate * learnedValue);
		
		table.get(lastState).put(lastMove, q);
		
	}
	
	public static List<Integer> getJunctions(Game game){
		List<Integer> junctions = new ArrayList<Integer>();
		
		int[] juncArr = game.getJunctionIndices();
		for(Integer i : juncArr)
			junctions.add(i);
		
		junctions.addAll(getTurns(game));
		
		return junctions;
		
	}
	
	private static List<Integer> getTurns(Game game) {
		
		List<Integer> turns = new ArrayList<Integer>();
		
		for(Node n : game.getCurrentMaze().graph){
			
			int down = game.getNeighbour(n.nodeIndex, MOVE.DOWN);
			int up = game.getNeighbour(n.nodeIndex, MOVE.UP);
			int left = game.getNeighbour(n.nodeIndex, MOVE.LEFT);
			int right = game.getNeighbour(n.nodeIndex, MOVE.RIGHT);
			
			if (((down != -1) != (up != -1)) || ((left != -1) != (right != -1))){
				turns.add(n.nodeIndex);
			} else if (down != -1 && up != -1 && left != -1 && right != -1){
				turns.add(n.nodeIndex);
			}
			
		}
		
		return turns;
	}
	
	private void persist(String filename) {
		
		//System.out.println("Generating output.");
		
		// Output
		String out = "";
		String tOut = "";
		int i = 0;
		for(QState state : table.keySet()){
			String stateOut = "";
			i++;
			if (i%1000==0){
				//System.out.println("Output for " + i + " states generated.");
				out += tOut;
				tOut = "";
			}
			
			stateOut += state.getLevel() + ";";
			stateOut += state.getJunction() + ";";
			
			stateOut += state.getDistanceUp() + ";";
			stateOut += state.isEdibleUp() + ";";
			
			stateOut += state.getDistanceRight() + ";";
			stateOut += state.isEdibleRight() + ";";
			
			stateOut += state.getDistanceDown() + ";";
			stateOut += state.isEdibleDown() + ";";
			
			stateOut += state.getDistanceLeft() + ";";
			stateOut += state.isEdibleLeft() + ";";
			
			stateOut += state.isFirstPP() + ";";
			stateOut += state.isSecondPP() + ";";
			stateOut += state.isThirdPP() + ";";
			stateOut += state.isFourthPP() + ";";
			
			if (table.get(state).containsKey(MOVE.UP))
				stateOut += table.get(state).get(MOVE.UP);
			stateOut += ";";
			if (table.get(state).containsKey(MOVE.RIGHT))
				stateOut += table.get(state).get(MOVE.RIGHT);
			stateOut += ";";
			if (table.get(state).containsKey(MOVE.DOWN))
				stateOut += table.get(state).get(MOVE.DOWN);
			stateOut += ";";
			if (table.get(state).containsKey(MOVE.LEFT))
				stateOut += table.get(state).get(MOVE.LEFT);
			stateOut += ";\n";
			tOut += stateOut;
		}	
		
		// Write to file
		//System.out.println("Ready to write file.");
        FileWriter fw = null;
		try {
			File old = new File(filename);
			if (old.exists()){
				old.delete();
				//System.out.println(filename + " deleted");
			}
			File file = new File(filename);
			fw = new FileWriter(file);
			fw.write(out);
			fw.close();
			System.out.println(filename + " saved with " + table.size() + " states.");
		} catch (FileNotFoundException e1) {
			System.out.println("Error saving " + filename + ". " + e1);
		} catch (IOException e2) {
			System.out.println("Error saving " + filename + ". " + e2);
		}
		
	}
	
	private void loadStates(String filename){
		
		table = new QTable();
		
		try {

            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                
            	String[] chunks = strLine.split(";");
            	
            	int level = Integer.parseInt(chunks[0]);
            	int junction = Integer.parseInt(chunks[1]);
            	int distanceUp = Integer.parseInt(chunks[2]);
            	boolean edibleUp = Boolean.parseBoolean(chunks[3]);
            	int distanceDown = Integer.parseInt(chunks[4]);
            	boolean edibleDown = Boolean.parseBoolean(chunks[5]);
            	int distanceRight = Integer.parseInt(chunks[6]);
            	boolean edibleRight = Boolean.parseBoolean(chunks[7]);
            	int distanceLeft = Integer.parseInt(chunks[8]);
            	boolean edibleLeft = Boolean.parseBoolean(chunks[9]);
            	
            	boolean firstPP = Boolean.parseBoolean(chunks[10]);
            	boolean secondPP = Boolean.parseBoolean(chunks[11]);
            	boolean thirdPP = Boolean.parseBoolean(chunks[12]);
            	boolean fourthPP = Boolean.parseBoolean(chunks[13]);
            	
            	float valueUp = Float.parseFloat(chunks[14]);
            	float valueRight = Float.parseFloat(chunks[15]);
            	float valueDown = Float.parseFloat(chunks[16]);
            	float valueLeft = Float.parseFloat(chunks[17]);
            	
            	QState state = new QState(	level, 
            								junction, 
            								distanceUp, 
            								edibleUp, 
            								distanceDown, 
            								edibleDown, 
            								distanceRight, 
            								edibleRight, 
            								distanceLeft, 
            								edibleLeft, 
            								firstPP,
            								secondPP,
            								thirdPP,
            								fourthPP);
            	
            	if (!table.containsKey(state))
            		table.put(state, new HashMap<MOVE, Float>());
            	
            	table.get(state).put(MOVE.UP, valueUp);
            	table.get(state).put(MOVE.RIGHT, valueRight);
            	table.get(state).put(MOVE.LEFT, valueLeft);
            	table.get(state).put(MOVE.DOWN, valueDown);
            	
            }
           
            in.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
		
	}
	
}
