package pacman.noju.mcts;

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
public class RandomPacman extends Controller<MOVE>
{
	
	public MOVE getMove(Game game, long timeDue) 
	{
		
		int random = (int) (Math.random() * 4);
		switch(random){
		case 0: return MOVE.UP;
		case 1: return MOVE.RIGHT;
		case 2: return MOVE.DOWN;
		}
		
		return MOVE.LEFT;
		
	}

}