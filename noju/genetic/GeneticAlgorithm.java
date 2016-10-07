package pacman.noju.genetic;

import java.util.List;

public abstract class GeneticAlgorithm {
	
	protected int size;
	protected int generations;
	protected int mutationRate;
	protected int trials;
	
	public GeneticAlgorithm(int size, int generations, int mutationRate, int trials){
		this.size = size;
		this.generations = generations;
		this.mutationRate = mutationRate;
		this.trials = trials;
	}
	
	public abstract Genome getBest(List<Genome> population);

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getGenerations() {
		return generations;
	}

	public void setGenerations(int generations) {
		this.generations = generations;
	}

	public int getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(int mutationRate) {
		this.mutationRate = mutationRate;
	}

	public int getTrials() {
		return trials;
	}

	public void setTrials(int trials) {
		this.trials = trials;
	}
	
}
