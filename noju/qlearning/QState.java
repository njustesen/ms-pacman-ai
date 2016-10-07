package pacman.noju.qlearning;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class QState {

	int level; 			// 1
	int junction; 		// 64
	int distanceUp;		// 4
	boolean edibleUp;	// 2
	int distanceDown;	// 4
	boolean edibleDown;	// 2
	int distanceRight;	// 4
	boolean edibleRight;// 2
	int distanceLeft;	// 4
	boolean edibleLeft;	// 2
	
	boolean firstPP;	// 2
	boolean secondPP;	// 2
	boolean thirdPP;	// 2
	boolean fourthPP;	// 2
	
	public QState(int level, int junction, int distanceUp, boolean edibleUp,
			int distanceDown, boolean edibleDown, int distanceRight,
			boolean edibleRight, int distanceLeft, boolean edibleLeft, 
			boolean firstPP, boolean secondPP, boolean thirdPP, boolean fourthPP) {
		super();
		this.level = level;
		this.junction = junction;
		this.distanceUp = distanceUp;
		this.edibleUp = edibleUp;
		this.distanceDown = distanceDown;
		this.edibleDown = edibleDown;
		this.distanceRight = distanceRight;
		this.edibleRight = edibleRight;
		this.distanceLeft = distanceLeft;
		this.edibleLeft = edibleLeft;
		this.firstPP = firstPP;
		this.secondPP = secondPP;
		this.thirdPP = thirdPP;
		this.fourthPP = fourthPP;
	}
	
	public QState(Game game) {
		
		this.level = game.getCurrentLevel();
		this.junction = game.getPacmanCurrentNodeIndex();
		setDistanceAndEdible(game, MOVE.UP);
		setDistanceAndEdible(game, MOVE.RIGHT);
		setDistanceAndEdible(game, MOVE.LEFT);
		setDistanceAndEdible(game, MOVE.DOWN);
		
		this.secondPP = false;
		this.firstPP = false;
		this.thirdPP = false;
		this.fourthPP = false;
		
		int [] pps = game.getCurrentMaze().powerPillIndices;
		for (int i = 0; i < pps.length; i++){
			for(Integer a : game.getActivePowerPillsIndices()){
				if (pps[i] == a){
					switch(i){
					case 0 : this.secondPP = true; break;
					case 1 : this.firstPP = true; break;
					case 2 : this.thirdPP = true; break;
					case 3 : this.fourthPP = true; break;
					}
				}
			}
		}
			
	}
	
	private void setDistanceAndEdible(Game game, MOVE move) {
		
		int shortest = 10000;
		boolean edible = false;
		for(GHOST ghost : GHOST.values()){
			
			int distance = 64;
			if (game.getGhostLairTime(ghost) == 0){
				int ghostPos = game.getGhostCurrentNodeIndex(ghost);
				int[] path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), ghostPos, move);
				distance = path.length;
			}
			
			if (distance < shortest){
				shortest = distance;
				if (game.isGhostEdible(ghost))
					edible = true;
				else
					edible = false;
			}
			
		}
		
		shortest = digitalize(shortest);
		
		switch(move){
			case UP : this.distanceUp = shortest; this.edibleUp = edible; break;
			case RIGHT : this.distanceRight = shortest; this.edibleRight = edible; break;
			case LEFT : this.distanceLeft = shortest; this.edibleLeft = edible; break;
			case DOWN : this.distanceDown = shortest; this.edibleDown = edible; break;
		}
		
	}
	private int digitalize(int i) {

		int n = 1;
		while(i > n && n < 64){
			n = n * 4;
		}
		return n;
		
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getJunction() {
		return junction;
	}
	public void setJunction(int junction) {
		this.junction = junction;
	}
	public int getDistanceUp() {
		return distanceUp;
	}
	public void setDistanceUp(int distanceUp) {
		this.distanceUp = distanceUp;
	}
	public boolean isEdibleUp() {
		return edibleUp;
	}
	public void setEdibleUp(boolean edibleUp) {
		this.edibleUp = edibleUp;
	}
	public int getDistanceDown() {
		return distanceDown;
	}
	public void setDistanceDown(int distanceDown) {
		this.distanceDown = distanceDown;
	}
	public boolean isEdibleDown() {
		return edibleDown;
	}
	public void setEdibleDown(boolean edibleDown) {
		this.edibleDown = edibleDown;
	}
	public int getDistanceRight() {
		return distanceRight;
	}
	public void setDistanceRight(int distanceRight) {
		this.distanceRight = distanceRight;
	}
	public boolean isEdibleRight() {
		return edibleRight;
	}
	public void setEdibleRight(boolean edibleRight) {
		this.edibleRight = edibleRight;
	}
	public int getDistanceLeft() {
		return distanceLeft;
	}
	public void setDistanceLeft(int distanceLeft) {
		this.distanceLeft = distanceLeft;
	}
	public boolean isEdibleLeft() {
		return edibleLeft;
	}
	public void setEdibleLeft(boolean edibleLeft) {
		this.edibleLeft = edibleLeft;
	}

	
	
	public boolean isFirstPP() {
		return firstPP;
	}

	public void setFirstPP(boolean firstPP) {
		this.firstPP = firstPP;
	}

	public boolean isSecondPP() {
		return secondPP;
	}

	public void setSecondPP(boolean secondPP) {
		this.secondPP = secondPP;
	}

	public boolean isThirdPP() {
		return thirdPP;
	}

	public void setThirdPP(boolean thirdPP) {
		this.thirdPP = thirdPP;
	}

	public boolean isFourthPP() {
		return fourthPP;
	}

	public void setFourthPP(boolean fourthPP) {
		this.fourthPP = fourthPP;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + distanceDown;
		result = prime * result + distanceLeft;
		result = prime * result + distanceRight;
		result = prime * result + distanceUp;
		result = prime * result + (edibleDown ? 1231 : 1237);
		result = prime * result + (edibleLeft ? 1231 : 1237);
		result = prime * result + (edibleRight ? 1231 : 1237);
		result = prime * result + (edibleUp ? 1231 : 1237);
		result = prime * result + (firstPP ? 1231 : 1237);
		result = prime * result + (fourthPP ? 1231 : 1237);
		result = prime * result + junction;
		result = prime * result + level;
		result = prime * result + (secondPP ? 1231 : 1237);
		result = prime * result + (thirdPP ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QState other = (QState) obj;
		if (distanceDown != other.distanceDown)
			return false;
		if (distanceLeft != other.distanceLeft)
			return false;
		if (distanceRight != other.distanceRight)
			return false;
		if (distanceUp != other.distanceUp)
			return false;
		if (edibleDown != other.edibleDown)
			return false;
		if (edibleLeft != other.edibleLeft)
			return false;
		if (edibleRight != other.edibleRight)
			return false;
		if (edibleUp != other.edibleUp)
			return false;
		if (firstPP != other.firstPP)
			return false;
		if (fourthPP != other.fourthPP)
			return false;
		if (junction != other.junction)
			return false;
		if (level != other.level)
			return false;
		if (secondPP != other.secondPP)
			return false;
		if (thirdPP != other.thirdPP)
			return false;
		return true;
	}

	
}
