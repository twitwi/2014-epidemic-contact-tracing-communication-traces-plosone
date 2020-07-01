/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.networkpropagation;

import java.util.Arrays;

/**
 *
 * @author twilight
 */
public class TestSpeedOfDrawing {

    public static void main(String[] args) {
        double[] lambdas = new double[100000];
        for (int i = 0; i < lambdas.length; i++) {
            lambdas[i] = Math.random() / 2.;
        }

        bench1(lambdas, 500);
        bench1(lambdas, 500);
        bench1(lambdas, 500);

        bench2(lambdas, 500);
        bench2(lambdas, 500);
        bench2(lambdas, 500);
        
        System.err.println("If the ending set of numbers are smaller (they should) then it really means it is better to use min(Exp(L_i)) = Exp(Sum(L_i)) to only have one exponential");
    }

    private static void bench1(double[] lambdas, int repeat) {
        System.gc();
        long start;
        start = System.currentTimeMillis();
        double sum = 0;
        for (int n = 0; n < repeat; n++) {
            double best = Double.POSITIVE_INFINITY;
            for (int i = 0; i < lambdas.length; i++) {
                double t = Distributions.drawExponential(lambdas[i]);
                if (t < best) {
                    best = t;
                }
            }
            sum += best;
        }
        System.err.println("1: " + (System.currentTimeMillis() - start) + " " + sum);
        System.gc();
        System.err.println("1: " + (System.currentTimeMillis() - start));
    }

    private static void bench2(double[] lambdas, int repeat) {
        System.gc();
        long start;
        start = System.currentTimeMillis();
        double sum = 0;
        for (int n = 0; n < repeat; n++) {
            double sumLambdas = 0;
            for (int i = 0; i < lambdas.length; i++) {
                sumLambdas += lambdas[i];
            }
            double best = Distributions.drawExponential(sumLambdas);
            double ind = drawFromProportionalMultinomial(lambdas, sumLambdas);
            sum += best;
        }
        System.err.println("2: " + (System.currentTimeMillis() - start) + " " + sum);
        System.gc();
        System.err.println("2: " + (System.currentTimeMillis() - start));
    }

    public static int drawFromProportionalMultinomial(double[] pObservationComesFromTopic, double sum) {
        if (sum == 0) {
            System.err.println("WARNING: should use log/exp thing as the non-log sum is 0\n  " + Arrays.toString(pObservationComesFromTopic));
            System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        }
        double u = Math.random() * sum;
        int i = 0;
        while (u > pObservationComesFromTopic[i]) {
            u -= pObservationComesFromTopic[i];
            i++;
            if (i == pObservationComesFromTopic.length) {
                System.err.println(Arrays.toString(pObservationComesFromTopic));
                System.err.println(sum);
                System.err.println(u);
            }
        }
        return i;
    }
}
