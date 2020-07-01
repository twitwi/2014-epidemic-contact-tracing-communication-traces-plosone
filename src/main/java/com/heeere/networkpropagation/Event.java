/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public class Event<STATE> {

    public final Node node;
    public final STATE from;
    public final STATE to;
    public final double time;

    public Event(Node node, STATE from, STATE to, double time) {
        this.node = node;
        this.from = from;
        this.to = to;
        this.time = time;
    }

}
