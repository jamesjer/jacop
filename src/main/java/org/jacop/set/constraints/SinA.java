/*
 * SinA.java
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

package org.jacop.set.constraints;

import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.core.IntDomain;
import org.jacop.core.Store;
import org.jacop.set.core.SetDomain;
import org.jacop.set.core.SetVar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * It creates an inclusion set constraint to make sure that provided set is
 * included in a set variable a.
 *
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski
 * @version 4.10
 */

public class SinA extends PrimitiveConstraint {

    static AtomicInteger idNumber = new AtomicInteger(0);

    /**
     * It specifies the set s which must be in variable a.
     */
    public IntDomain set;

    /**
     * It specifies variable a within which it must contains set s.
     */
    public SetVar a;

    /**
     * It specifies if the inclusion relation is strict.
     */
    public boolean strict;

    /**
     * It creates a set inclusion constraint.
     *
     * @param a      variable which value must include a provided set.
     * @param set    a set that must be included within a provided set variable a.
     * @param strict it specifies if the inclusion relation is strict.
     */
    public SinA(IntDomain set, SetVar a, boolean strict) {

        checkInputForNullness(new String[] {"set", "a"}, new Object[] {set, a});

        numberId = idNumber.incrementAndGet();

        this.a = a;
        this.set = set;
        this.strict = strict;

        setScope(a);

    }

    /**
     * It creates a set inclusion constraint. It is not strict by default.
     *
     * @param a   variable which value must include a provided set.
     * @param set a set that must be included within a provided set variable a.
     */
    public SinA(IntDomain set, SetVar a) {

        this(set, a, false);

    }

    @Override public void consistency(Store store) {

        /**
         * This consistency enforces the following rules.
         *
         * if (s not in lubA) then fail.
         *
         * glbA = glbA \/ S
         *
         *
         */

        a.domain.inGLB(store.level, a, set);

        if (strict)
            a.domain.inCardinality(store.level, a, set.getSize() + 1, Integer.MAX_VALUE);

    }

    @Override public int getDefaultConsistencyPruningEvent() {
        return SetDomain.ANY;
    }

    @Override public boolean satisfied() {
        return grounded() && a.domain.lub().lex(set) < 0;
    }

    @Override public String toString() {
        return id() + " : SinA(" + set + " '< " + a + ")";
    }

    @Override protected int getDefaultNestedNotConsistencyPruningEvent() {
        return SetDomain.ANY;
    }

    @Override protected int getDefaultNestedConsistencyPruningEvent() {
        return SetDomain.ANY;
    }

    @Override protected int getDefaultNotConsistencyPruningEvent() {
        return SetDomain.ANY;
    }

    @Override public void notConsistency(Store store) {

        // TODO, test it properly.

        if (set.getSize() > a.domain.lub().getSize() + 1)
            return;

        if (!a.domain.lub().contains(set))
            return;

        IntDomain result = set.subtract(a.domain.glb());

        if (result.isEmpty())
            if (strict) {
                if (set.getSize() < a.domain.glb().getSize())
                    throw Store.failException;
                else {
                    // set contains only elements within a.domain.glb()
                    // set does not have less elements a.domain.glb()
                    // => a must be equal to set to make strict relation not true.
                    a.domain.inGLB(store.level, a, set);
                }
            } else
                throw Store.failException;

        if (!strict && result.getSize() == 1) {
            a.domain.inLUBComplement(store.level, a, result.value());
        }

        if (strict && result.getSize() == 1 && set.getSize() - 1 < a.domain.glb().getSize()) {
            a.domain.inLUBComplement(store.level, a, result.value());
        }


    }

    @Override public boolean notSatisfied() {
        return !a.domain.lub().contains(set) || (strict && set.eq(a.domain.lub()));
    }

}
