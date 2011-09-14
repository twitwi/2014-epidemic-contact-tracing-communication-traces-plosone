/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public interface Plotter {

    public void wasParameters(double alpha, double betaRandom, double betaTraced, double gamma, int netSize, int averageNeighborCount);
    public void iter(int iter);
    public void statusAtTime(double time, int nI, int totalInfected);
    public void endIter();


}
