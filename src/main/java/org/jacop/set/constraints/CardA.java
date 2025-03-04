/*
 * CardA.java
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

import org.jacop.api.SatisfiedPresent;
import org.jacop.constraints.Constraint;
import org.jacop.core.IntDomain;
import org.jacop.core.IntervalDomain;
import org.jacop.core.Store;
import org.jacop.set.core.SetDomain;
import org.jacop.set.core.SetVar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The set cardinality constraint.
 *
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski
 * @version 4.10
 */

public class CardA extends Constraint implements SatisfiedPresent {

    static AtomicInteger idNumber = new AtomicInteger(0);

    /**
     * It specifies a set variable x which is being restricted.
     */
    public SetVar a;

    /**
     * It specifies variable c specifying the possible cardinality of variable x.
     */
    public IntDomain cardinality;

    /**
     * It constructs a cardinality constraint to restrict the number of elements
     * in the set assigned to a set variable a.
     *
     * @param a variable that is restricted to have the cardinality c.
     * @param c the value specifying  cardinality of variable a.
     */
    public CardA(SetVar a, int c) {

        this(a);
        this.cardinality = new IntervalDomain(c, c);

    }

    /**
     * It constructs a cardinality constraint to restrict the number of elements
     * in the set assigned to set variable a.
     *
     * @param a variable that is restricted to have the cardinality c.
     * @param c domain for the cardinality variable.
     */
    public CardA(SetVar a, IntDomain c) {

        this(a);
        this.cardinality = c.cloneLight();

    }

    /**
     * It constructs a cardinality constraint to restrict the number of elements
     * in the set assigned to set variable a.
     *
     * @param a   variable that is restricted to have the cardinality [min, max].
     * @param min the minimum value possible for the cardinality of a.
     * @param max the maximum value possible for the cardinality of a.
     */
    public CardA(SetVar a, int min, int max) {

        this(a);
        this.cardinality = new IntervalDomain(min, max);

    }

    private CardA(SetVar a) {

        checkInputForNullness("a", new Object[] {a});
        numberId = idNumber.incrementAndGet();
        this.a = a;
        setScope(a);

    }

    @Override public void consistency(Store store) {

        /**
         * It computes the consistency of the constraint.
         *
         * #A in (min, max)
         *
         * Cardinality of set variable A is within interval (min, max).
         *
         */

        SetDomain aDom = a.domain;

        int min = Math.max(aDom.glb().getSize(), cardinality.min());
        int max = Math.min(aDom.lub().getSize(), cardinality.max());

        if (min > max)
            throw Store.failException;

        /**
         * If #glbA is already equal to maximum allowed cardinality then set is specified by glbA.
         * if (#glbA == max) then A = glbA
         * If #lubA is already equal to minimum allowed cardinality then set is specified by lubA.
         * if (#lubA == min) then A = lubA
         *
         */

        a.domain.inCardinality(store.level, a, min, max);

    }

    @Override public int getDefaultConsistencyPruningEvent() {
        return SetDomain.ANY;
    }

    @Override public boolean satisfied() {
        return grounded() && cardinality.contains(a.domain.glb().getSize());
    }

    @Override public String toString() {
        return id() + " : cardA(" + a + ", " + cardinality + " )";
    }

}
