package pacman.noju.qlearning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import pacman.game.Constants.MOVE;

public class QTable {

	private HashMap<QState, HashMap<MOVE,Float>> table;

	public QTable(){
		this.table = new HashMap<QState, HashMap<MOVE,Float>>();
	}
	
	public void clear() {
		table.clear();
	}

	public boolean containsKey(QState state) {
		return table.containsKey(state);
	}

	public boolean containsValue(HashMap<MOVE,Float> values) {
		return table.containsValue(values);
	}

	public Set<Entry<QState, HashMap<MOVE, Float>>> entrySet() {
		return table.entrySet();
	}

	public HashMap<MOVE,Float> get(QState state) {
		return table.get(state);
	}

	public boolean isEmpty() {
		return table.isEmpty();
	}

	public Set<QState> keySet() {
		return table.keySet();
	}

	public HashMap<MOVE,Float> put(QState state, HashMap<MOVE,Float> map) {
		return table.put(state, map);
	}

	public HashMap<MOVE,Float> remove(QState state) {
		return table.remove(state);
	}

	public int size() {
		return table.size();
	}

	public Collection<HashMap<MOVE,Float>> values() {
		return table.values();
	}
	
}
