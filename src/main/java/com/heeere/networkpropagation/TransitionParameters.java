/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.networkpropagation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author twilight
 */
public class TransitionParameters<STATE extends Enum<STATE>> {

    public TransitionParameters(Class<STATE> aClass) {
        transitions = new EnumMap(aClass);
    }

    public void addTransition(STATE from, STATE to, TimeToTransitionDrawer<STATE> tttDrawer) {
        EnumMap<STATE, TimeToTransitionDrawer> m = transitions.get(from);
        if (m == null) {
            m = new EnumMap(from.getClass());
            transitions.put(from, m);
        }
        m.put(to, tttDrawer);
    }

    public Map<STATE, TimeToTransitionDrawer> getTransitionsFrom(STATE from) {
        Map<STATE, TimeToTransitionDrawer> res = transitions.get(from);
        if (res == null) {
            res = Collections.emptyMap();
        }
        return res;
    }

    EnumMap<STATE, EnumMap<STATE, TimeToTransitionDrawer>> transitions = null;
}
