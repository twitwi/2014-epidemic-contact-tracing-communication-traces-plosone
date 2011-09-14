/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

/**
 *
 * @author twilight
 */
public class JFreechartPlotter implements Plotter {

    private static class Id implements Comparable<Id> {
        public static AtomicInteger globalId = new AtomicInteger(0);
        int id;
        String string;
        public Id(String string) {
            id = globalId.incrementAndGet();
            this.string = string;
        }
        @Override
        public String toString() {
            return string;
        }

        @Override
        public int compareTo(Id o) {
            return id - o.id;
        }
    }

    JFrame f = new JFrame("Graphs");
    DefaultXYDataset dataset = new DefaultXYDataset();
    JFreeChart chart;
    ChartPanel chartPanel;
    Plotter chain;
    PrintStream out = null;
    
    public JFreechartPlotter(File outputOrNull, Plotter chain) throws IOException {
        this.chain = chain;
        if (outputOrNull != null) {
            out = new PrintStream(outputOrNull);
        }
        chart = ChartFactory.createXYLineChart(null, "time", "#infected", dataset, PlotOrientation.VERTICAL, true, true, true);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setRangeAxis(new LogAxis());
        //chart.getXYPlot().setRenderer(new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES_AND_LINES));
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 600));
        f.add(chartPanel);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private List<Id> seriesToRemove = new ArrayList<Id>();
    private List<double[][]> seriesToSum = new ArrayList<double[][]>();

    @Override
    public void wasParameters(double alpha, double betaRandom, double betaTraced, double gamma, int netSize, int averageNeighborCount) {
        if (chain != null) {
            chain.wasParameters(alpha, betaRandom, betaTraced, gamma, netSize, averageNeighborCount);
        }
        if (!seriesToSum.isEmpty()) {
            if (out != null) {
                out.format(Locale.US, "params: %f %f %f %f %d %d\n", alpha, betaRandom, betaTraced, gamma, netSize, averageNeighborCount);
            }
            double maxTime = Double.NEGATIVE_INFINITY;
            for (double[][] ser : seriesToSum) {
                maxTime = Math.max(ser[0][ser[0].length - 1], maxTime);
            }
            int split = 1000;
            final double[][] average = new double[2][split];
            for (int i = 0; i < split; i++) {
                average[0][i] = maxTime * i / split;
            }

            for (int i = 0; i < seriesToSum.size(); i++) {
                double[][] ser = seriesToSum.get(i);
                int serI = 1;
                for (int j = 0; j < average[0].length; j++) {
                    while (average[0][j] > ser[0][serI] && serI < ser[0].length - 1) {
                        serI++;
                    }
                    average[1][j] += ser[1][serI];
                }
            }
            for (int j = 0; j < average[0].length; j++) {
                average[1][j] /= seriesToSum.size();
                if (out != null) {
                    out.format(Locale.US, "%f:%f ", average[0][j], average[1][j]);
                }
            }
            if (out != null) {
                out.append("\n");
                out.flush();
            }
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        for (Id id : seriesToRemove) {
                            dataset.removeSeries(id);
                        }
                        dataset.addSeries(new Id("Mean"), average);
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(JFreechartPlotter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JFreechartPlotter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        seriesToRemove.clear();
        seriesToSum.clear();
    }

    private ArrayList<Double> elementPairs;
    private int iter;

    @Override
    public void iter(int iter) {
        if (chain != null) {
            chain.iter(iter);
        }
        this.iter = iter;
        elementPairs = new ArrayList<Double>();
    }

    @Override
    public void statusAtTime(double time, int nI, int totalInfected) {
        if (chain != null) {
            chain.statusAtTime(time, nI, totalInfected);
        }
        elementPairs.add(time);
        elementPairs.add((double) totalInfected); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Override
    public void endIter() {
        if (chain != null) {
            chain.endIter();
        }
        int size = elementPairs.size() / 2;
        double[][] values = new double[2][size];
        for (int l = 0; l < elementPairs.size(); l += 2) {
            values[0][l / 2] = elementPairs.get(l);
            values[1][l / 2] = elementPairs.get(l + 1);
            values[1][l / 2] = values[1][l / 2] == 0 ? 1 : values[1][l / 2]; // for the log scale
        }
        seriesToSum.add(values);
        /*
        Id id = new Id("Iteration " + iter);
        seriesToRemove.add(id);
        dataset.addSeries( id, values);
         */
    }    

}
