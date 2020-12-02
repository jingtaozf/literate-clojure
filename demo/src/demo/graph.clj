# -*- encoding:utf-8; Mode: ORG;  -*- ---
#+Startup: noindent
#+SubTitle: Just in one file without tangle
#+OPTIONS: tex:t toc:2 \n:nil @:t ::t |:t ^:nil -:t f:t *:t <:t
#+STARTUP: latexpreview
#+STARTUP: noindent
#+STARTUP: inlineimages
#+PROPERTY: literate-lang clojure
#+PROPERTY: literate-load yes
#+STARTUP: entitiespretty
* Table of Contents                                               :noexport:TOC:
- [[#introduction][Introduction]]
- [[#preparation][Preparation]]
  - [[#namespace][namespace]]
- [[#original-codes-from-the-article][Original codes from the article]]
  - [[#graph-representing][graph representing]]
  - [[#a-depth-first-search-on-a-graph-returning-a-sequence][a depth first search on a graph returning a sequence]]
  - [[#a-lazy-sequence-version-of-a-depth-first-search-on-a-graph][a lazy sequence version of a depth first search on a graph]]
  - [[#a-lazy-sequence-version-of-a-breadth-first-search-on-a-graph][a lazy sequence version of a breadth first search on a graph]]
  - [[#generic-function-for-dfs-and-bfs][generic function for DFS and BFS]]
- [[#questions][Questions]]
  - [[#1-extend-the-graph-definition-to-include-a-weight-between-graph-edges][1. Extend the graph definition to include a weight between graph edges]]
  - [[#2-write-an-algorithm-to-randomly-generate-a-simple-directed-graph-using-your-answer-from-1][2. Write an algorithm to randomly generate a simple directed graph using your answer from #1]]
  - [[#3-write-an-implementation-of-dijkstras-algorithm][3. Write an implementation of Dijkstra's algorithm]]
  - [[#4-write-a-suite-of-functions-to-calculate-distance-properties-for-your-graph][4. Write a suite of functions to calculate distance properties for your graph.]]
  - [[#notes-for-java][Notes for JAVA]]
- [[#answers][Answers]]
  - [[#1-how-to-represent-a-directed-graph][1. How to represent a directed graph]]
  - [[#2-an-algorithm-to-randomly-generate-a-simple-directed-graph][2. An algorithm to randomly generate a simple directed graph]]
  - [[#3-write-an-implementation-of-dijkstras-algorithm-1][3. Write an implementation of Dijkstra's algorithm]]
  - [[#4-write-a-suite-of-functions-to-calculate-distance-properties-for-your-graph-1][4. Write a suite of functions to calculate distance properties for your graph.]]

* Introduction
This is a demo code for graph traversal, to pass the [[https://bitbucket.org/audiencerepublic/developer-test/wiki/clojure-2][developer test]] based on article [[http://hueypetersen.com/posts/2013/06/25/graph-traversal-with-clojure/][Graph Traversal with Clojure]].
* Preparation
** namespace
Let's create a new namespace for this library.
#+BEGIN_SRC clojure
(ns demo.graph
  (:require
   [clojure.pprint :refer [cl-format]]
   [dorothy.core :as dot]
   [clojure.data.priority-map :refer [priority-map]]
   ))
#+END_SRC
* Original codes from the article
This section is a summary of the article [[http://hueypetersen.com/posts/2013/06/25/graph-traversal-with-clojure/][Graph Traversal with Clojure]].
** graph representing
The graph structure is a map of nodes to a list of neighbors.
So ={ :1 [:2 :3] }= means =:1= has two edges
– one to =:2= and
- one to =:3=.
#+BEGIN_SRC clojure
(def G {
        :1 [:2 :3],
        :2 [:4],
        :3 [:4],
        :4 [] })
#+END_SRC
#+begin_src dot :file "../../docs/images/graph-representing.png" :results file :exports results
digraph D {
	rankdir=LR;
	n1[label="1"];
	n2[label="2"];
	n3[label="3"];
	n4[label="4"];
	{rank = same; n2; n3;};
	n1 -> n2;
	n1 -> n3;
	n2 -> n4;
	n3 -> n4;
}
#+end_src

#+RESULTS:
[[file:../../docs/images/graph-representing.png]]
** a depth first search on a graph returning a sequence
The course describes the algorithm recursively so I’m using Clojure’s loop and recur syntax.
In the loop we want to keep track of three things:
- a built up vector of traversed vertices
- a set of explored vertices (explored means we have seen it)
- a stack of frontier vertices (frontier means we have explored it but haven’t explored its neighbors)

In the initial case we have
- an empty vector of traversed vertices,
- an explored set including our starting vertex and
- a frontier stack including our starting vertex.

On each iteration we check if the frontier stack is empty.
If it is we are done and return the traversed vertices.
If not we pop the stack for a vertex and get the vertex’s neighbors.
We then recurse adding the vertex to the traversed vertices, adding the neighbors to the explored set,
and adding the unexplored neighbors to the frontier.

This works but its eager – the graph is fully traversed when you call the function.
#+BEGIN_SRC clojure
(defn traverse-graph-dfs [g s]
  (loop [vertices []
         explored #{s}
         frontier [s]]
    (if (empty? frontier)
      vertices
      (let [v (peek frontier)
            neighbors (g v)]
        (recur
         (conj vertices v)
         (into explored neighbors)
         (into (pop frontier) (remove explored neighbors)))))))
#+END_SRC

A test for it
#+BEGIN_SRC clojure :load no :exports both
(traverse-graph-dfs G :1)
#+END_SRC

#+RESULTS:
| :1 | :3 | :4 | :2 |

** a lazy sequence version of a depth first search on a graph
Instead of loop and recur we define a recursive function, =rec-dfs=, and use =lazy-seq= to build up the sequence with recursive calls.
Each call cons the current vertex to the result of a new recursive call finally ending with a nil (end of sequence).
Through the magic of lazy-seq we don't have to worry about blowing the stack and get the benefits of the sequence being lazy.
This means if I take 2 from the sequence we only traverse 2 nodes, not the entire graph as we would have done in =traverse-graph-dfs=.
#+BEGIN_SRC clojure
(defn seq-graph-dfs [g s]
  ((fn rec-dfs [explored frontier]
     (lazy-seq
      (when (seq frontier)
        (let [v (peek frontier)
              neighbors (g v)]
          (cons v (rec-dfs
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{s} [s]))
#+END_SRC

A test for it
#+BEGIN_SRC clojure :load no :exports both
(seq-graph-dfs G :1)
#+END_SRC

#+RESULTS:
| :1 | :3 | :4 | :2 |

** a lazy sequence version of a breadth first search on a graph
With DFS working I wanted to try BFS.
Luckily the change is simple – just change the stack to a queue.
#+BEGIN_SRC clojure
(defn seq-graph-bfs [g s]
  ((fn rec-bfs [explored frontier]
     (lazy-seq
      (when (seq frontier)
        (let [v (peek frontier)
              neighbors (g v)]
          (cons v (rec-bfs
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{s} (conj (clojure.lang.PersistentQueue/EMPTY) s)))
#+END_SRC

A test for it
#+BEGIN_SRC clojure :load no :exports both
(seq-graph-bfs G :1)
#+END_SRC

#+RESULTS:
| :1 | :2 | :3 | :4 |

** generic function for DFS and BFS
Finally, since the only difference is the initial data structure we can make it an argument to a generic function.
#+BEGIN_SRC clojure
(defn seq-graph [d g s]
  ((fn rec-seq [explored frontier]
     (lazy-seq
      (when (seq frontier)
        (let [v (peek frontier)
              neighbors (g v)]
          (cons v (rec-seq
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{s} (conj d s)))

(def seq-graph-dfs (partial seq-graph []))
(def seq-graph-bfs (partial seq-graph (clojure.lang.PersistentQueue/EMPTY)))
#+END_SRC

* Questions
This section is a summary of the questions from the [[https://bitbucket.org/audiencerepublic/developer-test/wiki/clojure-2][developer test]].
** 1. Extend the graph definition to include a weight between graph edges
For example:
#+BEGIN_SRC clojure :load no
(def G {
        :1 [(:2 1) (:3 2)],
        :2 [(:4 4)],
        :3 [(:4 2)],
        :4 [] })
#+END_SRC
I've converted the items of the array into tuples with the vertex name and the weight -- as an integer --
for the edge weight from the start to end vertex.

You can choose something similar or extend it to something you prefer
** 2. Write an algorithm to randomly generate a simple directed graph using your answer from #1
Such that
#+begin_example
Input:
    N - size of generated graph
    S - sparseness (number of directed edges actually; from N-1 to N(N-1)/2)
Output:
    simple connected graph G(n,s) with N vertices and S edges
#+end_example
Please ensure that your graph is connected, otherwise you won't be able to complete the following questions.
** 3. Write an implementation of [[https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm][Dijkstra's algorithm]]
that traverses your graph and outputs the shortest path between any 2 randomly selected vertices.
I should be able to write something like this for example.
#+BEGIN_SRC clojure :load no
(def random-graph G(10,10))
(D random-graph (first (keys random-graph)) (last (keys random-graph)))
#+END_SRC
=> list of nodes which is the shortest path by edge weight between the 2 nodes,
or no path if one does not exist.
** 4. Write a suite of functions to calculate distance properties for your graph.
Now that you have implemented Dijkstra's algorithm you should be able to calculate the eccentricity of
any vertex in your graph, and in turn the radius and diameter of your graph.

Please re-acquaint yourself with graph distance properties
https://en.wikipedia.org/wiki/Distance_(graph_theory),

The eccentricity of a vertex =v= is defined as the greatest distance between =v= and any other vertex.
The radius of a graph is the minimum eccentricity of any vertex in a graph.
The diameter of a graph is the maximum eccentricity of any vertex in a graph.
I should be able to write something like this:
#+BEGIN_SRC clojure :load no
(def random-graph G(10,10))

(eccentricity random-graph (first (keys random-graph)))
;; => number expressing eccentricity for `first` vertex in random-graph

(radius random-graph); => minimal eccentricity
(diameter random-graph); => maximal eccentricity
#+END_SRC
** Notes for JAVA
If you've written this test in Java, please provide, a simple cmd-line interface that accepts as input N size and S sparseness, for example:
#+begin_src sh
graph -N 5 -S 10
#+end_src
it would then print out the randomly generated graph
#+BEGIN_SRC clojure :load no
{
 :1 [(:2 1) (:3 2)],
 :2 [(:4 4)],
 :3 [(:4 2)],
 :4 [] }
#+END_SRC
and also print:
- radius
- diameter

of the randomly generated graph.

In addition, it should also:

- randomly select 2 nodes and print the shortest path distance between them,
- compute eccentricity of a random node

We'll also use JShell to interactively run some of your functions, please ensure that you can connect to your answer via JShell.
* Answers
This section show the answers for above questions.
** 1. How to represent a directed graph
To represent a directed graph with weight, we will use
- a unique integer as the vertex name
  - Actually it can be any valid key name of a map in our implementation.
  - We will not use a =keyword= as the vertex name to avoid introducing additional keywords to Clojure namespace.
- an array containing a list of tuples with the vertex name and the weight

And a graph will be a =map= which =keys= are all vertices and =values= are all edges with weight.
#+BEGIN_SRC clojure :load no
(def G {
        1 [(2 1) (3 2)],
        2 [(4 4)],
        3 [(4 2)],
        4 [] })
#+END_SRC
So edges of one vertex can be obtained by its vertex name.
#+BEGIN_SRC clojure
(defn edges [G vertex]
  (get G vertex))
#+END_SRC

To create a new edge
#+BEGIN_SRC clojure
(defn create-edge [edge-vertex weight]
  [edge-vertex weight])
#+END_SRC


And edge target vertex is its first element in the tuple.
#+BEGIN_SRC clojure
(defn edge-vertex [edge]
  (first edge))
#+END_SRC
Edge weight is its second element in the tuple.
#+BEGIN_SRC clojure
(defn edge-weight [edge]
  (second edge))
#+END_SRC


** 2. An algorithm to randomly generate a simple directed graph
Such that
#+begin_example
Input:
    N - size of generated graph
    S - sparseness (number of directed edges actually; from N-1 to N(N-1)/2)
Output:
    simple connected graph G(n,s) with N vertices and S edges
#+end_example

For our random graph =G(N S)=, we will apply the following rules
1. The vertices are integers from =1= to =N=.
2. Only a connection from a smaller vertex name to a larger vertex name is allowed.
3. The weight for an edge is a random integer value between =1= and =10=.
   #+BEGIN_SRC clojure
   (defn random-weight []
     (inc (rand-int 10)))
   #+END_SRC
4. The edges for a vertex is sorted ascendingly by its target vertex.
   - It will give a better readability to our graph data.

By above definition, any edge in a graph =G(N S)= will have a unique identifier based on its two vertices =(A B)= like this
#+BEGIN_SRC clojure
(defn edge-id [N A B]
  (+ (* A N) B))
#+END_SRC

So for a graph =G(N S)=, we can build an array to hold all its edges, please note that for any edge =(A B)=, =A= must be smaller than =B=:
#+BEGIN_SRC clojure
(defn graph-edges [N]
  (for [A (range 1 N)
        B (range 2 (inc N))
        :when (> B A)]
    (edge-id N A B)))
#+END_SRC

And we can choose random edges after a =shuffle= like this:
#+BEGIN_SRC clojure
(defn random-edges [N S]
  (sort (take S (shuffle (graph-edges N)))))
#+END_SRC
We will sort the edges to meet the rule 4 in our definition.

We can get the start vertex =A= of an edge by such calculation:
#+BEGIN_SRC clojure
(defn edge-start-vertex [N edge-id]
  (int (/ (dec edge-id) N)))
#+END_SRC

And get the end vertex =B= of an edge by such calculation:
#+BEGIN_SRC clojure
(defn edge-end-vertex [N edge-id edge-start-vertex]
  (- edge-id (* edge-start-vertex N)))
#+END_SRC

Then we can extract edges for each vertex and build it to a graph like this:
#+BEGIN_SRC clojure
(defn G [N S]
  {:pre [(and (> N 0)
              (<= (dec N) S (int (/ (* N (dec N)) 2))))]}
  (as-> (reduce (fn [g edge-id]
                  (let [start-vertex (edge-start-vertex N edge-id)
                        end-vertex (edge-end-vertex N edge-id start-vertex)]
                    (assoc! g start-vertex (conj (get g start-vertex [])
                                                 (create-edge end-vertex (random-weight))))))
                (transient {})
                (random-edges N S))
      graph
    (persistent! graph)
    ;; convert into a sorted map for a better readability.
    (into (sorted-map) graph)
    ;; add lacked vertices which don't have any edge for a better readability.
    (merge graph (apply sorted-map (reduce (fn [keyvals vertex-id]
                                             (concat keyvals [vertex-id []]))
                                           {} (apply (partial disj (set (range 1 (inc N))))
                                                     (keys graph)))))))
#+END_SRC

We can visualize this graph into a picture via [[https://github.com/daveray/dorothy][graphviz DOT]].
#+BEGIN_SRC clojure
(defn render-graph [graph graph-file]
  (-> (reduce (fn [ret vertex]
                (concat ret (map
                             (fn [edge]
                               [vertex (edge-vertex edge) {:label (edge-weight edge)}])
                             (edges graph vertex))))
              [] (keys graph))
      dot/digraph
      dot/dot
      (dot/save! graph-file {:format "png"})))
#+END_SRC

So you can define a graph and visualize it like this
#+BEGIN_SRC clojure :load no
;; (def random-graph (G 10 10))
(def random-graph
  {1 [[2 8] [8 5]],
   2 [[4 1] [7 6] [10 4]],
   3 [],
   4 [[6 5] [10 6]],
   5 [[8 9]],
   6 [[10 2]],
   7 [],
   8 [],
   9 [[10 1]],
   10 []})
(render-graph random-graph "docs/images/random-graph.png")
#+END_SRC
[[../../docs/images/random-graph.png]]
** 3. Write an implementation of [[https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm][Dijkstra's algorithm]]
We use a priority queue =Q= to hold the distance for current visiting node for a better performance, and instead of
filling the priority queue with all nodes, we initialize it to contain only source to reduce the memory cost.

[[https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#Using_a_priority_queue][This wiki page]] contains the detailed description about this algorithm,
so we just list the steps of it here with some notes for our implementation.

1. Mark all nodes unvisited. Create a set of all the unvisited nodes called the unvisited set.
   - ~In this implementation~, it is unvisited if the vertex is not in the map =dist=.
2. Assign to every node a tentative distance value:
   set it to zero for our initial node and to infinity for all other nodes.
   Set the initial node as current
   - ~In this implementation~, the tentative distance value will be Clojure value =##Inf= for all other nodes.
3. For the current node, consider all of its unvisited neighbors and calculate their tentative distances through the current node.
   Compare the newly calculated tentative distance to the current assigned value and assign the smaller one.
   For example, if the current node A is marked with a distance of 6, and the edge connecting it with a neighbors B has length 2,
   then the distance to B through A will be 6 + 2 = 8. If B was previously marked with a distance greater than 8 then change it to 8.
   Otherwise, the current value will be kept.
4. When we are done considering all the unvisited neighbors of the current node,
   mark the current node as visited and remove it from the unvisited set. A visited node will never be checked again.
5. If the destination node has been marked visited (when planning a route between two specific nodes)
   or if the smallest tentative distance among the nodes in the unvisited set is infinity (when planning a complete traversal;
   occurs when there is no connection between the initial node and remaining unvisited nodes), then stop.
   The algorithm has finished.
   - ~in this implementation~, if we can't meet the =goal= vertex, then it will return =nil=.
6. Otherwise, select the unvisited node that is marked with the smallest tentative distance,
   set it as the new "current node", and go back to step 3.

#+BEGIN_SRC clojure
(defn D [G start goal]
  (loop [result {:Q (priority-map start 0)
                 :prev {}
                 :explored #{}
                 :dist {}}]
    ;; return best vertex
    (if-let [[v d] (peek (:Q result))]
      (if (= v goal)
        ;; return the path list.
        (extract-path-from-prev (:prev result) start goal)
        (recur (reduce (partial D-process-edge v d)
                       (update-in (update-in result [:Q] pop)
                                  [:explored] (fn [explored] (conj explored v)))
                       (edges G v)))))))
#+END_SRC
To update result based on an edge of current vertex.
#+BEGIN_SRC clojure
(defn D-process-edge [v d result edge]
  (let [edge-vertex (edge-vertex edge)]
    (if (contains? (:explored result) edge-vertex)
      ;; if it has been visited, not visit it twice.
      result
      (let [alt (+ d (edge-weight edge))]
        (if (< alt (get-in result [:dist edge-vertex] ##Inf))
          (assoc result
                 ;; add best vertex
                 :Q (assoc (:Q result) edge-vertex alt)
                 :dist (assoc (:dist result) edge-vertex alt)
                 :prev (assoc (:prev result) edge-vertex v))
          result)))))
#+END_SRC

To extract path from the =prev= section, please note that:
- each key in map =prev= is a vertex and
- the value for each key is its previous shortest vertex.
#+BEGIN_SRC clojure
(defn extract-path-from-prev [prev start goal]
  (loop [path ()
         prev-node (get prev goal)]
    (if (= prev-node start)
      path
      (recur (cons prev-node path)
             (get prev prev-node)))))
#+END_SRC

Now you can get the shortest path between two vertices like this:
#+BEGIN_SRC clojure :load no
(def random-graph (G 10 10))
(D random-graph (first (keys random-graph)) (last (keys random-graph)))
(D random-graph 1 10)
#+END_SRC
** 4. Write a suite of functions to calculate distance properties for your graph.
