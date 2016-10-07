package pacman.noju.qlearning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.internal.Node;
import pacman.game.Game;

public class QLearner extends Controller<MOVE>{

	private QTable table;
	private MOVE lastMove;
	private List<Integer> junctions;
	private int lastLevel = -1;
	
	public QLearner(QTable table){
		super();
		this.table = table;
		this.lastMove = MOVE.LEFT;
	}
	
	public MOVE bestMove(Game game, float exploit, boolean inJunction) {
		
		// In junction
		if (!inJunction)
			return lastMove;
		
		// Strategy
		if (Math.random() > exploit)
			return explore(game);
		
		return exploit(game);
		
	}

	private MOVE explore(Game game) {

		List<MOVE> moves = getPossblesMoves(game);
		int idx = (int) (Math.random() * moves.size());
		lastMove = moves.get(idx);
		return lastMove;
		
	}

	private MOVE exploit(Game game) {
		
		MOVE bestMove = null;
		float bestValue = -9999f;
		QState state = new QState(game.copy());
		Map<MOVE, Float> map = table.get(state);
		List<MOVE> newMoves = new ArrayList<MOVE>();
		
		for(MOVE move : getPossblesMoves(game)){
			
			float value = -10000f;
			if (map.containsKey(move)){
				value = map.get(move);
			} else {
				newMoves.add(move);
			}
			
			if (value > bestValue){
				bestMove = move;
				bestValue = value;
			}
			
		}
		
		if (bestMove == null)
			bestMove = explore(game);
		
		lastMove = bestMove;
		
		return bestMove;
	}

	private List<MOVE> getPossblesMoves(Game game) {
		
		int pacman = game.getPacmanCurrentNodeIndex();
		List<MOVE> moves = new ArrayList<MOVE>();
		
		if(game.getNeighbour(pacman, MOVE.UP) != -1)
			moves.add(MOVE.UP);
		if(game.getNeighbour(pacman, MOVE.DOWN) != -1)
			moves.add(MOVE.DOWN);
		if(game.getNeighbour(pacman, MOVE.RIGHT) != -1)
			moves.add(MOVE.RIGHT);
		if(game.getNeighbour(pacman, MOVE.LEFT) != -1)
			moves.add(MOVE.LEFT);
		
		return moves;
		
	}

	@Override
	public MOVE getMove(Game game, long timeDue) {
		
		int currentLevel = game.getCurrentLevel();
		boolean inJunction = false;
		
		if (lastLevel != currentLevel){
			junctions = getJunctions(game);
			lastLevel = currentLevel;
		}
		
		if (junctions.contains(game.getPacmanCurrentNodeIndex()))
			inJunction = true;
		
		return bestMove(game, 1.0f, inJunction);
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
}
