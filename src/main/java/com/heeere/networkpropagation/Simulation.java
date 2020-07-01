/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

import java.util.Map.Entry;

/**
 *
 * @author twilight
 */
public class Simulation<STATE extends Enum<STATE>> {

    NetworkWithNeighboringStateCount<STATE> network;
    TransitionParameters<STATE> transitions;

    public Simulation(NetworkWithNeighboringStateCount<STATE> network, TransitionParameters<STATE> transitions) {
        this.network = network;
        this.transitions = transitions;
    }

    double time = 0;

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void runUntilAtMost(final double timeLimit) {
        if (time >= timeLimit) {
            System.err.println("WARNING: runUntilAtMost called with a passed time");
            return;
        }
        Event<STATE> e = getNextEvent();
        if (e.time < timeLimit) {
            runEvent(e);
        }
    }

    public Event<STATE> getNextEvent() {
        Node consideredNode = null;
        STATE from = null;
        STATE to = null;
        double bestTime = Double.POSITIVE_INFINITY;
        // iterate over the possible transitions to get the closest in time
        for (Node n : network.nodes()) {
            STATE currentState = network.currentState(n);
            for (Entry<STATE, TimeToTransitionDrawer> e : transitions.getTransitionsFrom(currentState).entrySet()) {
                double transitionTime = time + e.getValue().drawTime(network, n);
                if (transitionTime < bestTime) {
                    bestTime = transitionTime;
                    from = currentState;
                    to = e.getKey();
                    consideredNode = n;
                }
            }
        }
        if (consideredNode != null) {
            return new Event<STATE>(consideredNode, from, to, bestTime);
        } else {
            return null;
        }
    }

    public void runEvent(Event<STATE> e) {
        network.changeNodeState(e.node, e.to);
        time = e.time;
    }

}
