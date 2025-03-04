/*
 * Diff2.java
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

import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Diff2 constraint assures that any two rectangles from a vector of
 * rectangles does not overlap in at least one direction.
 * <p>
 * Zero-width rectangles can be packed anywhere.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.10
 */

public class Diff2 extends Diff {

    static AtomicInteger idNumber = new AtomicInteger(0);

    Diff2Var EvalRects[];

    boolean exceptionListPresent = false;

    /**
     * It specifies a list of pairs of rectangles which can overlap.
     */
    public int[] exclusiveList = new int[0];

    /**
     * Conditional Diff2. The rectangles that are specified on the list
     * Exclusive list is specified contains pairs of rectangles
     * that are excluded from checking that they must be non-overlapping.
     * The rectangles are numbered from 1, for example list [1, 3, 3, 4]
     * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
     * other.
     *
     * @param rectangles    a list of rectangles.
     * @param exclusiveList a list denoting the pair of rectangles, which can overlap
     * @param doProfile     should profile be computed and used.
     */
    public Diff2(Rectangle[] rectangles, int[] exclusiveList, boolean doProfile) {

        checkInputForNullness("rectangles", rectangles);
        checkInputForNullness("exlusiveList", exclusiveList);
        checkInput(rectangles, i -> i.dim == 2, "rectangle should have exactly two dimensions");

        this.queueIndex = 2;
        this.numberId = idNumber.incrementAndGet();

        this.rectangles = Arrays.copyOf(rectangles, rectangles.length);
        this.exclusiveList = Arrays.copyOf(exclusiveList, exclusiveList.length);
        this.doProfile = doProfile;
        exceptionListPresent = true;

        setScope(Rectangle.getStream(this.rectangles));

    }

    /**
     * It creates a diff2 constraint.
     *
     * @param o1      list of variables denoting the origin in the first dimension.
     * @param o2      list of variables denoting the origin in the second dimension.
     * @param l1      list of variables denoting the length in the first dimension.
     * @param l2      list of variables denoting the length in the second dimension.
     * @param profile specifies if the profile should be computed.
     */
    public Diff2(List<? extends IntVar> o1, List<? extends IntVar> o2, List<? extends IntVar> l1, List<? extends IntVar> l2, boolean profile) {
        this(o1, o2, l1, l2);
        doProfile = profile;
    }

    /**
     * It creates a diff2 constraint.
     *
     * @param rectangles list of rectangles with origins and lengths in both dimensions.
     */

    public Diff2(List<? extends List<? extends IntVar>> rectangles) {

        queueIndex = 2;
        this.rectangles = Rectangle.toArrayOf2DRectangles(rectangles);
        numberId = idNumber.incrementAndGet();

        setScope(rectangles.stream().map(i -> i.stream()).flatMap(i -> i));
    }

    /**
     * It creates a diff2 constraint.
     *
     * @param rectangles list of rectangles with origins and lengths in both dimensions.
     * @param profile    specifies if the profile is computed and used.
     */

    public Diff2(List<? extends List<? extends IntVar>> rectangles, boolean profile) {
        this(rectangles);
        doProfile = profile;
    }


    /**
     * It creates a diff2 constraint.
     *
     * @param o1 list of variables denoting the origin in the first dimension.
     * @param o2 list of variables denoting the origin in the second dimension.
     * @param l1 list of variables denoting the length in the first dimension.
     * @param l2 list of variables denoting the length in the second dimension.
     */
    public Diff2(List<? extends IntVar> o1, List<? extends IntVar> o2, List<? extends IntVar> l1, List<? extends IntVar> l2) {

        this(o1.toArray(new IntVar[o1.size()]), o2.toArray(new IntVar[o2.size()]), l1.toArray(new IntVar[l1.size()]),
            l2.toArray(new IntVar[l2.size()]));

    }

    /**
     * It creates a diff2 constraint.
     *
     * @param origin1 list of variables denoting the origin in the first dimension.
     * @param origin2 list of variables denoting the origin in the second dimension.
     * @param length1 list of variables denoting the length in the first dimension.
     * @param length2 list of variables denoting the length in the second dimension.
     */

    public Diff2(IntVar[] origin1, IntVar[] origin2, IntVar[] length1, IntVar[] length2) {

        checkInputForNullness(new String[] {"origin1", "origin2", "length1", "length2"},
            new Object[][] {origin1, origin2, length1, length2});

        queueIndex = 2;
        this.rectangles = Rectangle.toArrayOf2DRectangles(origin1, origin2, length1, length2);
        numberId = idNumber.incrementAndGet();

        setScope(origin1, origin2, length1, length2);

    }

    /**
     * It creates a diff2 constraint.
     *
     * @param o1      list of variables denoting the origin in the first dimension.
     * @param o2      list of variables denoting the origin in the second dimension.
     * @param l1      list of variables denoting the length in the first dimension.
     * @param l2      list of variables denoting the length in the second dimension.
     * @param profile specifies if the profile should be computed.
     */
    public Diff2(IntVar[] o1, IntVar[] o2, IntVar[] l1, IntVar[] l2, boolean profile) {
        this(o1, o2, l1, l2);
        doProfile = profile;
    }

    /**
     * It creates a diff2 constraint.
     *
     * @param rectangles list of rectangles with origins and lengths in both dimensions.
     */

    public Diff2(IntVar[][] rectangles) {

        assert (rectangles != null) : "Rectangles list is null";

        queueIndex = 2;
        this.rectangles = Rectangle.toArrayOf2DRectangles(rectangles);
        numberId = idNumber.incrementAndGet();

        setScope(Rectangle.getStream(this.rectangles));

    }

    /**
     * It creates a diff2 constraint.
     *
     * @param rectangles list of rectangles with origins and lengths in both dimensions.
     * @param profile    specifies if the profile is computed and used.
     */

    public Diff2(IntVar[][] rectangles, boolean profile) {
        this(rectangles);
        doProfile = profile;
    }

    /**
     * Conditional Diff2. The rectangles that are specified on the list
     * Exclusive are excluded from checking that they must be non-overlapping.
     * The rectangles are numbered from 1, for example list [[1,3], [3,4]]
     * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
     * other.
     *
     * @param rectangles    - list of rectangles, each rectangle represented by a list of variables.
     * @param exclusiveList - list of rectangles pairs which can overlap.
     */
    public Diff2(List<List<? extends IntVar>> rectangles, List<List<Integer>> exclusiveList) {

        queueIndex = 2;
        this.rectangles = Rectangle.toArrayOf2DRectangles(rectangles);
        numberId = idNumber.incrementAndGet();

        exceptionListPresent = true;

        List<Integer> list = new ArrayList<Integer>(exclusiveList.size() * 2);

        for (List<Integer> pair : exclusiveList)
            for (Integer item : pair)
                list.add(item);

        this.exclusiveList = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            this.exclusiveList[i] = list.get(i);

        setScope(rectangles.stream().map(i -> i.stream()).flatMap(i -> i));

    }

    /**
     * Conditional Diff2. The rectangles that are specified on the list
     * Exclusive are excluded from checking that they must be non-overlapping.
     * The rectangles are numbered from 1, for example list [[1,3], [3,4]]
     * specifies that rectangles 1 and 3 as well as 3 and 4 can overlap each
     * other.
     *
     * @param rect      - list of rectangles, each rectangle represented by a list of variables.
     * @param exclusive - list of rectangles pairs which can overlap.
     */
    public Diff2(IntVar[][] rect, List<List<Integer>> exclusive) {

        queueIndex = 2;
        this.rectangles = Rectangle.toArrayOf2DRectangles(rect);
        numberId = idNumber.incrementAndGet();

        exceptionListPresent = true;

        List<Integer> list = new ArrayList<Integer>(exclusive.size() * 2);

        for (List<Integer> pair : exclusive)
            for (Integer item : pair)
                list.add(item);

        this.exclusiveList = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            this.exclusiveList[i] = list.get(i);

        setScope(Rectangle.getStream(this.rectangles));

    }

    private Rectangle[] onList(int index, int[] exclusiveList) {

        List<Rectangle> list = new ArrayList<Rectangle>();

        for (int i = 0; i < rectangles.length; i++)
            if (notOverlapping(index + 1, i + 1, exclusiveList))
                list.add(rectangles[i]);

        return list.toArray(new Rectangle[list.size()]);
    }

    boolean notOverlapping(int i, int j, int[] exclusiveList) {

        boolean onList = false;
        int l = 0;

        while (!onList && l < exclusiveList.length / 2) {
            int el1 = exclusiveList[l * 2];
            int el2 = exclusiveList[l * 2 + 1];

            onList = (i == el1 && j == el2) || (i == el2 && j == el1);
            l++;
        }

        return !onList;
    }

    @Override public void impose(Store store) {

        super.impose(store);

        if (this.exclusiveList.length == 0) {
            EvalRects = new Diff2Var[rectangles.length];

            for (int j = 0; j < EvalRects.length; j++)
                EvalRects[j] = new Diff2Var(store, this.rectangles);
        } else {

            EvalRects = new Diff2Var[rectangles.length];

            for (int j = 0; j < EvalRects.length; j++)
                EvalRects[j] = new Diff2Var(store, onList(j, exclusiveList));
        }

    }

    @Override void narrowRectangles(Set<IntVar> fdvQueue) {

        boolean needToNarrow = false;

        for (int l = 0; l < rectangles.length; l++) {
            Rectangle r = rectangles[l];

            boolean settled = true, minLengthEq0 = false;
            int maxLevel = 0;
            for (int i = 0; i < r.dim(); i++) {
                IntDomain rOrigin = r.origin[i].dom();
                IntDomain rLength = r.length[i].dom();
                settled = settled && rOrigin.singleton() && rLength.singleton();

                minLengthEq0 = minLengthEq0 || (rLength.min() <= 0);

                int originStamp = rOrigin.stamp, lengthStamp = rLength.stamp;
                if (maxLevel < originStamp)
                    maxLevel = originStamp;
                if (maxLevel < lengthStamp)
                    maxLevel = lengthStamp;
            }

            if (!minLengthEq0 && // Check for rectangle r which has
                // all lengths > 0
                !(settled && maxLevel < currentStore.level)) {
                // and are not fixed already

                needToNarrow = needToNarrow || containsChangedVariable(r, fdvQueue);

                List<IntRectangle> UsedRect = new ArrayList<IntRectangle>();
                List<Rectangle> ProfileCandidates = new ArrayList<Rectangle>();
                List<Rectangle> OverlappingRects = new ArrayList<Rectangle>();
                boolean ntN = findRectangles(r, l, UsedRect, ProfileCandidates, OverlappingRects, fdvQueue);

                needToNarrow = needToNarrow || ntN;

                // Checking r against all s with minUse in the domain of r
                if (needToNarrow) {

                    if (OverlappingRects.size() != ((Diff2VarValue) EvalRects[l].value()).Rects.length) {
                        Diff2VarValue newRects = new Diff2VarValue();
                        newRects.setValue(OverlappingRects);
                        EvalRects[l].update(newRects);
                    }

                    narrowRectangle(r, UsedRect, ProfileCandidates);
                }
            }
        }
    }

    boolean findRectangles(Rectangle r, int index, List<IntRectangle> UsedRect, List<Rectangle> ProfileCandidates,
        List<Rectangle> OverlappingRects, Set<IntVar> fdvQueue) {

        boolean contains = false, checkArea = false;

        long area = 0;
        long commonArea = 0;
        int totalNumberOfRectangles = 0;
        int dim = r.dim();
        int startMin[] = new int[dim];
        int stopMax[] = new int[dim];
        int minLength[] = new int[dim];
        int r_min[] = new int[dim], r_max[] = new int[dim];
        for (int i = 0; i < startMin.length; i++) {
            IntDomain rLengthDom = r.length[i].dom();
            startMin[i] = IntDomain.MaxInt;
            stopMax[i] = 0;
            minLength[i] = rLengthDom.min();

            IntDomain rOriginDom = r.origin[i].dom();
            r_min[i] = rOriginDom.min();
            r_max[i] = rOriginDom.max() + rLengthDom.max();
        }

        for (Rectangle s : ((Diff2VarValue) EvalRects[index].value()).Rects) {
            boolean overlap = true;

            if (r != s) {

                boolean sChanged = containsChangedVariable(s, fdvQueue);

                IntRectangle Use = new IntRectangle(dim);
                long sArea = 1;
                long partialCommonArea = 1;

                boolean use = true, minLength0 = false;
                int s_min, s_max, start, stop;
                int m = 0, j = 0;
                int sOriginMin[] = new int[dim], sOriginMax[] = new int[dim], sLengthMin[] = new int[dim];

                while (overlap && m < dim) {
                    // check if domains of r and s overlap
                    IntDomain sOriginIdom = s.origin[m].dom();
                    IntDomain sLengthIdom = s.length[m].dom();
                    int sLengthIMin = sLengthIdom.min();
                    int sOriginIMax = sOriginIdom.max();
                    s_min = sOriginIdom.min();
                    s_max = sOriginIMax + sLengthIdom.max();
                    overlap = overlap && intervalOverlap(r_min[m], r_max[m], s_min, s_max);

                    // min start, max stop and min length
                    sOriginMin[m] = s_min;
                    sOriginMax[m] = sOriginIMax + sLengthIMin;
                    sLengthMin[m] = sLengthIMin;

                    // check if s occupies some space
                    start = sOriginIMax;
                    stop = s_min + sLengthIMin;
                    if (start < stop) {
                        Use.add(start, stop - start);
                        j++;
                    } else
                        use = false;

                    minLength0 = minLength0 || (sLengthMin[m] <= 0);

                    m++;
                }

                if (overlap) {

                    OverlappingRects.add(s);

                    if (use) { // rectangles taking space
                        UsedRect.add(Use);
                        contains = contains || sChanged;
                    }

                    if (!minLength0) { // profile candiates
                        if (j > 0) {
                            ProfileCandidates.add(s);
                            contains = contains || sChanged;
                        }

                        checkArea = true;
                        totalNumberOfRectangles++;
                        for (int i = 0; i < dim; i++) {
                            if (sOriginMin[i] < startMin[i])
                                startMin[i] = sOriginMin[i];
                            if (sOriginMax[i] > stopMax[i])
                                stopMax[i] = sOriginMax[i];
                            if (minLength[i] > sLengthMin[i])
                                minLength[i] = sLengthMin[i];

                            sArea = sArea * sLengthMin[i];
                        }
                        area += sArea;
                    } // profile candidate end

                    // calculate area within rectangle r possible placement
                    for (int i = 0; i < dim; i++) {
                        if (sOriginMin[i] <= r_min[i]) {
                            if (sOriginMax[i] <= r_max[i]) {
                                int distance1 = sOriginMin[i] + sLengthMin[i] - r_min[i];
                                sLengthMin[i] = (distance1 > 0) ? distance1 : 0;
                            } else {
                                // sOriginMax[i] > r_max[i])
                                int rmax = r.origin[i].max() + r.length[i].min();

                                int distance1 = sOriginMin[i] + sLengthMin[i] - r_min[i];
                                int distance2 = sLengthMin[i] - (sOriginMax[i] - rmax);
                                if (distance1 > rmax - r_min[i])
                                    distance1 = rmax - r_min[i];
                                if (distance2 > rmax - r_min[i])
                                    distance2 = rmax - r_min[i];
                                if (distance1 < distance2)
                                    sLengthMin[i] = (distance1 > 0) ? distance1 : 0;
                                else if (distance2 > 0) {
                                    if (distance2 < sLengthMin[i])
                                        sLengthMin[i] = distance2;
                                } else
                                    sLengthMin[i] = 0;
                            }
                        } else // sOriginMin[i] > r_min[i]
                            if (sOriginMax[i] > r_max[i]) {
                                int distance2 = sLengthMin[i] - (sOriginMax[i] - (r.origin[i].max() + r.length[i].min()));
                                if (distance2 > 0) {
                                    if (distance2 < sLengthMin[i])
                                        sLengthMin[i] = distance2;
                                } else
                                    sLengthMin[i] = 0;
                            }
                        partialCommonArea = partialCommonArea * sLengthMin[i];
                    }
                    commonArea += partialCommonArea;
                }
                if (!exceptionListPresent)
                    if (commonArea + r.minArea() > (r_max[0] - r_min[0]) * (r_max[1] - r_min[1]))
                        throw Store.failException;

            }
        }

        if (checkArea) { // check whether there is
            // enough room for all rectangles
            area += r.minArea();
            long availArea = 1;
            long rectNumber = 1;
            for (int i = 0; i < startMin.length; i++) {
                IntDomain rOriginIdom = r.origin[i].dom();
                IntDomain rLengthIdom = r.length[i].dom();
                int rOriginIMin = rOriginIdom.min(), rOriginIMax = rOriginIdom.max(), rLengthIMin = rLengthIdom.min();
                if (rOriginIMin < startMin[i])
                    startMin[i] = rOriginIMin;
                if (rOriginIMax + rLengthIMin > stopMax[i])
                    stopMax[i] = rOriginIMax + rLengthIMin;
            }
            boolean checkRectNumber = true;
            for (int i = 0; i < startMin.length; i++) {
                availArea = availArea * (stopMax[i] - startMin[i]);
                if (minLength[i] != 0)
                    rectNumber *= ((stopMax[i] - startMin[i]) / minLength[i]);
                else
                    checkRectNumber = false;
            }

            if (!exceptionListPresent)
                if (availArea < area)
                    throw Store.failException;
                else
                    // check whether there is enough room for
                    // all minimal rectangles
                    if (checkRectNumber && rectNumber < (totalNumberOfRectangles + 1))
                        throw Store.failException;
        }

        return contains;
    }

    @Override public boolean satisfied() {
        boolean sat = true;

        Rectangle recti, rectj;
        int i = 0;
        while (sat && i < rectangles.length) {
            recti = rectangles[i];
            int j = 0;
            Rectangle[] toEvaluate = ((Diff2VarValue) EvalRects[i].value()).Rects;
            while (sat && j < toEvaluate.length) {
                rectj = toEvaluate[j];
                sat = sat && !recti.domOverlap(rectj);
                j++;
            }
            i++;
        }
        return sat;
    }

    @Override public String toString() {

        StringBuffer result = new StringBuffer(id());

        result.append(" : diff2( ");

        for (int i = 0; i < rectangles.length - 1; i++) {
            result.append(rectangles[i]);
            result.append(", ");

        }
        result.append(rectangles[rectangles.length - 1]);
        result.append(")");

        return result.toString();

    }

}
