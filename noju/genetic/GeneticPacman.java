package pacman.noju.genetic;

import java.util.ArrayList;
import java.util.List;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class GeneticPacman extends Controller<MOVE>
{
	
	private Genome genome;
	private static int searchLimit = 8;
	
	public GeneticPacman(Genome genome) {
		super();
		this.genome = genome;
	}
	
	public GeneticPacman() {
		super();
		this.genome = null;
	}

	public MOVE getMove(Game game, long timeDue) {
				
		int position = game.getPacmanCurrentNodeIndex();
		List<List<Integer>> paths = findPaths(game, position, null, searchLimit);
		System.out.println("Paths: " + paths.size());
		List<Integer> best = bestPath(game, paths);
		System.out.println("Length: " + best.size());
		
		return game.getNextMoveTowardsTarget(position, best.get(0), DM.MANHATTAN);
		
	}

	private List<List<Integer>> findPaths(Game game, int position, MOVE move, int acc) {
		
		
		List<List<Integer>> paths = new ArrayList<List<Integer>>();
		List<Integer> localPath = new ArrayList<Integer>();
		
		// Find local path
		while(true){
			int[] neighbors = findNeighbors(game, position, move);
			
			if (neighbors.length == 1){
				localPath.add(neighbors[0]);
				position = neighbors[0];
				move = game.getMoveToMakeToReachDirectNeighbour(position, neighbors[0]); 
				continue;
			}
			
			break;
		}
		
		if (acc == 0){
			paths.add(localPath);
			return paths;
		}
			
		// Find deeper paths
		int[] neighbors = findNeighbors(game, position, move);
		acc--;
		for(int neighbor : neighbors){
			
			MOVE direction = game.getMoveToMakeToReachDirectNeighbour(position, neighbor);
			List<List<Integer>> deepPaths = findPaths(game, neighbor, direction, acc);
			for(List<Integer> deepPath : deepPaths){
				List<Integer> completePath = clonePath(localPath);
				for(int i : deepPath){
					completePath.add(i);
				}
				paths.add(completePath);
			}
			
		}
		
		return paths;
		
	}

	private List<Integer> clonePath(List<Integer> path) {
		
		List<Integer> newPath = new ArrayList<Integer>();
		
		for(int i : path){
			newPath.add(i);
		}
		
		return newPath;
		
	}

	private int[] findNeighbors(Game game, int position, MOVE move) {
		
		int[] neighbors = game.getNeighbouringNodes(position);
		List<Integer> nonOppositeNeighbors = new ArrayList<Integer>();
		
		for(int neighbor : neighbors){
			MOVE direction = game.getMoveToMakeToReachDirectNeighbour(position, neighbor);
			if (!isOpposite(direction, move))
				nonOppositeNeighbors.add(neighbor);
		}
		
		if (nonOppositeNeighbors.isEmpty())
			return neighbors;
		
		neighbors = new int[nonOppositeNeighbors.size()];
		int x = 0;
		for(Integer i : nonOppositeNeighbors){
			neighbors[x] = i;
			x++;
		}
		
		return neighbors;
	}

	private boolean isOpposite(MOVE direction, MOVE move) {
		
		if (direction == MOVE.DOWN && move == MOVE.UP)
			return true;
		
		if (direction == MOVE.LEFT && move == MOVE.RIGHT)
			return true;
		
		if (direction == MOVE.RIGHT && move == MOVE.LEFT)
			return true;
		
		if (direction == MOVE.UP && move == MOVE.DOWN)
			return true;
		
		return false;
	}
	
	private List<Integer> bestPath(Game game, List<List<Integer>> paths) {
		
		int bestValue = -999999;
		List<Integer> bestPath = null;
		
		for(List<Integer> path : paths){
			
			int value = valueOfPath(game, path);
			if (value > bestValue){
				bestValue = value;
				bestPath = path;
			}
			
		}
		
		System.out.println("Value: " + bestValue);
		
		return bestPath;
	}

	private int valueOfPath(Game game, List<Integer> path) {
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
		boolean power = false;
		int pillValue = 10;
		if (game.getActivePillsIndices().length < 40)
			pillValue = 20;
		else if (game.getActivePillsIndices().length < 20)
			pillValue = 30;
		else if (game.getActivePillsIndices().length < 10)
			pillValue = 40;
		else if (game.getActivePillsIndices().length < 5)
			pillValue = 50;
		
		int pillsPicked = 0;
		for(int i : path){
			
			if (pillList.contains(i)){
				value+=pillValue;
				pillsPicked++;
			}
			
			if (powerPillList.contains(i)){
				value += 100 - distanceToGhost(game, i);
				power = true;
			}
			
			if (distanceToGhost(game, i) <= step && !power){
				value-=1000;
			} else if (distanceToGhost(game, i) <= 10 && !power){
				value -= Math.max(0, 10-distanceToGhost(game, i));
			}
			
			if (distanceToGhost(game, i) == 0 && power){
				value+=1000;
			}
			
			step++;
		}
		if (pillsPicked == game.getActivePillsIndices().length){
			value+=100;
		}
		value+=step*1.5;
		return value;
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
		boolean power = false;
		int pillValue = 10;
		if (game.getActivePillsIndices().length < 40)
			pillValue = 20;
		else if (game.getActivePillsIndices().length < 20)
			pillValue = 30;
		else if (game.getActivePillsIndices().length < 10)
			pillValue = 40;
		else if (game.getActivePillsIndices().length < 5)
			pillValue = 50;
		
		int pillsPicked = 0;
		for(int i : path){
			
			if (pillList.contains(i)){
				value+=pillValue;
				pillsPicked++;
			}
			
			if (powerPillList.contains(i)){
				value += 100 - distanceToGhost(game, i);
				power = true;
			}
			
			if (distanceToGhost(game, i) <= step && !power){
				value-=1000;
			} else if (distanceToGhost(game, i) <= 10 && !power){
				value -= Math.max(0, 10-distanceToGhost(game, i));
			}
			
			if (distanceToGhost(game, i) == 0 && power){
				value+=1000;
			}
			
			step++;
		}
		if (pillsPicked == game.getActivePillsIndices().length){
			value+=100;
		}
		value+=step*1.5;
		return value;
	}

	private int distanceToGhost(Game game, int node) {
		int closestGhost = 999;
		for(GHOST ghost : GHOST.values()){
			int ghostNode = game.getGhostCurrentNodeIndex(ghost);
			int distance = game.getShortestPathDistance(node,ghostNode);
			if (distance < closestGhost && distance != -1){
				closestGhost = distance;
			}
		}
		return closestGhost;
	}

	public Genome getGenome() {
		return genome;
	}

	public void setGenome(Genome genome) {
		this.genome = genome;
	}

}


