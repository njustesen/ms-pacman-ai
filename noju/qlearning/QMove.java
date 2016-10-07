package pacman.noju.qlearning;

import pacman.game.Constants.MOVE;

public class QMove{
	
	MOVE move;
	QState state;
	public QMove(MOVE move, QState state) {
		super();
		this.move = move;
		this.state = state;
	}
	public MOVE getMove() {
		return move;
	}
	public void setMove(MOVE move) {
		this.move = move;
	}
	public QState getState() {
		return state;
	}
	public void setState(QState state) {
		this.state = state;
	}
	
}