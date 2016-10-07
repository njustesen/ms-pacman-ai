package pacman.noju.mcts;

import static pacman.game.Constants.DELAY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Maze;
import pacman.game.internal.Node;
import pacman.game.Game;

public class MCTS extends Controller<MOVE>{

	public static final int NEW_LIFE_VALUE = 0;
	public static final int LOST_LIFE_VALUE = -500;
	private static final int SIM_STEPS = 200;
	private static final int TREE_TIME_LIMIT = 55;
	private static final int MISSUSE_OF_POWER_PILL = -50;
	private static final int GHOST_DISTANCE = 200;
	// Hoeffding ineqality
	float C = (float) (1f / Math.sqrt(2));
	Controller<EnumMap<GHOST,MOVE>> ghosts = new Legacy();
	
	public static Set<Integer> junctions;
	int lastLevel = 1;
	Maze maze3;
	boolean useScript = false;
	MOVE scriptMove = MOVE.LEFT;
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		
		int level = game.getCurrentLevel();
		
		if (junctions == null || lastLevel != level){
			junctions = getJunctions(game);
		}
		
		lastLevel = level;
		
		return MctsSearch(game, 38);
		
	}

	private MOVE MctsSearch(Game game, long ms) {
		
		long start = new Date().getTime();
		MctsNode v0 = new MctsNode(new MctsState(true, game), null, game.getPacmanLastMoveMade(), 0);
		
		while(new Date().getTime() < start + ms){
			
			MctsNode v1 = treePolicy(v0);
			
			if (v1 == null)
				return MOVE.DOWN;
			
			int score = defaultPolicy(v1, v0);
			
			backup(v1, score);
			
		}
		
		MctsNode bestNode = bestChild(v0, 0);
		MOVE move = MOVE.UP;
		if (bestNode != null)
			move = bestNode.getMove();
		
		/*
		System.out.println(v0.print(0));
		System.out.println(move);
		*/
		
		return move;
		
	}

	private MctsNode treePolicy(MctsNode node) {
		
		if (node.isExpandable()){
			if (node.getTime() <= TREE_TIME_LIMIT)
				return node.expand();
			else
				return node;
		}
		
		if (node.getState().isAlive())
			return treePolicy(bestChild(node, C));
		else
			return node;
			
	}
	
	private MctsNode bestChild(MctsNode v, float c) {
		
		float bestValue = -99999999;
		MctsNode urgent = null;
		
		for(MctsNode node : v.children){
			float value = UCT(node, c);
			
			if (!node.getState().isAlive())
				value = -99999;
			
			if (value > bestValue){
				if (c != 0 || dieTest(v, node)){
					urgent = node;
					bestValue = value;
				}
			}
		}
		
		return urgent;
	}

	private boolean dieTest(MctsNode v, MctsNode node) {
		
		Controller<MOVE> pacManController = new RandomJunctionPacman();
		Controller<EnumMap<GHOST,MOVE>> ghostController = ghosts;
    	
		Game game = v.getState().getGame().copy();
			
		int livesBefore = game.getPacmanNumberOfLivesRemaining();
		
		game.advanceGame(node.getMove(),
	        	ghostController.getMove(game.copy(),System.currentTimeMillis()));
	        
	    int livesAfter = game.getPacmanNumberOfLivesRemaining();
		if (livesAfter < livesBefore)
			return false;
		
		return true;
		
	}

	private float UCT(MctsNode node, float c) {
		
		float reward = node.getValue() / node.getVisited();
		reward = normalize(reward);
		
		float n = 0;
		if (node.getParent() != null)
			n = node.getParent().getVisited();
		
		float nj = node.getVisited();
		
		float uct = (float) (reward + 2 * c * Math.sqrt((2 * Math.log(n)) / nj));
		
		return uct;
		
	}

	private float normalize(float x) {	
		
		float min = -500;
		float max = 2000;
		float range = max - min;
		float inZeroRange = (x - min);
		float norm = inZeroRange / range;
		
		return norm;
	}

	private int defaultPolicy(MctsNode node, MctsNode root) {
		
		// Terminal
		if (!node.getState().isAlive() || 
				node.getState().getGame().getPacmanNumberOfLivesRemaining() < root.getState().getGame().getPacmanNumberOfLivesRemaining())
			return LOST_LIFE_VALUE;
		
		int result = runExperimentWithAvgScoreLimit(node, SIM_STEPS);
		
		return result -= root.getState().getGame().getScore();

	}

	private void backup(MctsNode v, int score) {
		
		v.setVisited(v.getVisited() + 1);
		v.setValue(v.getValue() + score);
		v.getSimulations().add(score);
		if (v.getParent() != null)
			backup(v.getParent(), score);
		
	}
	

	public static Set<Integer> getJunctions(Game game){
		Set<Integer> junctions = new HashSet<Integer>();
		
		int[] juncArr = game.getJunctionIndices();
		for(Integer i : juncArr)
			junctions.add(i);
		
		junctions.addAll(getTurns(game));
		
		return junctions;
		
	}
	
	private static Collection<? extends Integer> getTurns(Game game) {
		
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
	
	public int runExperimentWithAvgScoreLimit(MctsNode node, int steps) {
		
		Controller<MOVE> pacManController = new RandomJunctionPacman();
		Controller<EnumMap<GHOST,MOVE>> ghostController = ghosts;
    	
		Game game = node.getState().getGame().copy();
			
		int livesBefore = game.getPacmanNumberOfLivesRemaining();
		int ppBefore = game.getNumberOfActivePowerPills();
		int s = 0;
		int bonus = 0;
		while(!game.gameOver())
		{
			if (s >= steps && game.getNeighbouringNodes(game.getPacmanCurrentNodeIndex()).length > 2)
				break;
			
			
	        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()),
	        		ghostController.getMove(game.copy(),System.currentTimeMillis()));
	        s++;
	        int ppAfter = game.getNumberOfActivePowerPills();
	        if (ppAfter < ppBefore && avgDistanceToGhosts(game) > GHOST_DISTANCE){
	        	bonus += MISSUSE_OF_POWER_PILL;
	        }
	        int livesAfter = game.getPacmanNumberOfLivesRemaining();
			if (livesAfter < livesBefore){
				break;
			}
		}
		
		int score = game.getScore();
		
		int livesAfter = game.getPacmanNumberOfLivesRemaining();
		if (livesAfter > livesBefore){
			score += NEW_LIFE_VALUE;
		} else if (livesAfter < livesBefore){
			score += LOST_LIFE_VALUE;
		}
		
		return score + bonus;
	}

	private int avgDistanceToGhosts(Game game) {
		int sum = 0;
		for(GHOST ghost : GHOST.values())
			sum += game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
		return sum/4;
	}
	
}