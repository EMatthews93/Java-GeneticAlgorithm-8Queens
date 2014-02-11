package org.eric.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Main {

	//the size that the population should be
	public static int POP_SIZE = 50;

	//sorts a hash map of entities and fitness values using selection sort
	public static ArrayList<Entry<int[], Double>> sortMap(HashMap<int[], Double> hashMap)
	{
		ArrayList<Entry<int[], Double>> sortedMapList = new ArrayList<Entry<int[], Double>>();
		
		for (int i = 0; i < POP_SIZE; i++)
		{
			Entry<int[], Double> smallest = null;
			
			for (Entry<int[], Double> e : hashMap.entrySet())
			{
				if (smallest == null || smallest.getValue() > e.getValue())
					smallest = e;
			}
			
			sortedMapList.add(smallest);
			hashMap.remove(smallest.getKey());
		}
		
		return sortedMapList;
	}
	
	//generates the fitness of a single entity
	static int fitness(int[] x)
	{
		int count = 0;
		
		//horizontals
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (i != j)
				{
					if (x[i] == x[j])
						count++;
				}
			}
		}
		
		//diagonals
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (i != j)
				{
					if (Math.abs(x[i]-x[j]) == Math.abs(i-j))
						count++;
				}
			}
		}
		
		//56 is the max amount of queens that can be attacking each other
		return (56 - count);
	}
	
	//calculates a map of the fitness of each entity in the population
	static HashMap<int[], Double> populationFitness(ArrayList<int[]> pop)
	{
		HashMap<int[], Double> returnMap = new HashMap<int[], Double>();
		
		double total = 0;
		for (int i = 0; i < pop.size(); i++)
		{
			total += fitness(pop.get(i));
		}
		
		if (total == 0)
		 return null;
		
		for (int i = 0; i < pop.size(); i++)
		{
			returnMap.put(pop.get(i),
							(double)fitness(pop.get(i))/56.0 * 100.0);
		}
		
		return returnMap;
	}

	//generates a random board of queen positions
	static int[] genRandBoard()
	{
	    int[] b = new int[8];
	    int i;
	    for (i = 0; i < 8; i++)
	    {
	        int r = (int)(Math.random() * 8);
	        b[i] = r;
	    }
	    
	    return b;
	}

	//swaps genes between entities in the population
	static void swapGenes(int[] a, int[] b)
	{
	    int r = (int)(Math.random() * 8);
	    int i;
	    for (i = 0; i < r; ++i)
	    {
	        int temp = b[i];
	        b[i] = a[i];
	        a[i] = temp;
	    }
	}

	//mutates genes randomly (usually doesn't do anything)
	static void mutate(int[] a)
	{
	    int i;
	    for (i = 0; i < 8; i++)
	    {
	        int r = (int)(Math.random() * 1000);
	        if (r == 0)
	        {
	            a[i] = (int)(Math.random() * 8);
	        }
	    }
	}

	//takes in a sortedPopulation, sorted by fitness percentage
	//outputs a weighted selection of the best of the population
	static ArrayList<int[]> roulette(ArrayList<Entry<int[], Double>> sortedPop)
	{
	    ArrayList<int[]> result = new ArrayList<int[]>();

	    double r = Math.random() * 100.0;
	    
	    while (result.size() < POP_SIZE)
	    {
		    for (Entry<int[], Double> e : sortedPop)
		    {
		    	if (r < e.getValue().doubleValue())
		    	{
		    		System.out.println(e.getValue().doubleValue());
		    		int[] newArray = new int[8];
					System.arraycopy(e.getKey(), 0, newArray, 0, e.getKey().length);
					result.add(newArray);
		    		break;
		    	}
		    }
	    }
	    System.out.println("--------------");
	    System.out.println(result.size());
	    
	    return result;
	}

	//generates the next generation of the population through swapping and mutation
	static ArrayList<int[]> nextGeneration(ArrayList<int[]> pop)
	{
	    for(int i = 0; i < pop.size()-1; i++)
	    {
	        swapGenes(pop.get(i), pop.get(i+1));
	        mutate(pop.get(i));
	        mutate(pop.get(i+1));
	        //pop = roulette(sortMap(populationFitness(pop)));
	    }
	
	    return pop;
	}

	//finds if there is a solution in the population
	static ArrayList<int[]> findSolution(ArrayList<int[]> pop)
	{
		ArrayList<int[]> result = new ArrayList<int[]>();
		
		for (int i = 0; i < pop.size(); i++)
		{
			int f = fitness(pop.get(i));
			if (f == 56)
			{
				result.add(pop.get(i));
				System.out.println("Solution found");
			}
		}
		
		return result;
	}

	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		 ArrayList<int[]> population = new ArrayList<int[]>();
		 
	    //generate POP_SIZE amount of boards
	    for (int i = 0; i < POP_SIZE; i++)
	    {
	        population.add(genRandBoard());
	    }
	    
	    int solCount = POP_SIZE;

	    //finds the next generation of entities until there is a sutable result that meets
	    //the "findSolution" criteria
	    ArrayList<int[]> result = findSolution(population);
	    while (result.size() == 0)
	    {
	    	ArrayList<int[]> nextGen = nextGeneration(population);
	    	result = findSolution(nextGen);
	    	population = nextGen;
	    	solCount += POP_SIZE;
	    }
	    
	    long endTime = System.currentTimeMillis();
	    
	    //assignment calculations
	    
	    double stateCount = Math.pow(8.0, 8.0); //16777216 states
	    double workPerState = 8*8 + 8*8 + 4; //counted from amount of work done in fitness function
	    double cpuCyclesPerSecond = 1000000; //1Ghz CPU
	    
	    double entitiesPerSecond = cpuCyclesPerSecond / workPerState;
	    double timeToSearchTree = stateCount / entitiesPerSecond;
	    
	    //output each resulting solution found
	    for (int[] e : result)
	    {
	    	for (int j = 0; j < 8; j++)
	    	{
	    		System.out.print(e[j] + " ");
	    	}
	    	System.out.println();
	    	System.out.println("fitness: " + (double)fitness(e)/56.0*100.0 + "%");
	    	System.out.println();
	    }
	    
	    //additional information
	    System.out.println("# of entities generated: " + solCount);
    	System.out.println("Time to find solution: " + (endTime - startTime) + "ms");
    	System.out.println();
    	System.out.println("With a 1Ghz CPU, the entire search space (8^8) could be searched in: " + (int)timeToSearchTree + " seconds");
	}

}
