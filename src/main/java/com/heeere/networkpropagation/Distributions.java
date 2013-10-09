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

    
    public static class ModifiableExpFactorBasePlusLambdaTimesCount<S extends Enum<S>> implements TimeToTransitionDrawer<S> {
        double base;
        double lambda;
        S s;

        public ModifiableExpFactorBasePlusLambdaTimesCount(double base, double lambda, S s) {
            this.base = base;
            this.lambda = lambda;
            this.s = s;
        }

        public void setBase(double base) {
            this.base = base;
        }

        public void setLambda(double lambda) {
            this.lambda = lambda;
        }
        
        @Override
        public double drawTime(NetworkWithNeighboringStateCount<S> net, Node n) {
            // TODO: could use or reuse a faster algorithm
            double expParameter = base + lambda * net.countNeighbors(n, s);
            // TODO: use a controlled random (seeded manually)
            return drawExponential(expParameter);
        }
    }
    public static <S extends Enum<S>> ModifiableExpFactorBasePlusLambdaTimesCount<S> modifiableExpFactorBasePlusLambdaTimesCount(final double base, final double lambda, final S s) {
        return new ModifiableExpFactorBasePlusLambdaTimesCount(base, lambda, s);
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

    // Probability Mass Function (discrete pdf)
    public static double poissonPmf(double lambda, int k) {
        // no specific optimization
        return Math.pow(lambda, k) * Math.exp(-lambda) / dfact(k);
    }

//    public static void main(String[] args) {
//        for (int i = 1; i < 25; i++) {
//            System.err.println(i + " \t" + dfact(i - 1) + " \t" + (dfact(i) / i));
//        }
//    }

    public static double dfact(int k) {
        double res = 1;
        while (k > 1) {
            res *= k;
            k--;
        }
        return res;
    }
    public static long fact(int k) {
        if (k > 20) {
            throw new IllegalArgumentException("Factorial result won't fit in a long, use the dfact to work with doubles");
        }
        long res = 1;
        while (k > 1) {
            res *= k;
            k--;
        }
        return res;
    }
}
