% 
% Ramsey partition problem in MiniZinc.
% 
% http://www.mathematik.uni-bielefeld.de/~sillke/PUZZLES/partion3-ramsey
% """
% Partition the integers 1 to 23 into three sets, such that for no set are
% there three different numbers with two adding to the third.
% ...
% There are three solutions...
% *****************
%   1   2   4   8   11   16   22
%   3   5   6   7   19   21   23
%   9   10   12   13   14   15   17   18   20
% *****************
%   1   2   4   8   11   17   22
%   3   5   6   7   19   21   23
%   9   10   12   13   14   15   16   18   20
% *****************
%   1   2   4   8   11   22
%   3   5   6   7   19   21   23
%   9   10   12   13   14   15   16   17   18   20
% *****************
% ...
% """


% The following solutions are generated by fz with "smallest", "indomain", "complete":
% 
% [{1,2,4,8,11,16,22}#(7), {3,5..7,19,21,23}#(7), {9,10,12..15,17,18,20}#(9)]
% [{1,2,4,8,11,17,22}#(7), {3,5..7,19,21,23}#(7), {9,10,12..16,18,20}#(9)]
% [{1,2,4,8,11,22}#(6), {3,5..7,19,21,23}#(7), {9,10,12..18,20}#(10)]
% [{1,2,4,8,11,16,22}#(7), {9,10,12..15,17,18,20}#(9), {3,5..7,19,21,23}#(7)]
% [{1,2,4,8,11,17,22}#(7), {9,10,12..16,18,20}#(9), {3,5..7,19,21,23}#(7)]
% [{1,2,4,8,11,22}#(6), {9,10,12..18,20}#(10), {3,5..7,19,21,23}#(7)]
% [{3,5..7,19,21,23}#(7), {1,2,4,8,11,16,22}#(7), {9,10,12..15,17,18,20}#(9)]
% [{3,5..7,19,21,23}#(7), {1,2,4,8,11,17,22}#(7), {9,10,12..16,18,20}#(9)]
% [{3,5..7,19,21,23}#(7), {1,2,4,8,11,22}#(6), {9,10,12..18,20}#(10)]
% [{9,10,12..18,20}#(10), {1,2,4,8,11,22}#(6), {3,5..7,19,21,23}#(7)]
% [{9,10,12..16,18,20}#(9), {1,2,4,8,11,17,22}#(7), {3,5..7,19,21,23}#(7)]
% [{9,10,12..15,17,18,20}#(9), {1,2,4,8,11,16,22}#(7), {3,5..7,19,21,23}#(7)]
% [{3,5..7,19,21,23}#(7), {9,10,12..18,20}#(10), {1,2,4,8,11,22}#(6)]
% [{3,5..7,19,21,23}#(7), {9,10,12..16,18,20}#(9), {1,2,4,8,11,17,22}#(7)]
% [{3,5..7,19,21,23}#(7), {9,10,12..15,17,18,20}#(9), {1,2,4,8,11,16,22}#(7)]
% [{9,10,12..18,20}#(10), {3,5..7,19,21,23}#(7), {1,2,4,8,11,22}#(6)]
% [{9,10,12..16,18,20}#(9), {3,5..7,19,21,23}#(7), {1,2,4,8,11,17,22}#(7)]
% [{9,10,12..15,17,18,20}#(9), {3,5..7,19,21,23}#(7), {1,2,4,8,11,16,22}#(7)]
% 

%  
% Model created by Hakan Kjellerstrand, hakank@bonetmail.com
% See also my MiniZinc page: http://www.hakank.org/minizinc
%

include "globals.mzn"; 

int: n = 23;
array[1..3] of var set of 1..n: x;

%solve satisfy;
solve :: set_search(x, smallest, indomain_min, complete) satisfy;

constraint
  partition_set(x, 1..n)
  /\
  forall(i in 1..3) (
    let {
       array[1..n] of var bool: y
    }
    in
    card(x[i]) >= 3
    /\
    link_set_to_booleans(x[i], y)
    /\
    forall(a, b, c in ub(x[i])) (
       ( a in x[i] /\ b in x[i] /\ c in x[i]  
         /\ 
         a < b /\ b < c
      )
       ->
       (
         a + b != c
       )
    )
  )
    % this symmetry breaking works but is probably considered cheating
%   /\
%   1 in x[1]
%   /\
%   3 in x[2]
%   /\
%   9 in x[3]
   /\ % this symmetry breaking don't work: "Constraint set_lt not found"
   forall(i in 2..3) (
     x[i-1] < x[i]
   )
;

output [
  show(x), "\n"
];