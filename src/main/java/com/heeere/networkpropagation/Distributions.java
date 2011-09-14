/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public class Distributions {

    public static <S extends Enum<S>> TimeToTransitionDrawer expFactorTimesCount(final double lambda, final S s) {
        return new TimeToTransitionDrawer<S>() {
            @Override
            public double drawTime(NetworkWithNeighboringStateCount<S> net, Node n) {
                // TODO: could use or reuse a faster algorithm
                double expParameter = lambda * net.countNeighbors(n, s);
                // TODO: use a controlled random (seeded manually)
                return drawExponential(expParameter);
            }

        };
    }

    public static <S extends Enum<S>> TimeToTransitionDrawer expFactorBasePlusLambdaTimesCount(final double base, final double lambda, final S s) {
        return new TimeToTransitionDrawer<S>() {
            @Override
            public double drawTime(NetworkWithNeighboringStateCount<S> net, Node n) {
                // TODO: could use or reuse a faster algorithm
                double expParameter = base + lambda * net.countNeighbors(n, s);
                // TODO: use a controlled random (seeded manually)
                return drawExponential(expParameter);
            }

        };
    }

    public static <S extends Enum<S>> TimeToTransitionDrawer<S> exp(final double lambda) {
        return new TimeToTransitionDrawer<S>() {
            @Override
            public double drawTime(NetworkWithNeighboringStateCount<S> net, Node n) {
                return drawExponential(lambda);
            }

        };
    }
    
    public static double drawExponential(double lambda) {
        // expParameter is lambda (that is 1/mu), mu is the mean of the exponential distribution
        return -(Math.log(Math.random()) / lambda);
    }

}
