/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twilight
 */
public class NetworkWithNeighboringStateCount<STATE extends Enum<STATE>> {

    /**
     * This needs to be called if the network is not bidirectional and if node states have been changed.
     */
    public void updateAllCounts() {
        for (Node node : nodes) {
            ((NodeImpl)node).updateCounts();
        }
    }

    private class NodeImpl implements Node {
        STATE currentState;
        int[/*STATE*/] neighborCounts = null;

        public void setCurrentState(STATE currentState) {
            this.currentState = currentState;
            if (neighborCounts == null) {
                neighborCounts = new int[currentState.getClass().getEnumConstants().length];
            }
        }

        void updateCounts() {
            Arrays.fill(neighborCounts, 0);
            for (NodeImpl n : getNeighbors(this)) {
                neighborCounts[n.currentState.ordinal()]++;
            }
        }

    }
    public double countNeighbors(Node n, STATE state) {
        return ((NodeImpl) n).neighborCounts[state.ordinal()];
    }
    public STATE currentState(Node n) {
        return ((NodeImpl) n).currentState;
    }
    public void changeNodeState(Node n, STATE state) {
        NodeImpl nn = (NodeImpl) n;
        nn.setCurrentState(state);
        for (NodeImpl neighbor : getNeighbors(nn)) {
            neighbor.updateCounts();
        }
    }
    public void initAllNodes(STATE state) {
        for (Node node : nodes) {
            ((NodeImpl) node).setCurrentState(state);
        }
        updateAllCounts();
    }

    ArrayList<Node> nodes = new ArrayList<Node>();
    public Node addNode() {
        Node res = new NodeImpl();
        nodes.add(res);
        return res;
    }
    public Iterable<Node> nodes() {
        return nodes;
    }

    public int addBidirectionalLink(Node n1, Node n2) {
        if (n1 == n2) {
            //System.err.println("WARNING: self-link not added");
            return 0;
        }
        return registerLink((NodeImpl) n1, (NodeImpl) n2) + registerLink((NodeImpl) n2, (NodeImpl) n1);
    }
    public void removeBidirectionalLink(Node n1, Node n2) {
        unregisterLink((NodeImpl) n1, (NodeImpl) n2);
        unregisterLink((NodeImpl) n2, (NodeImpl) n1);
    }

    private int registerLink(NodeImpl n1, NodeImpl n2) {
        List<NodeImpl> l = links.get(n1);
        if (l == null) {
            l = new ArrayList<NodeImpl>(5);
            links.put(n1, l);
        } else if (l.contains(n2)) {
            //System.err.println("WARNING: link not added as already present");
            return 0;
        }
        l.add(n2);
        return 1;
    }

    private Iterable<NodeImpl> getNeighbors(NodeImpl n) {
        List<NodeImpl> l = links.get(n);
        if (l == null) {
            l = Collections.emptyList();
        }
        return l;
    }

    private void unregisterLink(NodeImpl n1, NodeImpl n2) {
        List<NodeImpl> l = links.get(n1);
        if (l == null || !l.remove(n2)) {
            System.err.println("WARNING: removing an unexisting link");
        }
        n1.updateCounts();
    }

    Map<NodeImpl, List<NodeImpl>> links = new HashMap<NodeImpl, List<NodeImpl>>();
}
