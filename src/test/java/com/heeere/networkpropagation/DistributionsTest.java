/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.networkpropagation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twilight
 */
public class DistributionsTest {

    @Test
    public void testDrawExponential() {
        for (double d : new double[]{.1, .33, 1, 2, 33, 2000}) {
            double sum = 0;
            int samples = 1000000;
            for (int i = 0; i < samples; i++) {
                sum += Distributions.drawExponential(d);
            }
            double mean = sum / samples;
            double expectedMean = 1 / d;
            assertEquals(expectedMean, mean, .05);
        }
    }

    @Test
    public void testFactorial() {
        assertEquals(1l, Distributions.fact(0));
        assertEquals(1l, Distributions.fact(1));
        assertEquals(2l, Distributions.fact(2));
        assertEquals(6l, Distributions.fact(3));
        assertEquals(479001600l, Distributions.fact(12));
    }
}