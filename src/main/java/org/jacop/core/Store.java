/*
 * Store.java
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

package org.jacop.core;

import org.jacop.api.RemoveLevelLate;
import org.jacop.api.Replaceable;
import org.jacop.api.Stateful;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.DecomposedConstraint;
import org.jacop.constraints.Reified;
import org.jacop.constraints.replace.ReifiedIfThen;
import org.jacop.util.SimpleHashSet;
import org.jacop.util.SparseSet;
import java.util.*;

/**
 * It is an abstract class to describe all necessary functions of any store.
 *
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski
 * @version 4.10
 */

public class Store {

    /**
     * It stores standard fail exception used when empty domain encountered.
     */

    public static final FailException failException = new FailException();

    /**
     * It specifies if some debugging information is printed.
     */
    public static final boolean debug = true;

    /**
     * It stores constraints scheduled for reevaluation. It does not register
     * constraints which are already scheduled for reevaluation.
     */

    public SimpleHashSet<Constraint>[] changed;

    /**
     * It stores boolean variables as soon as they change (become grounded or
     * number of constraints being attached is changed). Later each level
     * remembers the part of the array which contains variables changed at this
     * level (efficient backtracking).
     */

    public BooleanVar[] changeHistory4BooleanVariables;

    /**
     * More advanced constraints may require to be informed of a backtrack to be
     * able to recover the older versions of the data structures. For example,
     * the constraints can clear their queue of changed variables if a
     * backtracks has occurred. It holds the list of constraints which want to be informed
     * about level being removed before it has actually began.
     */
    public Set<Stateful> removeLevelListeners = new HashSet<>(10);

    /**
     * More advanced constraints may require to be informed of a backtrack to be
     * able to recover the older versions of the data structures. For example,
     * the constraints can clear their queue of changed variables if a
     * backtracks has occurred. It holds the list of constraints which want to be informed
     * about level being removed after it has been removed.
     */
    public Set<RemoveLevelLate> removeLevelLateListeners = new HashSet<>(10);

    /**
     * It contains all auxilary variables created by decomposable constraints. They
     * have to be grounded by search for a solution to be valid.
     */

    public List<Var> auxilaryVariables = new ArrayList<>();

    /**
     * It stores constraint which is currently re-evaluated.
     */
    public Constraint currentConstraint = null;


    /**
     * It stores constraint that has recently failed during store.consistency() execution.
     */
    public Constraint recentlyFailedConstraint = null;

    /**
     * It stores current queue, which is being evaluated.
     */

    public int currentQueue = 0;

    /**
     * It specifies long description of the store.
     */
    public String description = null;

    /**
     * Id string of the store.
     */
    public String id = "Store";

    /**
     * It specifies the time point in the search. Every time this variable is
     * increased a new layer of changes to the variables are recorded. This is
     * the most important variable. It is assumed that initially this value is
     * equal to zero. Use setLevel function if you want to play with it.
     */
    public int level = 0;

    /**
     * A mutable variable is a special variable which can change value during
     * the search. In the event of backtracks the old value must be restored,
     * therefore the store keeps information about all mutable variables.
     */

    protected List<MutableVar> mutableVariables = new ArrayList<>(100);

    /**
     * This variable specifies if there was a new propagation. Any change to any
     * variable will setup this variable to true. Usefull variable to discover
     * the idempodence of the consistency propagator.
     */
    public boolean propagationHasOccurred = false;

    /**
     * It stores the number of constraints which were imposed to the store.
     */

    protected int numberOfConstraints = 0;

    /**
     * It specifies the current pointer to put next changed boolean variable. It
     * has to be maintained manually (within removeLevel function).
     */

    public TimeStamp<Integer> pointer4GroundedBooleanVariables = null;

    /**
     * It stores number of queues used in this store. It has to be at least 1.
     * No constraint can be imposed with queue index greater or equal this
     * number.
     */

    // TODO, create setQueue function so all data structures are properly updated
    // upon changing the number of queues.
    public int queueNo = 5;

    /**
     * Some constraints maintain complex data structure based on function
     * recentDomainPruning of a variable, this function for proper functioning
     * requires to raise store level after imposition and before any changes to
     * variables of this constraint occur. This flag is set by constraints at
     * imposition stage.
     */

    public boolean raiseLevelBeforeConsistency = false;

    /**
     * It specifies if the weight of variables which are in the scope of the failure
     * constraint should be increased.
     */

    public boolean variableWeightManagement = false;

    /**
     * It switches on/off debuging of remove level facilities.
     */

    final boolean removeDebug = false;

    /**
     * Number of variables stored within a store.
     */

    protected int size = 0;


    /**
     * Number of calls to consistency methods of constraints.
     */
    public long numberConsistencyCalls = 0;

    /**
     * It indicates that consistency function should immediately return fail if last inconsistency was not followed
     * yet by removeLevel function.
     */
    public boolean strict = true;

    /**
     * This flag is set to true when consistency function of the store encounters failure.
     */
    public boolean isLastConsistencyFailure = false;


    /**
     * TimeStamp variable is a simpler version of a mutable variable. It is
     * basically a stack. During search items are push onto the stack. If the
     * search backtracks then the old values can be simply restored. Simple and
     * efficient way for getting mutable variable functionality for simple data
     * types.
     */
    protected List<Stateful> timeStamps = new ArrayList<>(100);

    /**
     * This keeps information about watched constraints by given variable.
     * Watched constraints are active all the time. Use this with care and do
     * not be surprised if some constraints stay longer than you expect. It can
     * be directly manipulated in any way (including setting to null if no
     * watched constraints are being in the queue system).
     */

    public Map<Var, Set<Constraint>> watchedConstraints;

    /**
     * It stores all the active replacements of constraints that are being applied
     * upon constraint imposition. It makes it possible to replace constraints into
     * other constraints. It can be very useful for efficiency or testing purposes. 
     */
    private Map<Class<? extends Constraint>, Set<Replaceable>> replacements = new HashMap<>();

    /**
     * Variables for accumulated failure count (AFC) for constraints.
     * constraintAFCManagement- opens AFC menagement
     * decay- decay factor
     * allConstraints- all constraints in the store
     */
    boolean constraintAFCManagement = false;
    Set<Constraint>  allConstraints;

    double decay = 0.99d;
    
    /**
     * Variables for pruning count (variable activity) for constraints.
     * variableActivityManagement- opens activity menagement
     * variablePrunnedConstraints- all constraints in the store
     */
    boolean variableActivityManagement = false;
    Set<Var>  variablesPrunned;
    
    /**
     * It specifies the seed for random number generators.
     */
    static long seed;
    static boolean seedPresent = false;

    /**
     * Variable given as a parameter no longer watches constraint given as
     * parameter. This function will be called when watch is being moved from
     * one variable to another.
     *
     * @param v variable at which constraint is no longer watching.
     * @param c constraint which is no longer watched by given variable.
     */

    public void deregisterWatchedLiteralConstraint(Var v, Constraint c) {

        watchedConstraints.get(v).remove(c);

    }

    /**
     * Watched constraint given as parameter is being removed, no variable will
     * be watching it.
     *
     * @param c constraint for which all watches are removed.
     */

    public void deregisterWatchedLiteralConstraint(Constraint c) {

        for (Var v : c.arguments()) {
            Set<Constraint> forVariable = watchedConstraints.get(v);
            if (forVariable != null)
                forVariable.remove(c);
        }
    }

    /**
     * It returns number of watches which are used to watch constraints.
     *
     * @return returns the number of watches attached to variables.
     */
    public int countWatches() {

        if (watchedConstraints == null)
            return 0;

        int count = 0;

        for (Set<Constraint> c : watchedConstraints.values())
            count += c.size();

        return count;

    }

    /**
     * It register variable to watch given constraint. This function is called
     * either by impose function of a constraint or by consistency function of a
     * constraint when watch is being moved.
     *
     * @param v variable which is used to watch the constraint.
     * @param c the constraint being used.
     */

    public void registerWatchedLiteralConstraint(Var v, Constraint c) {

        Set<Constraint> forVariable = watchedConstraints.get(v);

        if (forVariable != null)
            forVariable.add(c);
        else {
            forVariable = new HashSet<>();
            forVariable.add(c);
            watchedConstraints.put(v, forVariable);
        }

    }

    /**
     * It removes all watches to constraints, therefore
     * constraints are no longer watched, no longer part of the model.
     */
    public void clearWatchedConstraint() {

        watchedConstraints.clear();

    }

    /**
     * The prefix of any variable which was noname.
     */
    protected String variableIdPrefix = "_";

    /**
     * It stores integer variables created within a store.
     */

    public Var[] vars;

    /**
     * It allows to manage information about changed variables in
     * efficient/specialized/tailored manner.
     */
    public BacktrackableManager trailManager;

    /**
     * It specifies the default constructor of the store.
     */

    public Store() {

        this(100);
        
    }

    /**
     * It specifies the constructor of the store, which allows to decide what is
     * the initial size of the Variable list.
     *
     * @param size specifies the initial number of variables.
     */

    @SuppressWarnings("unchecked")
    public Store(int size) {

        vars = new Var[size];

        changed = new SimpleHashSet[queueNo];

        for (int i = 0; i < queueNo; i++)
            changed[i] = new SimpleHashSet<>(100);

        trailManager = new IntervalBasedBacktrackableManager(vars, this.size, 10, Math.max(size / 10, 4));

    }

    /**
     * This function schedules given constraint for re-evaluation. This function
     * will most probably be rarely used as constraints require reevaluation
     * only when a variable changes.
     *
     * @param c constraint which needs reevaluation.
     */

    public void addChanged(Constraint c) {

        propagationHasOccurred = true;

        if (c.queueIndex < currentQueue)
            currentQueue = c.queueIndex;

        changed[c.queueIndex].add(c);

    }


    /**
     * This function schedules all attached (not yet satisfied constraints) for
     * given variable for re-evaluation. This function must add all attached
     * constraints for reevaluation but it will do it any order which suits it.
     *
     * @param var          variable for which some pruning event has occurred.
     * @param pruningEvent specifies the type of the pruning event.
     * @param info         it specifies detailed information about the change of the variable domain.
     *                     the inputs of the currentConstraint in the manner that would validate another execution.
     */

    public void addChanged(Var var, int pruningEvent, int info) {

        propagationHasOccurred = true;

        if (variableActivityManagement)
            variablesPrunned.add(var);

        // It records V as being changed so backtracking later on can be invoked for this variable.
        recordChange(var);

        Domain vDom = var.dom();

        Constraint[] addedConstraints = null;
        Constraint c;

        // FIXME, BUG. it should not assume that events are from IntDomain.
        // Who and when should add constraints to the queue?
        // TEST, now.

        for (int j : vDom.getEventsInclusion(pruningEvent)) {

            addedConstraints = vDom.modelConstraints[j];

            for (int i = vDom.modelConstraintsToEvaluate[j] - 1; i >= 0; i--) {

                c = addedConstraints[i];

                c.queueVariable(level, var);

                if (currentConstraint != c) {
                    addChanged(c);
                }

            }

        }

        List<Constraint> constr = vDom.searchConstraints;

        for (int i = vDom.searchConstraintsToEvaluate - 1; i >= 0; i--) {

            c = constr.get(i);

            c.queueVariable(level, var);

            if (currentConstraint != c) {

                addChanged(c);

            }
        }

        // Watched constraints
        if (watchedConstraints != null && pruningEvent == IntDomain.GROUND) {

            Set<Constraint> list = watchedConstraints.get(var);

            if (list != null)
                for (Constraint con : list) {
                    con.queueVariable(level, var);

                    if (currentConstraint != con) {
                        addChanged(con);
                    }
                }
        }


    }

    /**
     * It clears the queue of constraints which need to be reevaluated usefull
     * if different scheme propagation scheme needs to be implemented.
     */

    public void clearChanged() {

        while (currentQueue < queueNo)
            changed[currentQueue++].clear();

    }

    /**
     * This function computes the consistency function. It evaluates all
     * constraints which are in the changed queue.
     *
     * @return returns true if all constraints which were in changed queue are consistent, false otherwise.
     */

    public boolean consistency() {

        if (strict && isLastConsistencyFailure)
            return false;

        if (raiseLevelBeforeConsistency) {
            raiseLevelBeforeConsistency = false;
            setLevel(level + 1);
        }

        if (this.sparseSetSize > 0 && this.sparseSet == null)
            sparseSet = new SparseSet(sparseSetSize);

        try {

            while (currentQueue < queueNo) {
                // Selects changed constraints from changed queue
                // and evaluates them
                if (currentQueue < queueNo)
                    while (!changed[currentQueue].isEmpty()) {

                        currentConstraint = getFirstChanged();

                        numberConsistencyCalls++;

                        currentConstraint.consistency(this);

                        if (variableActivityManagement) {
                            updateActivities(currentConstraint);
                            variablesPrunned.clear();
                        }
                    }

                currentQueue++;
            }

        } catch (FailException f) {

            if (currentConstraint != null) {

                currentConstraint.cleanAfterFailure();

                if (variableWeightManagement)
                    currentConstraint.increaseWeight();

                if (constraintAFCManagement)
                    currentConstraint.updateAFC(allConstraints, decay);

                if (variableActivityManagement) {
                    updateActivities(currentConstraint);
                    variablesPrunned.clear();
                }
            }

            recentlyFailedConstraint = currentConstraint;
            currentConstraint = null;

            isLastConsistencyFailure = true;
            return false;

        }

        currentConstraint = null;
        return true;

    }

    /**
     * This function is called when a counter of constraints should be
     * increased. It is most probable that this function will called from the
     * impose function of the constraint.
     */
    public void countConstraint() {
        numberOfConstraints++;
    }

    /**
     * This function is called when a counter of constraints should be increased
     * by given value. If for some reason some constraints should be counted as
     * multiple ones than this function could be called.
     *
     * @param n integer by which the counter of constraints should be increased.
     */
    public void countConstraint(int n) {
        numberOfConstraints += n;
    }

    /**
     * It may be used for faster retrieval of variables given their id. However,
     * by default this variable is not created to reduce memory consumption. If
     * it exists then it will be used by functions looking for a variable given the name.
     */

    public Map<String, Var> variablesHashMap = new HashMap<String, Var>();

    /**
     * This function looks for a variable with given id. It will first
     * check the existence of a hashmap variablesHashMap to get the
     * variable from the hashmap in constant time. Only if the variable
     * was not found or hashmap object was not created a linear algorithm
     * scanning through the whole list of variables will be employed.
     *
     * @param id unique identifier of the variable.
     * @return reference to a variable with the given id.
     */

    public Var findVariable(String id) {

        if (variablesHashMap != null) {

            Var key = variablesHashMap.get(id);

            if (key != null)
                return key;

        }

        for (Var v : vars)
            if (v != null && id.equals(v.id()))
                return v;

        return null;

    }

    /**
     * It loads CSP from XML file, which uses an extended version of XCSP 2.0.
     * @param path path pointing at a file
     * @param filename
     */

    /**
     * This function returns the constraint which is currently reevaluated. It
     * is an easy way to discover which constraint caused a failure right after
     * the inconsistency is signaled.
     *
     * @return constraint for which consistency method is being executed.
     */

    public Constraint getCurrentConstraint() {
        return currentConstraint;
    }

    /**
     * This function returns the long description of the store.
     *
     * @return store description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This function returns the constraint scheduled for re-evaluation. It
     * returns constraints based on criteria first-in-first out. It is simple,
     * easy, fair, and efficient way of getting constraints for reevaluation.
     * The constraint is _removed_ from the queue, since it is assumed that they
     * are reevaluated right away.
     *
     * @return first constraint which is being marked as the one which needs to be checked for consistency.
     */

    public Constraint getFirstChanged() {

        return changed[currentQueue].removeFirst();

    }

    /**
     * This function returns the id of the store.
     *
     * @return id of store.
     */
    public String getName() {
        return id;
    }

    /**
     * This function returns the prefix of the automatically generated names for
     * noname variables.
     *
     * @return he prefix of the automatically generated names for noname variables.
     */
    public String getVariableIdPrefix() {
        return variableIdPrefix;
    }

    /**
     * This function imposes a constraint to a store. The constraint is
     * scheduled for evaluation for the next store consistency call. Therefore,
     * the constraint is added to queue of changed constraints.
     *
     * @param c constraint to be imposed.
     */

    @SuppressWarnings("unchecked")
    public void impose(Constraint c) {

        Optional.ofNullable(replacements.get(c.getClass())).ifPresent(l -> l.forEach(r -> {
            if (r.isReplaceable(c)) {
                r.replace(c).imposeDecomposition(this);
                return;
            }
        }));
        c.impose(this);

    }

    /**
     * This function imposes a constraint to a store. The constraint is
     * scheduled for evaluation for the next store consistency call. Therefore,
     * the constraint is added to queue of changed constraints.
     *
     * @param c          constraint to be added to specified queue.
     * @param queueIndex specifies index of the queue for a constraint.
     */

    public void impose(Constraint c, int queueIndex) {

        assert (queueIndex < queueNo) : "Constraint queue number larger than permitted by store.";

        c.impose(this, queueIndex);
    }

    /**
     * In some special cases it may be beneficial to compute consistency of
     * constraint store immediately after the constraint is imposed. This
     * function will impose a constraint and call the consistency function of
     * the store immediately.
     *
     * @param c constraint to be imposed.
     * @throws FailException failure exception.
     */

    public void imposeWithConsistency(Constraint c) throws FailException {

        c.impose(this);

        if (!consistency()) {
            throw Store.failException;
        }

    }

    /**
     * In some special cases it may be beneficial to compute consistency of
     * constraint store immediately after the constraint is imposed. This
     * function will impose a constraint and call the consistency function of
     * the store immediately.
     *
     * @param c constraint to be imposed.
     * @param queueIndex constraint priority for evaluation.
     * @throws FailException failure exception.
     */

    public void imposeWithConsistency(Constraint c, int queueIndex) throws FailException {

        assert (queueIndex < queueNo) : "Constraint queue number larger than permitted by store.";

        c.impose(this, queueIndex);

        if (!consistency()) {
            throw Store.failException;
        }

    }

    /**
     * This function imposes a decomposable constraint to a store. The decomposition is
     * scheduled for evaluation for the next store consistency call. Therefore,
     * the constraints are added to queue of changed constraints.
     *
     * @param c constraint to be imposed.
     */

    public <T extends Constraint> void imposeDecomposition(DecomposedConstraint<T> c) {

        c.imposeDecomposition(this);

    }

    /**
     * This function imposes a constraint decomposition to a store. The decomposition
     * constraints are scheduled for evaluation for the next store consistency call. Therefore,
     * the constraints are added to queue of changed constraints.
     *
     * @param c          constraint to be added to specified queue.
     * @param queueIndex specifies index of the queue for a constraint.
     */

    public <T extends Constraint> void imposeDecomposition(DecomposedConstraint<T> c, int queueIndex) {

        assert (queueIndex < queueNo) : "Constraint queue number larger than permitted by store.";

        c.imposeDecomposition(this, queueIndex);
    }

    /**
     * In some special cases it may be beneficial to compute consistency of
     * constraint store immediately after the decomposed constraint is imposed. This
     * function will impose constraint decomposition and call the consistency function of
     * the store immediately.
     *
     * @param c decomposed constraint to be imposed.
     */

    public <T extends Constraint> void imposeDecompositionWithConsistency(DecomposedConstraint<T> c) {

        c.imposeDecomposition(this);

        if (!consistency()) {
            throw Store.failException;
        }


    }



    /**
     * This function checks if all variables within a store are grounded. It is
     * advised to make sure that after search all variables are grounded.
     *
     * @return true if all variables are singletons, false otherwise.
     */
    public boolean isGround() {
        for (Var v : vars)
            if (!v.singleton())
                return false;
        return true;
    }

    /**
     * This function returns the number of constraints.
     *
     * @return number of constraints.
     */

    public int numberConstraints() {
        return numberOfConstraints;
    }

    /**
     * This function prints the information of the store to standard output
     * stream.
     */

    public void print() {
        System.out.println(toString());
    }

    /**
     * Any constraint may have their own mutable variables which can be register
     * at store and then store will be responsible for calling appropriate
     * functions from MutableVar interface to keep the variables consistent with
     * the search.
     *
     * @param value MutableVariable to be added and maintained by a store.
     * @return the position of MutableVariable at which it is being stored.
     */

    public int putMutableVar(MutableVar value) {
        mutableVariables.add(value);
        return mutableVariables.size() - 1;
    }

    /**
     * Any entity (for example constraints) may have their own mutable variables
     * (timestamps) which can be register at store and then store will be
     * responsible for calling appropriate functions from TimeStamp class to
     * keep the variables consistent with the search.
     *
     * @param value timestamp to be added and maintained by a store.
     * @return the position of timestamp at which it is being stored.
     */

    public int putMutableVar(Stateful value) {
        timeStamps.add(value);
        return timeStamps.size() - 1;
    }

    /**
     * This function is used to register a variable within a store. It will be
     * most probably called from variable constructor. It returns the current
     * position of fdv in a store local data structure.
     *
     * @param var variable to be registered.
     * @return position of the variable at which it is being stored.
     */
    public int putVariable(Var var) {

        Var previousVar = variablesHashMap.put(var.id(), var);

        assert (previousVar == null) : "Two variables have the same id " + previousVar + " " + var;

        if (var.index != -1) {
            if (vars[var.index] == var) {
                throw new IllegalArgumentException("\nSetting Variable: Variable already exists: " + var.id());
            }
        }

        // boolean variables are not trailed the same fashion as int variables.
        // return default index specifying that this variable is not stored within vars array.
        if (var instanceof BooleanVar)
            return -1;

        if (size < vars.length) {

            vars[size] = var;
            size++;

        } else {

            Var[] oldVars = vars;
            vars = new Var[oldVars.length * 2];

            System.arraycopy(oldVars, 0, vars, 0, size);

            vars[size] = var;
            size++;

            trailManager.update(vars, size);

        }

        trailManager.setSize(size);

        return size - 1;
    }

    /**
     * Any boolean variable which is changed must be recorded by store, so it
     * can be restored to the previous state if backtracking is performed.
     *
     * @param recordedVariable boolean variable which has changed.
     */

    public void recordBooleanChange(BooleanVar recordedVariable) {

        int position = pointer4GroundedBooleanVariables.value();

        if (position >= changeHistory4BooleanVariables.length) {

            BooleanVar[] temp = changeHistory4BooleanVariables;
            changeHistory4BooleanVariables = new BooleanVar[changeHistory4BooleanVariables.length * 2];
            System.arraycopy(temp, 0, changeHistory4BooleanVariables, 0, position);
        }

        changeHistory4BooleanVariables[position] = recordedVariable;

        pointer4GroundedBooleanVariables.update(position + 1);

    }



    /**
     * Any change of finite domain variable must also be recorded, so intervals
     * denoting changed variables can be updated.
     *
     * @param recordedVariable variable which has changed.
     */

    public void recordChange(Var recordedVariable) {

        // Boolean variables or other variables with index -1 are
        // stored each time they change in the special 1D array.
        if (recordedVariable.index == -1) {
            recordBooleanChange((BooleanVar) recordedVariable);
            return;
        }

        assert (trailManager.getLevel()
            == level) : "An attempt to remeber a changed item at the level which have not been set properly by calling function setLevel()";

        //      assert (!trailManager.trailContainsAllChanges
        //                      || trailManager.levelInfo.get(trailManager.levelInfo.size() - 1) == level) :
        //                             "An error. Trail should be containing all changes but it is not available";

        trailManager.addChanged(recordedVariable.index);

    }

    /**
     * It makes it possible to register replacement for a particular constraint
     * type.
     *
     * @param replacement that is being registered.
     * @return true if replacement has been added and was not already registered, false otherwise.
     *
     */
    public boolean registerReplacement(Replaceable<? extends Constraint> replacement) {

        if (!replacements.containsKey(replacement.forClass())) {
            replacements.put(replacement.forClass(), new HashSet<>());
        }

        Set<Replaceable> current = replacements.get(replacement.forClass());

        return current.add(replacement);
        
    }

    /**
     * It makes it possible to deregister the replacement. The constraint that have been
     * already replaced remained replaced.
     *
     * @param replacement that is being deregister from within the constraint store.
     * @return true if replacement was present and was deregistered, false otherwise.
     */
    public boolean deregisterReplacement(Replaceable<? extends Constraint> replacement) {

        Optional<Set<Replaceable>> forClass =
            Optional.ofNullable(replacements.get(replacement.forClass()));

        return forClass.map(replaceables -> replaceables.remove(replacement)).orElse(false);

    }
    /**
     * Any constraint in general may need information what variables have changed
     * since the last time a consistency was called. This function is called
     * just *before* removeLevel method is executed for variables, mutable variables,
     * and timestamps.
     *
     * @param stateful constraint which is interested in listening to remove level events.
     * @return true if constraint stateful was watching remove level events.
     */

    public boolean registerRemoveLevelListener(Stateful stateful) {

        if (!removeLevelListeners.contains(stateful)) {
            return removeLevelListeners.add(stateful);
        }
        return false;
    }


    /**
     * Any constraint in general may need information what variables have changed
     * since the last time a consistency was called. This function is called
     * just *after* removeLevel method is executed for variables, mutable variables,
     * and timestamps.
     *
     * @param c constraint which is no longer interested in listening to remove level events.
     * @return true if constraint c was watching remove level events.
     */

    public boolean registerRemoveLevelLateListener(RemoveLevelLate c) {

        if (!removeLevelLateListeners.contains(c)) {
            return removeLevelLateListeners.add(c);
        }
        return false;
    }

    /**
     * This important function removes all changes which has been recorded to
     * any variable at given level. Before backtracking to earlier level all
     * levels after earlier level must be removed. The removal order must be
     * reversed to the creation order.
     *
     * @param rLevel Store level to be removed.
     */

    public void removeLevel(int rLevel) {

        // Remove level is called so we clear flag isLastConsistencyFailure.
        isLastConsistencyFailure = false;

        while (currentQueue < queueNo) {
            changed[currentQueue++].clear();
        }

        // It has to inform listeners first, as they may use values of
        // mutables variables, just before they get deleted.

        for (Stateful statefulConstraint : removeLevelListeners)
            statefulConstraint.removeLevel(rLevel);

        // It needs to be before as there is a timestamp for number of boolean variables.
        for (Stateful var : timeStamps)
            var.removeLevel(rLevel);

        // Boolean Variables.

        if (changeHistory4BooleanVariables != null) {

            int previousPosition = pointer4GroundedBooleanVariables.value();

            pointer4GroundedBooleanVariables.removeLevel(rLevel);

            int currentPosition = pointer4GroundedBooleanVariables.value();

            for (int i = currentPosition; i < previousPosition; i++) {

                Domain dom = changeHistory4BooleanVariables[i].domain;

                // Condition not true, when first constraint imposed
                // on not grounded variable is satisfied then a
                // variable becomes grounded on the same
                // level. Without this condition a null point
                // exception will occur.
                if (dom.stamp == rLevel) {

                    changeHistory4BooleanVariables[i].domain.removeLevel(rLevel, changeHistory4BooleanVariables[i]);

                }
            }
        }

        // TODO, added functionality.
        trailManager.removeLevel(rLevel);

        for (int i = mutableVariables.size() - 1; i >= 0; i--)
            mutableVariables.get(i).removeLevel(rLevel);

        for (RemoveLevelLate c : removeLevelLateListeners)
            c.removeLevelLate(rLevel);

        assert checkInvariants() == null : checkInvariants();

    }

    /**
     * This function sets the long description of the store.
     *
     * @param description description of the store
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This function sets the id of the store. This id is used when saving to
     * XML file.
     *
     * @param id store id.
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * This function allows to proceed with the search, create new layer at
     * which new values for variables will be recorded. This function is also
     * used during backtracking, after removing current level the store can be
     * set to the previous level.
     *
     * @param levelSetTo level number to which store is changing to.
     */

    public void setLevel(int levelSetTo) {

        // TODO, functionality added.
        trailManager.setLevel(levelSetTo);

        if (level == levelSetTo)
            return;

        if (removeDebug) {

            if (level > levelSetTo && level > 0) {

                for (int i = 0; i < size; i++) {

                    if (vars[i].level() >= level) {
                        assert trailManager.isRecognizedAsChanged(vars[i].index) :
                            "Variable position " + i + " not properly recorded to have changed ";
                    }
                }
            }
        }

        if (removeDebug)
            System.out.println("Store level changes from " + level + " to " + levelSetTo);

        level = levelSetTo;
    }

    /**
     * This function sets the prefix of the automatically generated names for
     * noname variables.
     *
     * @param idPrefix prefix of all variables with automatically generated names.
     */
    public void setVariableIdPrefix(String idPrefix) {
        variableIdPrefix = idPrefix;
    }

    /**
     * It returns number of variables in a store.
     *
     * @return number of variables in a store.
     */

    public int size() {
        return size;
    }

    /**
     * It throws an exception after printing trace information if tracing is
     * switched on.
     *
     * @param x variable causing the failure exception.
     * @throws FailException is always thrown.
     */

    public void throwFailException(Var x) {

        throw failException;

    }

    /**
     * This function returns a string a representation of the store. Whatever
     * seems important may be included here.
     */

    @Override public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("\n*** Store\n");

        // first BooleanVar
        for (Var v : variablesHashMap.values()) {
            if (v instanceof BooleanVar)
                result.append(v + "\n");
        }

        // all other variables
        for (int i = 0; i < size; i++)
            result.append(vars[i] + "\n");

        int i = 0;
        for (MutableVar var : mutableVariables) {
            result.append("MutableVar[").append((int) i++).append("] ").append("(").append(var.value().stamp()).append(")");

            result.append(var.value()).append("\n");
        }

        for (Constraint c : getConstraints())
            result.append("*** Constraint:\n").append(c + "\n");

        result.append("\n*** Constraints for evaluation:\n{").append(toStringChangedEl()).append(" }");

        return result.toString();

    }

    public Set<Constraint> getConstraints() {

        Set<Constraint> constraints = new HashSet<Constraint>();

        for (Var v : variablesHashMap.values()) {
            Domain d = v.dom();
            List<Constraint> c = d.constraints();
            constraints.addAll(c);
        }

        return constraints;
    }

    public void setAllConstraints() {
        allConstraints = getConstraints();
    }
    

    public void setDecay(double d) {
        decay = d;
    }

    public double getDecay() {
        return decay;
    }

    public void afcManagement(boolean m) {
        constraintAFCManagement = m;
    }
    
    public void activityManagement(boolean m) {
        variableActivityManagement = m;
        variablesPrunned = new HashSet<Var>();
    }

    void updateActivities(Constraint constraint) {
        for (Var v : variablesPrunned)
            v.updateActivity();

        if (decay < 1.0d)
            for (Var v : constraint.arguments())
                if (!variablesPrunned.contains(v))
                    v.applyDecay();
    }
    
    /**
     * This function returns a string representation of the constraints pending
     * for re-evaluation.
     *
     * @return string description of changed constraints.
     */

    public String toStringChangedEl() {

        StringBuffer c = new StringBuffer();

        for (int i = 0; i < queueNo; i++)
            c.append(changed[i].toString() + "\n");

        return c.toString();

    }

    /**
     * It is used by Extensional MDD constraints. It is to represent G_yes.
     */
    public SparseSet sparseSet;

    /**
     * It is used by Extensional MDD constraints. It is to represent the size of G_yes.
     */
    public int sparseSetSize = 0;


    /**
     * It checks invariants to see if the execution went smoothly.
     *
     * @return description of the violated invariant, null otherwise.
     */
    public String checkInvariants() {

        for (int i = 0; i < size; i++)
            if (vars[i].level() > level)
                return "Removal of old values was done properly " + vars[i];

        return null;
    }

    public String toStringOrderedVars() {

        StringBuffer result = new StringBuffer();

        result.append("[");

        // first BooleanVar
        for (String key : new TreeSet<>(variablesHashMap.keySet())) {
            Var v = variablesHashMap.get(key);
            if (v instanceof BooleanVar)
                result.append(v + ",");
        }

        // all other variables
        TreeSet<Var> orderedVariables = new TreeSet<>(Comparator.comparing(Var::id));
        for (int i = 0; i < size; i++)
            orderedVariables.add(vars[i]);
        for (Var var : orderedVariables)
            result.append(var + ",");

        int i = 0;
        for (MutableVar var : mutableVariables) {
            result.append("MutableVar[").append((int) i++).append("] ").append("(").append(var.value().stamp()).append(")");
            result.append(var.value()).append(",");
        }

        result.replace(result.length() - 1, result.length(), "]");
        return result.toString();

    }

    public static void setSeed(long s) {
        seed = s;
        seedPresent = true;
    }

    public static long getSeed() {
        if (seedPresent)
            return seed;

        throw new IllegalArgumentException("Not defined seed for random generator");
    }

    public static boolean seedPresent() {
        return seedPresent;
    }

    public static void resetSeed() {
        seedPresent = false;
    }
}
