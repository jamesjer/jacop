/*
 * CircuitVarValue.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jacop.constraints;

import org.jacop.core.MutableVarValue;

/**
 * Defines a current value of the CircuitVar and related operations on it.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

class CircuitVarValue implements MutableVarValue, Cloneable {

    int next = 0, previous = 0;

    CircuitVarValue nextCircuitVarValue = null;

    int stamp = 0;

    CircuitVarValue() {
    }

    CircuitVarValue(int n, int p) {
        next = n;
        previous = p;
    }

    @Override public Object clone() {

        CircuitVarValue val = new CircuitVarValue(next, previous);
        val.stamp = stamp;
        val.nextCircuitVarValue = nextCircuitVarValue;
        return val;
    }

    public MutableVarValue previous() {
        return nextCircuitVarValue;
    }

    public void setPrevious(MutableVarValue nn) {
        nextCircuitVarValue = (CircuitVarValue) nn;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    void setValue(int n, int p) {
        next = n;
        previous = p;
    }

    public int stamp() {
        return stamp;
    }

    @Override public String toString() {
        return "[" + next + ", " + previous + "]";
    }

}
