package com.heeere.networkpropagation;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Random;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Here we simulate some random network with randomly added edges.
 * We then try to see if the distribution of number of edges for a node is following a Poisson distribution.
 * @author twilight
 */
public class DoesRandomEdgesLeadToPoissonDistribution {

    // we don't care about the actual state, we use a dummy type with a single possible state
    public static enum State {

        S
    }

    public static void main(String[] args) {
        // size of the network
        int netSize = 100000;
        int averageNeighborCount = 10;

        // generate the network
        Random rand = new Random();
        NetworkWithNeighboringStateCount<State> network = new NetworkWithNeighboringStateCount<State>();
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
        network.initAllNodes(State.S);

        // gather the statistics, a little verbose in java
        int[] neighborCounts = new int[netSize];
        int maxNeighbor = 0;
        for (int i = 0; i < neighborCounts.length; i++) {
            neighborCounts[i] = network.countNeighbors(nodes[i], State.S);
            maxNeighbor = Math.max(maxNeighbor, neighborCounts[i]);
        }
        // use double as it is easier for plotting
        int[] histogramOfNeighorCounts = new int[maxNeighbor + 1];
        for (int count : neighborCounts) {
            histogramOfNeighorCounts[count] += 1;
        }
        // plot it against the corresponding poisson
        // (mean of the poisson is also its lambda parameter, so we take the averageNeighborCount)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        { // fill the graph data
            System.out.println("# numberOfNeighbors probaFromStat probaFromPoisson");
            System.out.println("# to plot the two (almost identical) distributions, use:");
            System.out.println("#   gnuplot -e 'plot \"statsVsPoisson.data\" using 0:2, \"statsVsPoisson.data\" using 1:2; pause -1'");
            for (int i = 0; i < histogramOfNeighorCounts.length; i++) {
                double vStat = histogramOfNeighorCounts[i] / (double) (netSize);
                double vPoisson = Distributions.poissonPmf(averageNeighborCount, i);
                dataset.addValue(vStat, "Stat", "" + i);
                dataset.addValue(vPoisson, "Poisson", "" + i);
                System.out.println(i + " " + vStat + " " + vPoisson);
            }
        }
        JFreeChart chart;
        ChartPanel chartPanel;
        chart = ChartFactory.createBarChart(null, "number of neighbors", "'probability'", dataset, PlotOrientation.VERTICAL, true, true, true);
        chart.setBackgroundPaint(Color.WHITE);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 600));
        JFrame f = new JFrame("Stat on random edges VS poisson distribution, lambda = K = " + averageNeighborCount);
        f.add(chartPanel);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
