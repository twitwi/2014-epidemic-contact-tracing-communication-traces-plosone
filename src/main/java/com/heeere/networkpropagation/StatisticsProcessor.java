/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.networkpropagation;

import com.heeere.networkpropagation.AppTwoNetworks.Parameters;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight
 */
class StatisticsProcessor {

    private static class Event {

        final double time;
        final int nInfected;
        final int nTotalInfected;
        final int nTotalTraced;
        final int nTotalRemoved;
        final double tracingEffortRandom;
        final double tracingEffortContact;

        public Event(double time, int nInfected, int nTotalInfected, int nTotalTraced, int nTotalRemoved, double tracingEffortRandom, double tracingEffortContact) {
            this.time = time;
            this.nInfected = nInfected;
            this.nTotalInfected = nTotalInfected;
            this.nTotalTraced = nTotalTraced;
            this.nTotalRemoved = nTotalRemoved;
            this.tracingEffortRandom = tracingEffortRandom;
            this.tracingEffortContact = tracingEffortContact;
        }
    }
    private ArrayList<Event> elementPairs;
    private int iter;
    private List<ArrayList<Event>> seriesToSum = new ArrayList<ArrayList<Event>>();

    public void iter(int iter) {
        this.iter = iter;
        elementPairs = new ArrayList<Event>();
    }

    public void statusAtTime(double time, int nI, int totalInfected, int totalTraced, int nTotalRemoved, double tracingEffortRandom, double tracingEffortContact) {
        elementPairs.add(new Event(time, nI, totalInfected, totalTraced, nTotalRemoved, tracingEffortRandom, tracingEffortContact));
    }

    public void endIter() {
        seriesToSum.add(elementPairs);
    }

    void wasParameters(Parameters p, PrintStream out) {

        double maxTime = Double.NEGATIVE_INFINITY;
        for (ArrayList<Event> ser : seriesToSum) {
            maxTime = Math.max(maxTime, ser.get(ser.size() - 1).time);
        }
        int split = 1000;

        double averageMax = 0;
        double averageTimeOfMax = 0;
        double averageOverallInfected = 0;
        final double[][] average = new double[7][split]; // time nInfected nTotalInfected nTotalTraced nTotalRemoved effortRandom effortContact
        for (int i = 0; i < split; i++) {
            average[0][i] = maxTime * i / split;
        }

        for (int i = 0; i < seriesToSum.size(); i++) {
            ArrayList<Event> ser = seriesToSum.get(i);
            int serI = 1;
            for (int j = 0; j < average[0].length; j++) {
                while (average[0][j] > ser.get(serI).time && serI < ser.size() - 1) {
                    serI++;
                }
                average[1][j] += ser.get(serI - 1).nInfected;
                average[2][j] += ser.get(serI - 1).nTotalInfected;
                average[3][j] += ser.get(serI - 1).nTotalTraced;
                average[4][j] += ser.get(serI - 1).nTotalRemoved;
                average[5][j] += ser.get(serI - 1).tracingEffortRandom;
                average[6][j] += ser.get(serI - 1).tracingEffortContact;
            }
            double thisMax = ser.get(0).nInfected;
            double thisTimeOfMax = ser.get(0).time;
            for (Event event : ser) {
                if (thisMax < event.nInfected) {
                    thisMax = event.nInfected;
                    thisTimeOfMax = event.time;
                }
            }
            averageMax += thisMax;
            averageTimeOfMax += thisTimeOfMax;
            averageOverallInfected += ser.get(ser.size() - 1).nTotalInfected;
        }
        for (int j = 0; j < average[0].length; j++) {
            average[1][j] /= seriesToSum.size();
            average[2][j] /= seriesToSum.size();
            average[3][j] /= seriesToSum.size();
            average[4][j] /= seriesToSum.size();
            average[5][j] /= seriesToSum.size();
            average[6][j] /= seriesToSum.size();
        }
        averageMax /= seriesToSum.size();
        averageTimeOfMax /= seriesToSum.size();
        averageOverallInfected /= seriesToSum.size();

        seriesToSum.clear();

        out.format("average-infected %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[1][j]);
        }
        out.println();

        out.format("average-max-infected %g%n", averageMax);
        out.format("average-time-of-max-infected %g%n", averageTimeOfMax);
        out.format("average-overall-infected %g%n", averageOverallInfected);

        out.format("average-total-infected %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[2][j]);
        }
        out.println();

        out.format("average-total-traced %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[3][j]);
        }
        out.println();

        out.format("average-total-removed %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[4][j]);
        }
        out.println();

        out.format("average-effort-random %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[5][j]);
        }
        out.println();

        out.format("average-effort-contact %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[6][j]);
        }
        out.println();

        out.format("average-effort-total %g %d", maxTime, split);
        for (int j = 0; j < split; j++) {
            out.format(" %g", average[5][j] + average[6][j]);
        }
        out.println();

        //out.format("time-in-each-nInfected %d%n", average);
        //out.println();
    }
}
