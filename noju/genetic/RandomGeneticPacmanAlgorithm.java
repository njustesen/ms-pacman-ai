package pacman.noju.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

public class RandomGeneticPacmanAlgorithm extends GeneticAlgorithm {

	private Legacy ghostController = new Legacy();

	public static void main(String[] args)
	{
		
		//GeneticPacmanAlgorithm alg = new GeneticPacmanAlgorithm(25, 40, 80, 3);
		//GeneticPacmanAlgorithm alg = new GeneticPacmanAlgorithm(4, 40, 80, 1);
		RandomGeneticPacmanAlgorithm alg = new RandomGeneticPacmanAlgorithm(20, 40, 80, 1);
		
		//alg.getBest(null);
		
		List<Genome> population = new ArrayList<Genome>();
		Genome a = new Genome("pillValue=-130, pillMultiplier=-1.070768116311136, powerPillValue=912, ghostValue=5081, deathValue=-3728, winValue=5268, stepValue=-868, dangerDistance=39, killDistance=30, mutated=0");
		population.add(a);
		alg.getBest(population);
		
	}

	public RandomGeneticPacmanAlgorithm(int size, int generations, int mutationRate, int trials) {
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
			
			// Stop if last generation
			if (g == generations-1)
				return bestGenome;
			
			population = newPopulation();
			
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
