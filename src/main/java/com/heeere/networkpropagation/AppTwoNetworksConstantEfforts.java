package com.heeere.networkpropagation;

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
public class AppTwoNetworksConstantEfforts {

    public static enum State {

        S, I, R, T
    }

    public static class Parameters extends AppTwoNetworks.Parameters {

        final double targetTracingEffortRandom;
        final double targetTracingEffortContact;
        

        public Parameters(double alpha, double betaRandom, double betaTraced, double gamma, int netSize, double averageNeighborCount, double averageNeighborCountToAdd, double averageNeighborCountToRemove, int nNetwork, int nInitializationsPerNetwork, double targetTracingEffortRandom, double targetTracingEffortContact) {
            super(alpha, betaRandom, betaTraced, gamma, netSize, averageNeighborCount, averageNeighborCountToAdd, averageNeighborCountToRemove, nNetwork, nInitializationsPerNetwork);
            this.targetTracingEffortRandom = targetTracingEffortRandom;
            this.targetTracingEffortContact = targetTracingEffortContact;
        }

        private Parameters updatingBetas(double newBetaRandom, double newBetaContact) {
            return new Parameters(alpha, newBetaRandom, newBetaContact, gamma, netSize, averageNeighborCount, averageNeighborCountToAdd, averageNeighborCountToRemove, nNetwork, nInitializationsPerNetwork, targetTracingEffortRandom, targetTracingEffortContact);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            mainHardCoded(args);
            return;
        }
        if (args.length != 12) {
            System.err.println("Expected 12 parameters exactly");
            return;
        }
        ParameterReader r = new ParameterReader(args);
        Parameters p = new Parameters(
                r.d(), r.d(), r.d(), r.d(), r.i(), r.d(), r.d(), r.d(), r.i(), r.i(), r.d(), r.d());
        doIt(p, true);
    }

    public static void mainHardCoded(String[] args) throws IOException {

        final double alpha = .1;
        final double betaRandom = 0.02;
        final double betaTraced = .1;
        final double gamma = .5;
        final int netSize = 1000;
        final double averageNeighborCount = 10;
        final double averageNeighborCountToAdd = 5; // addition
        final double averageNeighborCountToRemove = 5; // removal
        final int nNetwork = 1;
        final int nIterations = 1;
        final double constantEffortRandom = 90;
        final double constantEffortTracing = 110;

        Parameters p = new Parameters(alpha, betaRandom, betaTraced, gamma, netSize, averageNeighborCount, averageNeighborCountToAdd, averageNeighborCountToRemove, nNetwork, nIterations, constantEffortRandom, constantEffortTracing);
        doIt(p, true);

    }

    public static void doIt(Parameters p, boolean useDisplay) throws IOException {

        StatisticsProcessor l = new StatisticsProcessor();
//        if (useDisplay) {
//            l = new JFreechartPlotter(new File("/tmp/testtttt-2nets.plot"), null);
//        }


        //for (double betaTraced = 0; betaTraced <= 2.5; betaTraced += .1) {
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
            
            Distributions.ModifiableExpFactorBasePlusLambdaTimesCount<State> tracing = Distributions.modifiableExpFactorBasePlusLambdaTimesCount(p.betaRandom, p.betaTraced, State.T);
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
                int totalTraced = 0;
                int nTotalRemoved = 0;
                double tracingEffortRandom; // actual, not target ones
                double tracingEffortContact; // actual, not target ones
                Simulation<State> simu1 = new Simulation(network, transitions);
                Simulation<State> simu2 = new Simulation(knownNetwork, tracingTransitions);
                while (true) {
                    { // set the betas to get the desired tracing efforts
                        int nRandomable = p.netSize - totalTraced;
                        int nTraceable = 0;
                        NetworkWithNeighboringStateCount<State> known = simu2.network;
                        for (Node node : known.nodes) {
                            State state = known.currentState(node);
                            if (state == State.S || state == State.I) { // only S and I can be decently contact traced
                                nTraceable += known.countNeighbors(node, State.T);
                            }
                        }
                        double newBetaRandom = p.targetTracingEffortRandom / nRandomable;
                        double newBetaContact = p.targetTracingEffortContact / nTraceable;
                        if (nTraceable == 0) {
                            newBetaContact = 0;
                            newBetaRandom *= (p.targetTracingEffortRandom + p.targetTracingEffortContact) / p.targetTracingEffortRandom; // compensate to get constant total tracing
                        }
                        if (nRandomable == 0) {
                            newBetaRandom = 0;
                            newBetaContact *= (p.targetTracingEffortRandom + p.targetTracingEffortContact) / p.targetTracingEffortContact;// compensate
                        }
                        p = p.updatingBetas(newBetaRandom, newBetaContact);
                        tracing.setBase(newBetaRandom);
                        tracing.setLambda(newBetaContact);
                    }
                    //System.out.println(nI + " infected persons at time " + simu.getTime());
                    { // compute tracing efforts (kind of check as it should be almost constant (network changed a little since we set the betas))
                        tracingEffortRandom = p.betaRandom * (p.netSize - totalTraced); // netsize - totalTraced => #S + #I
                        tracingEffortContact = 0;
                        NetworkWithNeighboringStateCount<State> known = simu2.network;
                        for (Node node : known.nodes) {
                            State state = known.currentState(node);
                            if (state == State.S || state == State.I) { // only S and I can be decently contact traced
                                tracingEffortContact += known.countNeighbors(node, State.T);
                            }
                        }
                        tracingEffortContact *= p.betaTraced;
                    }
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
                    if (e.to == State.T) {
                        totalTraced++;
                    }
                    if (e.to == State.I) {
                        nI++;
                        totalInfected++;
                    }
                    if (e.to == State.R) {
                        nTotalRemoved++;
                    }
                    l.statusAtTime(e.time, nI, totalInfected, totalTraced, nTotalRemoved, tracingEffortRandom, tracingEffortContact);
                }
                l.endIter();
                System.err.println("   Result: " + totalInfected + " total infected persons over time.");
            }

        }


        l.wasParameters(p, System.out);

        //}

    }
}
