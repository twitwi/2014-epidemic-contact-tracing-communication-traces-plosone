package com.heeere.networkpropagation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Hello world!
 *
 */
public class AppTwoNetworks {

    public static enum State {

        S, I, R, T
    }

    public static class Parameters {

        final double alpha;
        final double betaRandom;
        final double betaTraced;
        final double gamma;
        final int netSize;
        final double averageNeighborCount;
        final double averageNeighborCountToAdd; // addition
        final double averageNeighborCountToRemove; // removal
        final int nNetwork;
        final int nInitializationsPerNetwork;

        public Parameters(double alpha, double betaRandom, double betaTraced, double gamma, int netSize, double averageNeighborCount, double averageNeighborCountToAdd, double averageNeighborCountToRemove, int nNetwork, int nInitializationsPerNetwork) {
            this.alpha = alpha;
            this.betaRandom = betaRandom;
            this.betaTraced = betaTraced;
            this.gamma = gamma;
            this.netSize = netSize;
            this.averageNeighborCount = averageNeighborCount;
            this.averageNeighborCountToAdd = averageNeighborCountToAdd;
            this.averageNeighborCountToRemove = averageNeighborCountToRemove;
            this.nNetwork = nNetwork;
            this.nInitializationsPerNetwork = nInitializationsPerNetwork;
        }
    }

    public static class Results {
    }

    public static void main1(String[] args) throws IOException {
        ParameterReader r = new ParameterReader(args);
        Parameters p = new Parameters(
                r.d(), r.d(), r.d(), r.d(), r.i(), r.d(), r.d(), r.d(), r.i(), r.i()
                );
        doIt(p, true);
    }

    public static void main(String[] args) throws IOException {

        final double alpha = .1;
        final double betaRandom = 0.02;
        //final double betaTraced = 1.25;
        final double gamma = .5;
        final int netSize = 200;
        final int averageNeighborCount = 10;
        final int averageNeighborCountToAdd = 2; // addition
        final int averageNeighborCountToRemove = 4; // removal
        final int nNetwork = 10;
        final int nIterations = 20;

        Parameters p = new Parameters(.1, .02, 1.25, .5, 200, 10, 2, 4, 10, 20);
        doIt(p, true);

    }

    public static void doIt(Parameters p, boolean useDisplay) throws IOException {

        StatisticsProcessor l = new StatisticsProcessor();
//        if (useDisplay) {
//            l = new JFreechartPlotter(new File("/tmp/testtttt-2nets.plot"), null);
//        }


        for (double betaTraced = 0; betaTraced <= 2.5; betaTraced += .1) {
            for (int iNetwork = 0; iNetwork < p.nNetwork; iNetwork++) {

                final Random rand = new Random();
                NetworkWithNeighboringStateCount<State> network = new NetworkWithNeighboringStateCount<State>();
                NetworkWithNeighboringStateCount<State> knownNetwork = new NetworkWithNeighboringStateCount<State>();
                Map<Node, Node> networkToKnownNetwork = new IdentityHashMap<Node, Node>();
                Map<Node, Node> knownNetworkToNetwork = new IdentityHashMap<Node, Node>();

                // define the network
                Node[] nodes = new Node[p.netSize];
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = network.addNode();
                }
                {
                    int remainingLinkToCreate = (int) (nodes.length * p.averageNeighborCount);
                    while (remainingLinkToCreate > 0) {
                        int n1 = rand.nextInt(nodes.length);
                        int n2 = rand.nextInt(nodes.length);
                        remainingLinkToCreate -= network.addBidirectionalLink(nodes[n1], nodes[n2]);
                    }
                }
                // define the known network (with the same nodes)
                Node[] knownNodes = new Node[p.netSize];
                List<Pair<Node, Node>> knownNetworkSharedEdges = new ArrayList<Pair<Node, Node>>();
                for (int i = 0; i < nodes.length; i++) {
                    knownNodes[i] = knownNetwork.addNode();
                    networkToKnownNetwork.put(nodes[i], knownNodes[i]);
                    knownNetworkToNetwork.put(knownNodes[i], nodes[i]);
                }
                for (int i = 0; i < nodes.length; i++) {
                    Node kn1 = knownNodes[i];
                    for (Node n2 : network.getNeighbors(nodes[i])) {
                        Node kn2 = networkToKnownNetwork.get(n2);
                        if (0 < knownNetwork.addBidirectionalLink(kn1, kn2)) {
                            knownNetworkSharedEdges.add(new Pair<Node, Node>(kn1, kn2));
                        }
                    }
                }
                {
                    int remainingLinkToCreate = (int) (nodes.length * p.averageNeighborCountToAdd);
                    while (remainingLinkToCreate > 0) {
                        int kn1 = rand.nextInt(knownNodes.length);
                        int kn2 = rand.nextInt(knownNodes.length);
                        remainingLinkToCreate -= knownNetwork.addBidirectionalLink(knownNodes[kn1], knownNodes[kn2]);
                    }
                }
                Collections.shuffle(knownNetworkSharedEdges);
                {
                    int linksToRemove = (int) (nodes.length * p.averageNeighborCountToRemove / 2);
                    for (int i = 0; i < linksToRemove; i++) {
                        knownNetwork.removeBidirectionalLinkSilently(knownNetworkSharedEdges.get(i).getFirst(), knownNetworkSharedEdges.get(i).getSecond());
                    }
                }

                TransitionParameters<State> transitions = new TransitionParameters(State.class);
                transitions.addTransition(State.S, State.I, Distributions.expFactorTimesCount(p.alpha, State.I));
                //p.addTransition(State.I, State.R, Distributions.<State>exp());

                TransitionParameters<State> tracingTransitions = new TransitionParameters(State.class);
                TimeToTransitionDrawer<State> tracing = Distributions.expFactorBasePlusLambdaTimesCount(p.betaRandom, betaTraced, State.T);
                tracingTransitions.addTransition(State.I, State.T, tracing);
                TimeToTransitionDrawer<State> removing = Distributions.exp(p.gamma);
                tracingTransitions.addTransition(State.T, State.R, removing);

                for (int iter = 0; iter < p.nInitializationsPerNetwork; iter++) {
                    l.iter(iter);
                    System.err.println("Running iteration " + iter);
                    network.initAllNodes(State.S);
                    network.changeNodeState(nodes[0], State.I);
                    knownNetwork.initAllNodes(State.S);
                    knownNetwork.changeNodeState(knownNodes[0], State.I);
                    int nI = 1;
                    int totalInfected = 1;
                    Simulation<State> simu1 = new Simulation(network, transitions);
                    Simulation<State> simu2 = new Simulation(knownNetwork, tracingTransitions);
                    while (true) {
                        //System.out.println(nI + " infected persons at time " + simu.getTime());
                        Event e;
                        {
                            Event e1 = simu1.getNextEvent();
                            Event e2 = simu2.getNextEvent();
                            if (e1 == null && e2 == null) {
                                // no more possible moves
                                break;
                            }
                            boolean selectE1 = e2 == null || (e1 != null && e1.time < e2.time);
                            if (selectE1) {
                                e2 = new Event(networkToKnownNetwork.get(e1.node), e1.from, e1.to, e1.time);
                            } else {
                                e1 = new Event(knownNetworkToNetwork.get(e2.node), e2.from, e2.to, e2.time);
                            }
                            simu1.runEvent(e1);
                            simu2.runEvent(e2);
                            e = e1;
                        }
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


            l.wasParameters(p, System.out);

        }

    }
}
