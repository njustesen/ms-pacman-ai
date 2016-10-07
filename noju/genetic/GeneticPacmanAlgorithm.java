package pacman.noju.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

public class GeneticPacmanAlgorithm extends GeneticAlgorithm {

	private Legacy ghostController = new Legacy();

	public static void main(String[] args)
	{
		
		//GeneticPacmanAlgorithm alg = new GeneticPacmanAlgorithm(25, 40, 80, 3);
		//GeneticPacmanAlgorithm alg = new GeneticPacmanAlgorithm(4, 40, 80, 1);
		GeneticPacmanAlgorithm alg = new GeneticPacmanAlgorithm(4, 40, 80, 1);
		
		//alg.getBest(null);
		
		List<Genome> population = new ArrayList<Genome>();
		/*
		Genome a = new Genome("pillValue=-130, pillMultiplier=-1.070768116311136, powerPillValue=912, ghostValue=5081, deathValue=-3728, winValue=5268, stepValue=-868, dangerDistance=39, killDistance=30, mutated=0");
		population.add(a);
		alg.getBest(population);
		*/
		
	}

	public GeneticPacmanAlgorithm(int size, int generations, int mutationRate, int trials) {
		super(size, generations, mutationRate, trials);
		
	}
	
	@Override
	public Genome getBest(List<Genome> population) {
		
		// Populate
		if (population == null)
			population = newPopulation();
		
		Genome bestGenome = null;
		for(int g = 0; g < generations; g++){
			
			// Test
			double[] scores = new double[size];
			int idx = 0;
			for(Genome genome : population){
				
				System.out.println("Testing fitness for: " + genome);
				double score = runExperimentWithAvgScore(new GeneticPacman2(genome), new Legacy(), trials); 
				scores[idx] = score;
				idx++;
				
			}
			
			// Kill
			int killings = size / 2;
			
			double bestScore = -999999;
			double sum = 0;
			for(int i=0; i<scores.length;i++){
				sum += scores[i];
				if (scores[i] > bestScore){
					bestScore = scores[i];
					bestGenome = population.get(i);
				}
			}
			System.out.println("Best of generation " + g + ": " + bestScore + " avg: " + sum/scores.length + " - " + bestGenome);
			
			// Stop if last generation
			if (g == generations-1)
				return bestGenome;
			
			List<Genome> survivors = new ArrayList<Genome>();
			List<Genome> killedGenomes = new ArrayList<Genome>();
			
			int killed = 0;
			while(killed < killings){
				
				double worstScore = 999999999;
				Genome worstGenome = null;
				for(int i = 0; i < population.size(); i++){
					Genome genome = population.get(i);
					if (scores[i] < worstScore && !killedGenomes.contains(genome)){
						worstScore = scores[i];
						worstGenome = genome;
					}
				}
				killedGenomes.add(worstGenome);
				killed++;
			}
			
			
			for(Genome genome : population){
				if (!killedGenomes.contains(genome))
					survivors.add(genome);
			}
			
			// Reproduce
			List<Genome> nextGeneration = new ArrayList<Genome>();
			nextGeneration.addAll(survivors);
			
			for(int i = 0; i<survivors.size();i++){
				
				Genome parentA = survivors.get(i);
				int other = i;
				while(other == i)
					other = (int) (Math.random() * survivors.size());
				
				Genome parentB = survivors.get(other);
				
				Genome child = Genome.breedChild(parentA, parentB);
				nextGeneration.add(child);
				
			}
			
			// Mutate
			for(Genome genome : nextGeneration){
				if (Math.random() * 100 <= this.mutationRate && genome != bestGenome){
					genome.mutate();
				}
			}
		
			population = nextGeneration;
		}
		
		return null;
	}

	private double runExperimentWithAvgScore(Controller<MOVE> pacManController,
			Legacy legacy, int trials) {
		
		double score=0;
	    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),-1),
		        		ghostController.getMove(game.copy(),-1));
			}
			
			score+=game.getScore();
			System.out.println(i+"\t"+game.getScore());
		}
		
		return score/trials;
		
	}

	private List<Genome> newPopulation() {
		
		List<Genome> population = new ArrayList<Genome>();
		
		for(int i=0; i<size;i++){
			population.add(Genome.randomGenome());
		}
		
		return population;
		
	}

}
