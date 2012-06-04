/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.networkpropagation;

/**
 *
 * @author twilight
 */
public class ParameterReader {
    
    private String[] args;
    private int cur = 0;

    public ParameterReader(String[] args) {
        this.args = args.clone();
    }
    
    public int i() {
        return Integer.parseInt(args[cur++]);
    }
    public double d() {
        return Double.parseDouble(args[cur++]);
    }
}
