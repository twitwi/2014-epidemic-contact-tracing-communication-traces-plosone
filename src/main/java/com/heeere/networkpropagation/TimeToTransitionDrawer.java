/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public interface TimeToTransitionDrawer<S extends Enum<S>> {

    public double drawTime(NetworkWithNeighboringStateCount<S> network, Node n);
    
}
