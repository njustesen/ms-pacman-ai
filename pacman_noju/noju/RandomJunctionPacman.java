package pacman.entries.noju;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Node;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class RandomJunctionPacman extends Controller<MOVE>
{
	
	public MOVE getMove(Game game, long timeDue) {
		
		if (MCTSNIELS.junctions == null)
			MCTSNIELS.junctions = MCTSNIELS.getJunctions(game);
			
		
		MOVE lastMove = game.getPacmanLastMoveMade();
		
		if (inJunction(game))
			return randomAction(lastMove);
		else
			return lastMove;
		
	}
	
	private boolean inJunction(Game game) {
		
		if (MCTSNIELS.junctions.contains(game.getPacmanCurrentNodeIndex()))
			return true;
		
		return false;
	}

	private MOVE randomAction(MOVE except) {
		MOVE move = null;
		
		while(move == null){
			int random = (int) (Math.random() * 4);
			
			switch(random){
			case 0: move = MOVE.UP; break;
			case 1: move = MOVE.RIGHT; break;
			case 2: move = MOVE.DOWN; break;
			case 3: move = MOVE.LEFT; break;
			}
			
			if (move == except)
				move = null;
			
		}
		
		return move;
	}

	
	
	

}