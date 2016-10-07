package pacman.noju.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Node;

/**
 * Run with best found values:
 * new GeneticPacman2(new Genome(473, 3.1854737562458, 688, 9768, -8513, 3055, 521, 79, 4))
 * @author Niels
 *
 */
public class GeneticPacman2 extends Controller<MOVE>
{
	
	private Genome genome;
	
	public GeneticPacman2(Genome genome) {
		super();
		this.genome = genome;
	}
	
	public GeneticPacman2() {
		super();
		this.genome = null;
	}

	public MOVE getMove(Game game, long timeDue) 
	{
				
		int position = game.getPacmanCurrentNodeIndex();
		
		int[] bestPath = {};
		int bestValue = -999999;
		int[] ppills = game.getPowerPillIndices();

		List<Integer> points = new ArrayList<Integer>();
		points.addAll(getTurns(game));

		for(int i=0; i<ppills.length;i++)
			points.add(ppills[i]);
		
		if (game.isGhostEdible(GHOST.BLINKY))
			points.add(game.getGhostCurrentNodeIndex(GHOST.BLINKY));
		if (game.isGhostEdible(GHOST.PINKY))
			points.add(game.getGhostCurrentNodeIndex(GHOST.PINKY));
		if (game.isGhostEdible(GHOST.INKY))
			points.add(game.getGhostCurrentNodeIndex(GHOST.INKY));
		if (game.isGhostEdible(GHOST.SUE))
			points.add(game.getGhostCurrentNodeIndex(GHOST.SUE));

		List<Integer> values = new ArrayList<Integer>();
		
		for(int j : points){
			
			int[] path = game.getShortestPath(position, j);
			int value = valueOfPath(game, path);
			values.add(value);
			if (value > bestValue){
				bestPath = path;
				bestValue = value;
			}
			
		}

		if (bestPath.length > 0)
			return game.getNextMoveTowardsTarget(position, bestPath[0], DM.MANHATTAN);
		
		return MOVE.UP;
		
	}
	

	private void printPath(Game game, int position, int[] path) {
		for(int i : path){
			MOVE m = game.getMoveToMakeToReachDirectNeighbour(position, i);
			System.out.print(m + ",");
			position = i;
		}
		System.out.println("");
	}

	private Collection<? extends Integer> getTurns(Game game) {
		
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

	private int valueOfPath(Game game, int[] path) {
		
		int[] pills = game.getActivePillsIndices();
		int[] powers = game.getActivePowerPillsIndices();
		ArrayList<Integer> pillList = new ArrayList<Integer>();
		ArrayList<Integer> powerPillList = new ArrayList<Integer>();
		
		for(int i = 0; i<pills.length; i++){
			pillList.add(pills[i]);
		}
		
		for(int i = 0; i<powers.length; i++){
			powerPillList.add(powers[i]);
		}
		
		int value = 0;
		int step = 0;
		boolean death = false;
		double pillValue = genome.getPillValue();
		if (game.getActivePillsIndices().length < 40)
			pillValue = pillValue * genome.getPillMultiplier();
		if (game.getActivePillsIndices().length < 20)
			pillValue = pillValue * genome.getPillMultiplier();
		if (game.getActivePillsIndices().length < 10)
			pillValue = pillValue * genome.getPillMultiplier();
		if (game.getActivePillsIndices().length < 5)
			pillValue = pillValue * genome.getPillMultiplier();
		
		int pillsPicked = 0;
		boolean power = false;
		for(int i : path){
			
			if (pillList.contains(i)){
				value+=pillValue;
				pillsPicked++;
			}
			
			if (powerPillList.contains(i)){
				value += genome.getPowerPillValue();
				power = true;
			}
			
			if (distanceToDangerGhost(game, i, power) <= step + genome.getKillDistance()){
				value += genome.getDeathValue();
				death = true;
			} else if (distanceToDangerGhost(game, i, power) <= genome.getDangerDistance()){
				value -= Math.max(0, genome.getDeathValue() / distanceToDangerGhost(game, i, power));
			}
			if (distanceToEdibleGhost(game, i, power) <= step){
				value += genome.getGhostValue();
			}
			
			step++;
		}
		if (pillsPicked == game.getActivePillsIndices().length && !death){
			value += genome.getWinValue();
		}
		
		value += step * genome.getStepValue();
		
		return value;
	}


	private int distanceToDangerGhost(Game game, int node, boolean power) {
		int closestGhost = 999;
		for(GHOST ghost : GHOST.values()){
			int ghostNode = game.getGhostCurrentNodeIndex(ghost);
			int distance = game.getShortestPathDistance(node,ghostNode);
			if ((!game.isGhostEdible(ghost) && !power) || (power || game.getGhostEdibleTime(ghost) <= distance)){
				if (distance < closestGhost && distance != -1){
					closestGhost = distance;
				}
			}
		}
		return closestGhost;
	}
	
	private int distanceToEdibleGhost(Game game, int node, boolean power) {
		int closestGhost = 999;
		for(GHOST ghost : GHOST.values()){
			int ghostNode = game.getGhostCurrentNodeIndex(ghost);
			int distance = game.getShortestPathDistance(node,ghostNode);
			if ((power || game.isGhostEdible(ghost)) && (power || game.getGhostEdibleTime(ghost) > distance)){
				if (distance < closestGhost && distance != -1){
					closestGhost = distance;
				}
			}
		}
		return closestGhost;
	}
	

	private Integer closestEdibleGhostPosition(Game game, int node, boolean power) {
		int closestGhost = 999;
		int closestGhostPosition = 0;
		for(GHOST ghost : GHOST.values()){
			int ghostNode = game.getGhostCurrentNodeIndex(ghost);
			int distance = game.getShortestPathDistance(node,ghostNode);
			if ((power || game.isGhostEdible(ghost)) && (power || game.getGhostEdibleTime(ghost) > distance)){
				if (distance < closestGhost && distance != -1){
					closestGhost = distance;
					closestGhostPosition = ghostNode;
				}
			}
		}
		return closestGhostPosition;
	}
	
}


