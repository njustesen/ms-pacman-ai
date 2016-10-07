To run the solution found by EA:
new GeneticPacman2(new Genome(473, 3.1854737562458, 688, 9768, -8513, 3055, 521, 79, 4))

To increase/decreas the calculation time of MCTS - change the integer X in MCTS.getMove():
return MctsSearch(game, X);

To run with Qlearner:
- Insert states.dat into project root.
- Run main method of QTrainer.