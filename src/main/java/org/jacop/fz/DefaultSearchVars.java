/*
 * DefaultSearchVars.java
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
package org.jacop.fz;

import org.jacop.core.BooleanVar;
import org.jacop.core.IntVar;
import org.jacop.core.Var;
import org.jacop.floats.core.FloatVar;
import org.jacop.set.core.SetVar;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;


/**
 * The class gathers variables and array variables for default or
 * complementary search. Two methods are supported. One gathers all
 * output variables and the second one all non-introduced variables
 * and arrays.
 *
 * @author Krzysztof Kuchcinski
 * @version 4.10
 */
public class DefaultSearchVars {

    IntVar[] int_search_variables = new IntVar[0];
    SetVar[] set_search_variables = new SetVar[0];
    BooleanVar[] bool_search_variables = new BooleanVar[0];

    FloatVar[] float_search_variables = new FloatVar[0];

    Tables dictionary;

    private Comparator<Var> domainSizeComparator = (o1, o2) -> {
        int v1 = o1.getSize();
        int v2 = o2.getSize();
        return v1 - v2;
    };

    /**
     * It constructs the class for collecting default and complementary search variables.
     *
     * @param dict tables with model variables.
     */
    public DefaultSearchVars(Tables dict) {
        this.dictionary = dict;
    }

    /**
     * It collects all output variables for search.
     */
    void outputVars() {

        // ==== Collect ALL OUTPUT variables ====

        LinkedHashSet<IntVar> int_vars = new LinkedHashSet<IntVar>();
        LinkedHashSet<BooleanVar> bool_vars = new LinkedHashSet<BooleanVar>();
        LinkedHashSet<SetVar> set_vars = new LinkedHashSet<SetVar>();
        LinkedHashSet<FloatVar> float_vars = new LinkedHashSet<FloatVar>();

        // collect output arrays
        for (int i = 0; i < dictionary.outputArray.size(); i++)
            for (Var v : dictionary.outputArray.get(i).getArray()) {
                if (v instanceof BooleanVar) {
                    if (!v.singleton())
                        bool_vars.add((BooleanVar)v);
                } else if (v instanceof IntVar) {
                    if (!v.singleton())
                        int_vars.add((IntVar)v);
                } else if (v instanceof SetVar)
                    set_vars.add((SetVar)v);
                else if (v instanceof FloatVar)
                    float_vars.add((FloatVar) v);
            }
        // collect output variables
        for (Var v : dictionary.outputVariables) {
            if (v instanceof BooleanVar) {
                if (!v.singleton())
                    bool_vars.add((BooleanVar)v);
            } else if (v instanceof IntVar) {
                if (!v.singleton())
                    int_vars.add((IntVar)v);
            } else if (v instanceof SetVar)
                set_vars.add((SetVar)v);
            else if (v instanceof FloatVar)
                float_vars.add((FloatVar) v);
        }
        int_search_variables = int_vars.toArray(new IntVar[int_vars.size()]);
        bool_search_variables = bool_vars.toArray(new BooleanVar[bool_vars.size()]);
        set_search_variables = set_vars.toArray(new SetVar[set_vars.size()]);
        float_search_variables = float_vars.toArray(new FloatVar[float_vars.size()]);

        Arrays.sort(int_search_variables, domainSizeComparator);
    }

    /**
     * It collects all variables that were identified as search
     * variables by VariablesParameters class during parsing variable
     * definitions.
     */
    void defaultVars() {

        LinkedHashSet<IntVar> int_vars = new LinkedHashSet<IntVar>();
        LinkedHashSet<BooleanVar> bool_vars = new LinkedHashSet<BooleanVar>();
        Set<Map.Entry<IntVar, IntVar>> aliasEntries = dictionary.aliasTable.entrySet();

	Set<IntVar> aliasVars = new LinkedHashSet<IntVar>();
	// collect all boolean variables with int var alias
        for (Map.Entry<IntVar, IntVar> e : aliasEntries) {
            IntVar b = e.getKey();
	    aliasVars.add(b);
	}

        for (int i = 0; i < dictionary.defaultSearchArrays.size(); i++)
            for (Var v : dictionary.defaultSearchArrays.get(i)) {
		if (!v.singleton())
		    if (v instanceof BooleanVar)
                    bool_vars.add((BooleanVar)v);
		    else  if (((IntVar)v).min() >= 0 && ((IntVar)v).max() <= 1 && aliasVars.contains(v))
		        bool_vars.add((BooleanVar)v);
		    else
			int_vars.add((IntVar)v);
            }
        for (Var v : dictionary.defaultSearchVariables) {
	    if (!v.singleton())
	    	if (v instanceof BooleanVar)
	    	    bool_vars.add((BooleanVar)v);
	    	else if (((IntVar)v).min() >= 0 && ((IntVar)v).max() <= 1 && aliasVars.contains(v))
	    	    bool_vars.add((BooleanVar)v);
	    	else
		    int_vars.add((IntVar)v);
        }
        int_search_variables = int_vars.toArray(new IntVar[int_vars.size()]);
        bool_search_variables = bool_vars.toArray(new BooleanVar[bool_vars.size()]);

        Arrays.sort(int_search_variables, domainSizeComparator);

        LinkedHashSet<SetVar> set_vars = new LinkedHashSet<SetVar>();
        for (int i = 0; i < dictionary.defaultSearchSetArrays.size(); i++)
            for (Var v : dictionary.defaultSearchSetArrays.get(i))
                set_vars.add((SetVar)v);
        for (Var v : dictionary.defaultSearchSetVariables)
            set_vars.add((SetVar)v);

        set_search_variables = set_vars.toArray(new SetVar[set_vars.size()]);

        LinkedHashSet<FloatVar> float_vars = new LinkedHashSet<FloatVar>();

        for (int i = 0; i < dictionary.defaultSearchFloatArrays.size(); i++)
            for (Var v : dictionary.defaultSearchFloatArrays.get(i))
                float_vars.add((FloatVar) v);
        for (Var v : dictionary.defaultSearchFloatVariables)
            float_vars.add((FloatVar) v);

        float_search_variables = float_vars.toArray(new FloatVar[float_vars.size()]);
        // ==== End collect guessed search variables ====
    }

    IntVar[] getIntVars() {
        return int_search_variables;
    }

    SetVar[] getSetVars() {
        return set_search_variables;
    }

    BooleanVar[] getBoolVars() {
        return bool_search_variables;
    }

    FloatVar[] getFloatVars() {
        return float_search_variables;
    }

    public String toString() {

        StringBuffer buf = new StringBuffer();

        buf.append("%% default int search variables = array1d(1..");
        buf.append(int_search_variables.length + ", ");
        buf.append(Arrays.asList(int_search_variables));
        buf.append(")\n");

        buf.append("%% default boolean search variables = array1d(1..");
        buf.append(bool_search_variables.length + ", ");
        buf.append(Arrays.asList(bool_search_variables));
        buf.append(")\n");

        buf.append("%% default set search variables = array1d(1..");
        buf.append(set_search_variables.length + ", ");
        buf.append(Arrays.asList(set_search_variables));
        buf.append(")\n");

        buf.append("%% default float search variables = array1d(1..");
        buf.append(float_search_variables.length + ", ");
        buf.append(Arrays.asList(float_search_variables));
        buf.append(")\n");

        return buf.toString();
    }

}
