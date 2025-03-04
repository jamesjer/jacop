/*
 * FloatMinimize.java
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

package org.jacop.examples.flatzinc;

import org.jacop.core.Store;
import org.jacop.floats.core.FloatDomain;
import org.jacop.floats.core.FloatInterval;
import org.jacop.floats.core.FloatVar;
import org.jacop.floats.search.Optimize;
import org.jacop.floats.search.SplitSelectFloat;
import org.jacop.fz.FlatzincLoader;
import org.jacop.search.DepthFirstSearch;

import java.util.Arrays;

/**
 * The class Run is used to run test programs for JaCoP package.
 * It is used for test purpose only.
 *
 * @author Krzysztof Kuchcinski
 * @version 4.10
 */
public class FloatMinimize {

    public static void main(String args[]) {

        FloatMinimize run = new FloatMinimize();

        run.ex(args);

    }

    FloatMinimize() {
    }

    void ex(String[] args) {

        long T1, T2, T;
        T1 = System.currentTimeMillis();

        if (args.length == 0) {
            args = new String[2];
            args[0] = "-s";
            args[1] = "camel6.fzn";
        }

        FloatDomain.setPrecision(1E-4);

        FlatzincLoader fl = new FlatzincLoader(args);
        fl.load();

        Store store = fl.getStore();

        // System.out.println (store);

        System.out.println("\nVar store size: " + store.size() + "\nNumber of constraints: " + store.numberConstraints());


        if (fl.getSearch().type() == null || (!fl.getSearch().type().equals("float_search"))) {
            throw new RuntimeException("The problem is not of type float_search and cannot be handled by this method");
        }
        if (fl.getSolve().getSolveKind() != 1) {
            throw new RuntimeException("The problem is not minimization problem and cannot be handled by this method");
        }

        FloatVar[] vars = (FloatVar[]) fl.getSearch().vars();

        System.out.println("Decision variables: " + Arrays.asList(vars) + "\n");

        DepthFirstSearch<FloatVar> label = new DepthFirstSearch<FloatVar>();
        SplitSelectFloat<FloatVar> s = new SplitSelectFloat<FloatVar>(store, vars, fl.getSearch().getFloatVarSelect().getVarSel());

        Optimize<FloatVar> min = new Optimize<FloatVar>(store, label, s, (FloatVar) fl.getCost());
        boolean result = min.minimize();

        if (result) {
            System.out.println("Final cost = " + min.getFinalCost());
            System.out.println("Variables: ");
            FloatInterval[] values = min.getFinalVarValues();
            for (int i = 0; i < vars.length; i++)
                System.out.println(vars[i].id() + " = " + values[i]);
            System.out.println("Yes");
        } else
            System.out.println("*** No");

        T2 = System.currentTimeMillis();
        T = T2 - T1;
        System.out.println("\n\t*** Execution time = " + T + " ms");

    }

}
