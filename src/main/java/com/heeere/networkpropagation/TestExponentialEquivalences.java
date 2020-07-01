/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public class TestExponentialEquivalences {

    public static void main(String[] args) {

        int n = 10000;
        double lambda = .5;
        double expectedMean = 1. / lambda;
        double expectedVar = 1. / lambda / lambda;

        double sum;
        double sumSq;

        // Normal case (with simple exponential)
        sum = sumSq = 0;
        for (int i = 0; i < n; i++) {
            double v = Distributions.drawExponential(lambda);
            sum += v;
            sumSq += v * v;
        }
        System.err.println((sum / n - expectedMean) / expectedMean + " " + ((sumSq - sum * sum / n) / n - expectedVar) / expectedVar);

        // By block case (with simple exponential)
        sum = sumSq = 0;
        for (int i = 0; i < n; i++) {
            double v = magicDrawExponential(lambda, .1);
            sum += v;
            sumSq += v * v;
        }
        System.err.println((sum / n - expectedMean) / expectedMean + " " + ((sumSq - sum * sum / n) / n - expectedVar) / expectedVar);

        // By micro-block case (with simple exponential)
        sum = sumSq = 0;
        for (int i = 0; i < n; i++) {
            double v = magicDrawExponential(lambda, .001);
            sum += v;
            sumSq += v * v;
        }
        System.err.println((sum / n - expectedMean) / expectedMean + " " + ((sumSq - sum * sum / n) / n - expectedVar) / expectedVar);
    }

    private static double magicDrawExponential(double lambda, double block) {
        double base = 0;
        while (true) {
            double v = Distributions.drawExponential(lambda);
            if (v < block) {
                return base + v;
            } else {
                base += block;
            }
        }
    }
}
