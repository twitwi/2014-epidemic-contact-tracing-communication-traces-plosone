/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twilight
 */
public class SimulationTest {

    public static enum State {S, I}

    @Test
    public void testSimpleInfectionChain() {

        NetworkWithNeighboringStateCount<State> net = new NetworkWithNeighboringStateCount<State>();
        int netSize = 1000;
        Node[] chain = new Node[netSize];
        for (int i = 0; i < chain.length; i++) {
            chain[i] = net.addNode();
            if (i != 0) {
                net.addBidirectionalLink(chain[i], chain[i - 1]);
            }
        }

        double lambda = .1; // average time to transition with one neighbor: 10 time units
        TransitionParameters<State> transitions = new TransitionParameters(State.class);
        transitions.addTransition(State.S, State.I, Distributions.expFactorTimesCount(lambda, State.I));

        double sum = 0;
        int iter = 20;
        for (int i = 0; i < iter; i++) {
            net.initAllNodes(State.S);
            net.changeNodeState(chain[0], SimulationTest.State.I);
            Simulation<State> simu = new Simulation<State>(net, transitions);
            // we expect the whole network to be infected after around 10*(netSize-1) time units
            while (net.currentState(chain[chain.length - 1]) != State.I) {
                simu.runUntilAtMost(10000000);
            }
            sum += simu.getTime();
            System.out.println(simu.getTime());
        }
        double averageTimeToContamination = sum / iter;
        double expectedTimeToContamination = 1 / lambda * (netSize - 1);
        assertEquals(expectedTimeToContamination, averageTimeToContamination, expectedTimeToContamination * .05);
    }

    @Test
    public void testSimpleInfectionChainTwoEnds() {

        NetworkWithNeighboringStateCount<State> net = new NetworkWithNeighboringStateCount<State>();
        int netSize = 1000;
        Node[] chain = new Node[netSize];
        for (int i = 0; i < chain.length; i++) {
            chain[i] = net.addNode();
            if (i != 0) {
                net.addBidirectionalLink(chain[i], chain[i - 1]);
            }
        }

        double lambda = .1; // average time to transition with one neighbor: 10 time units
        TransitionParameters<State> transitions = new TransitionParameters(State.class);
        transitions.addTransition(State.S, State.I, Distributions.expFactorTimesCount(lambda, State.I));

        double sum = 0;
        int iter = 20;
        for (int i = 0; i < iter; i++) {
            net.initAllNodes(State.S);
            net.changeNodeState(chain[0], SimulationTest.State.I);
            net.changeNodeState(chain[chain.length-1], SimulationTest.State.I);
            Simulation<State> simu = new Simulation<State>(net, transitions);
            // we expect the whole network to be infected after around less than .5 * 10*(netSize-1) time units
            while (net.currentState(chain[chain.length / 2]) != State.I) {
                simu.runUntilAtMost(10000000);
            }
            sum += simu.getTime();
            System.out.println(simu.getTime());
        }
        double averageTimeToContamination = sum / iter;
        double expectedTimeToContamination = 1 / lambda * (netSize - 1) / 2;
        assertEquals(expectedTimeToContamination, averageTimeToContamination, expectedTimeToContamination * .05);
    }
}
