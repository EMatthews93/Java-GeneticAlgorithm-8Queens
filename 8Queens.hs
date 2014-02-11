module Main where

import System.Random
import qualified Data.List as L

popSize = 50

genRand :: IO [Int]
genRand = genRandHelp [1..8]
		where
			genRandHelp :: [Int] -> IO [Int]
			genRandHelp [] = do return []
			genRandHelp m =
				do
					randVal <- randomRIO(0, (length m)-1) :: IO Int
					nextVal <- genRandHelp ( (take randVal m) ++ (drop (randVal+1) m) )
					return $ (m!!randVal) : nextVal

--takes 5 of (iterate f a --> [a, f(a), f(f(a)), ...])
initialPopulation :: Int -> [IO [Int]]
initialPopulation 0 = []
initialPopulation n = genRand : (initialPopulation (n-1))

--mutates individual values with a very low probability
mutate :: [Int] -> IO [Int]
mutate [] = do return []
mutate xs =
	do
		rand <- randomRIO(1, 10000) :: IO Int
		rest <- mutate (tail xs)
		if rand == 1 then
			do
				randX <- randomRIO(1, 8) :: IO Int
				return $ randX : rest
		else
			do
				return $ (head xs) : rest
	
--chooses a random point in the middle of 2 sequences, and swaps sections between them
swapGenes :: [IO [Int]] -> IO [IO [Int]]
swapGenes []		= do return []
swapGenes (x:[])	= do return [x]
swapGenes (x:y:xs)	=
	do
		rand <- randomRIO(1, 8) :: IO Int
		x2 <- x
		y2 <- y
		nextSwap <- swapGenes xs
		return $ [return (take rand x2 ++ drop rand y2), return (take rand y2 ++ drop rand x2)] ++ nextSwap

--56 possible pairs, result is 56 - attacking pairs
--get the number of non-attacking pairs of queens
fitnessFunc :: IO [Int] -> IO Int
fitnessFunc x = do
	a <- x
	return $ 56 - (testDiagonals a + testHorizontals a)
	where
		--get the length of -> every combination of position and value, so long as difference in position = the difference in value
		testDiagonals x = length $ filter (\(rowDiff, colDiff) -> rowDiff == colDiff) [ ( abs(a-b), abs (posOf a x - posOf b x) ) | a<-x, b<-x; a /= b]
			where
				posOf k x = length $ takeWhile (/=k) x

		-- if any piece is on the same row number, count it as an attack
		testHorizontals []		= 0
		testHorizontals (x:xs)	= (length $ filter (==x) xs) + (testHorizontals xs)

totalFitness :: [IO [Int]] -> IO Int
totalFitness [] = do return 0
totalFitness (p:ps) = do
	a <- fitnessFunc p
	nextVal <- totalFitness ps
	return $ a + nextVal
		
--determines the fitness of the entire population
populationFitness :: [IO [Int]] -> IO Int -> IO [(IO [Int], Int, Int)]
populationFitness [] tot = do return []
populationFitness (p:ps) tot = do
	--created a lambda do function to take care of adding two IO Ints
	--total fitness is called in calling function for efficiency purposes
	total <- tot
	nextPop <- populationFitness ps tot
	fitnessOfP <- fitnessFunc p
	return $ (p, fitnessOfP, (fitnessOfP * 100) `div` total ) : nextPop

--roulette fst = solution, snd = fitness, thrd = fitness percentage
--sort by fitness percentage (lowest first)
roulette :: Int -> [(IO [Int], Int, Int)] -> IO [(IO [Int], Int, Int)]
roulette n x = rouletteHelper n (L.sortBy (\(a, b, c) (d, e, f) -> compare c f) x)
	where
		--choose a weight random sample from population
		rouletteHelper :: Int -> [(IO [Int], Int, Int)] -> IO [(IO [Int], Int, Int)]
		rouletteHelper 0 _ = do return []
		rouletteHelper n x = do
			rand <- getStdRandom (randomR (1,100)) :: IO Int
			elem <- return $ (dropWhile (\(a, b, c) -> c < rand) x)
			if length(elem) == 0 then
				do
					nextRoulette <- (rouletteHelper n x)
					return nextRoulette
			else
				do
					nextRoulette <- (rouletteHelper (n-1) x)
					return ( (head elem) : nextRoulette )
					
--generates the next generation of solutions
nextGeneration :: [IO [Int]] -> IO [IO [Int]]
nextGeneration x = do
	pf <- populationFitness x (totalFitness x) --x is [IO [Int]]
	r <- roulette popSize pf --pf is [(IO [Int], Int, Int)]
	s <- swapGenes $ (map (\(a,b,c) -> a) r) --r is [(IO [Int], Int, Int)]
	printStrLn $ show average s
	return $ map (\e -> do {a <- e; mutate a} ) s --s is [IO [Int]]

--determines if this is a solution
--WORKS
getSolutions :: [IO [Int]] -> IO [IO [Int]]
getSolutions [] = do return []
getSolutions (x:xs) = do
	val <- fitnessFunc x
	--putStrLn $ show(val)
	nextSolution <- getSolutions xs
	
	if (val > 54) then
		do
			return $ x : nextSolution
	else
		do
			return nextSolution
	
--find solution once fitness = 100%
findSolution :: [IO [Int]] -> IO [IO [Int]]
findSolution x = do

	solutions <- getSolutions x
	
	--no solution found, try next generation
	if (length solutions) == 0 then
		do
			nextGen <- nextGeneration x
			nextFind <- findSolution nextGen
			return nextFind
	--solution(s) found, return them
	else
		do
			return solutions
	
main = findSolution (initialPopulation popSize)

--test functions
fitnessOf0 = [1,1,1,1,1,1,1,1]
--testNextGeneration = nextGeneration testPopulation1

--showIO :: [IO [Int]]
showIO [] = do return []
showIO (x:xs) = do
	l <- x
	putStrLn $ show l
	showIO xs
	
showNextGen x = do
	nextGen <- nextGeneration x
	showIO nextGen
	return nextGen