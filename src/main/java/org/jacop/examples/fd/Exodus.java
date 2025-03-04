/*
 * Exodus.java
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

package org.jacop.examples.fd;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.ArrayList;

/**
 * It is a simple logic puzzle about children reading stories.
 * <p>
 * Title: Exodus
 * Author: Sophy McHannot
 * Publication: Dell Logic
 * Puzzles Issue: April, 1998 Page: 14 Stars: 2
 * <p>
 * In preparation for Passover, five children at Hebrew school
 * (Bernice, Carl, Debby, Sammy, and Ted) have been chosen to
 * present different parts of the story of the Exodus from Egypt
 * (burning bush, captivity, Moses's youth, Passover, or the Ten
 * Commandments).  Each child is a different age (three, five,
 * seven, eight, or ten), and the family of each child has
 * recently made its own exodus to America from a different
 * country (Ethiopia, Kazakhstan, Lithuania, Morocco, or
 * Yemen). Can you find the age of each child, his or her
 * family's country of origin, and the part of the Exodus story
 * each related?
 * <p>
 * 1. Debby's family is from Lithuania.
 * <p>
 * 2. The child who told the story of the Passover is two years
 * older than Bernice.
 * <p>
 * 3. The child whose family is from Yemen is younger than the
 * child from the Ethiopian family.
 * <p>
 * 4. The child from the Moroccan family is three years older
 * than Ted.
 * <p>
 * 5. Sammy is three years older than the child who told the
 * story of Moses's youth in the house of the Pharaoh.
 * <p>
 * 6. Carl related the story of the captivity of the Israelites
 * in Egypt.
 * <p>
 * 7. The five-year-old child told the story of the Ten
 * Commandments.
 * <p>
 * 8. The child who told the story of the burning bush is either
 * two or three years older than the one whose family came
 * from Kazakhstan.
 * <p>
 * Determine: Age -- Child -- Country -- Story
 *
 * @author Duda Wojciech and Radoslaw Szymanek
 * @version 4.10
 */

public class Exodus extends ExampleFD {

    @Override public void model() {

        store = new Store();
        vars = new ArrayList<IntVar>();

        String[] firstnames = {"Bernice", "Carl", "Debby", "Sammy", "Ted"};

        String[] storynames = {"Burning_Bush", "Captivity", "Moses's_Youth", "Passover", "Ten_Commandments"};

        String[] countrynames = {"Ethiopia", "Kazakhstan", "Lithuania", "Morocco", "Yemen"};

        // Indexes for easy of referring to objects.
        int ibernice = 0, icarl = 1, idebby = 2, isammy = 3, ited = 4;
        int iburn = 0, icap = 1, imoses = 2, ipass = 3, iten = 4;
        int iet = 0, ika = 1, ili = 2, imo = 3, iye = 4;

        // Arrays of FDVs'
        IntVar name[] = new IntVar[5];
        IntVar story[] = new IntVar[5];
        IntVar country[] = new IntVar[5];

        // Creation of FDVs with appropriate name and bounding domain.
        for (int i = 0; i < 5; i++) {
            name[i] = new IntVar(store, firstnames[i], 3, 10);
            story[i] = new IntVar(store, storynames[i], 3, 10);
            country[i] = new IntVar(store, countrynames[i], 3, 10);
            vars.add(name[i]);
            vars.add(story[i]);
            vars.add(country[i]);
        }

        // Removing age values which should not be in the domain of variables.
        for (int i = 0; i < 5; i++) {
            store.impose(new XneqC(name[i], 4));
            store.impose(new XneqC(name[i], 6));
            store.impose(new XneqC(name[i], 9));
            store.impose(new XneqC(story[i], 4));
            store.impose(new XneqC(story[i], 6));
            store.impose(new XneqC(story[i], 9));
            store.impose(new XneqC(country[i], 4));
            store.impose(new XneqC(country[i], 6));
            store.impose(new XneqC(country[i], 9));
        }

        // Each child has to have a different age.
        store.impose(new Alldifferent(name));
        // Each story must be told by a child with different age.
        store.impose(new Alldifferent(story));
        // Children with different ages have left the countries.
        store.impose(new Alldifferent(country));

        // 1. Debby's family is from Lithuania.
        store.impose(new XeqY(name[idebby], country[ili]));

        // 2. The child who told the story of the Passover is
        // two years older than Bernice.

        store.impose(new XplusCeqZ(name[ibernice], 3, story[ipass]));

        // 3. The child whose family is from Yemen is younger
        // than the child from the Ethiopian family.
        store.impose(new XgtY(country[iet], country[iye]));

        // 4. The child from the Moroccan family is three years
        // older than Ted.

        store.impose(new XplusCeqZ(name[ited], 3, country[imo]));

        // 5. Sammy is three years older than the child who
        // told the story of Moses's youth in the house of the
        // Pharaoh.

        store.impose(new XplusCeqZ(story[imoses], 3, name[isammy]));

        // 6. Carl related the story of the captivity of the Israelites in
        // Egypt.
        store.impose(new XeqY(name[icarl], story[icap]));

        // 7. The five-year-old child told the story of the Ten Commandments.
        store.impose(new XeqC(story[iten], 5));

        // 8. The child who told the story of the burning bush
        // is either two or three years older than the
        // one whose family came from Kazakhstan.

        // Simple or can be used. It is also possible to use auxilary variable y
        // with the domain 2..3 and impose constraint XplusYeqZ(country[ika], y,
        // story[iburn])
        PrimitiveConstraint wiekburning[] = new PrimitiveConstraint[2];
        wiekburning[0] = new XplusCeqZ(country[ika], 2, story[iburn]);
        wiekburning[1] = new XplusCeqZ(country[ika], 3, story[iburn]);

        store.impose(new Or(wiekburning));
    }


    /**
     * It executes the program to solve this simple puzzle.
     *
     * @param args no argument is used.
     */
    public static void main(String args[]) {

        Exodus example = new Exodus();

        example.model();

        if (example.search())
            System.out.println("Solution(s) found");

    }

}
