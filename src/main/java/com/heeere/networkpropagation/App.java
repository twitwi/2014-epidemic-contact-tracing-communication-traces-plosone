package com.heeere.networkpropagation;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Hello world!
 *
 */
public class App {
    public static enum State {
        S, I, R, T
    }
    public static void main(String[] args) throws IOException {

        final double alpha = .1;
        final double betaRandom = 0.02;
        //final double betaTraced = 1.25;
        final double gamma = .5;
        final int netSize = 1000;
        final int averageNeighborCount = 10;
        final int nNetwork = 20;
        final int nIterations = 30;

        Plotter l = new JFreechartPlotter(new File("/tmp/testtttt.plot"), null);

        for (double betaTraced = 0; betaTraced  <= 2.5; betaTraced += .1) {
            for (int iNetwork = 0; iNetwork < nNetwork; iNetwork++) {

                final Random rand = new Random();
                NetworkWithNeighboringStateCount<State> network = new NetworkWithNeighboringStateCount<State>();

                // define the network
                Node[] nodes = new Node[netSize];
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = network.addNode();
                }
                int remainingLinkToCreate = nodes.length * averageNeighborCount;
                while (remainingLinkToCreate > 0) {
                    int n1 = rand.nextInt(nodes.length);
                    int n2 = rand.nextInt(nodes.length);
                    remainingLinkToCreate -= network.addBidirectionalLink(nodes[n1], nodes[n2]);
                }

                TransitionParameters<State> p = new TransitionParameters(State.class);
                p.addTransition(State.S, State.I, Distributions.expFactorTimesCount(alpha, State.I));
                //p.addTransition(State.I, State.R, Distributions.<State>exp());
                TimeToTransitionDrawer<State> tracing = Distributions.expFactorBasePlusLambdaTimesCount(betaRandom, betaTraced, State.T);
                p.addTransition(State.I, State.T, tracing);
                TimeToTransitionDrawer<State> removing = Distributions.exp(gamma);
                p.addTransition(State.T, State.R, removing);

                for (int iter = 0; iter < nIterations; iter++) {
                    l.iter(iter);
                    System.err.println("Running iteration " + iter);
                    network.initAllNodes(State.S);
                    network.changeNodeState(nodes[0], State.I);
                    int nI = 1;
                    int totalInfected = 1;
                    Simulation<State> simu = new Simulation(network, p);
                    while (true) {
                        //System.out.println(nI + " infected persons at time " + simu.getTime());
                        Event e = simu.getNextEvent();
                        if (e == null) { // no more possible moves
                            break;
                        }
                        simu.runEvent(e);
                        if (e.from == State.I) {
                            nI--;
                        }
                        if (e.to == State.I) {
                            nI++;
                            totalInfected++;
                        }
                        l.statusAtTime(e.time, nI, totalInfected);
                    }
                    l.endIter();
                    System.err.println("   Result: " + totalInfected + " total infected persons over time.");
                }

            }
            
            l.wasParameters(alpha, betaRandom, betaTraced, gamma, netSize, averageNeighborCount);

        }

    }
}
