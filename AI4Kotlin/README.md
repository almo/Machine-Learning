## AI Applications in Kotlin

### Introduction
Kotlin [1] is a programming language introduced by JetBrains in 2011, 
released under an open source license in 2012. In the recent years, 
Kotlin is becoming more popular, being selected in 2017 as the official
programming language for Android [2]. IEEE Computer Society ranked 
Kotlin as the second programming language in the list of those that 
everyone should learn in 2020 [3]. In addition to fully compatible with 
Java [4], according the official website, its main features are:

  * **Concise**, drastically reduce the amount of boilerplate code.
  * **Safe**, avoid entire classes of errors such as null pointer exceptions.
  * **Interoperable**, Leverage existing libraries for the JVM, Android, and the browser.  

This project aims to foster the usage of Kotlin in more areas, showcasing
how the language can also be used in artificial intelligence and machine 
learning. In particular, the project includes three cases:

  * **Causal Reasoning**
  * **Dynamic programming**
  * **Machine Learning** 

### Causal Reasoning
According its entry in the Wikipedia, Causal Inference[5] is " is the process 
of drawing a conclusion about a causal connection based on the conditions 
of the occurrence of an effect". Causal reasoning has many and diverse 
applications. This project will implement some algorithms included in
the reference "Algorithms for Causal Reasoning in Probably Trees"[6], implementing
some of the algorithms[7] included in the papers. 

The paper proposes **discrete probability trees** as an alternative representation
of causal dependencies. Using this model, each node represents a state of the 
stochastic process, and the arrows connecting nodes causal dependencies between 
states, indicating probabilistic transitions. Formally and following a recursive approach
the paper establishes:

  * A node n &isin; &#990; is a tuple n=(u,S,C) where
    * u is an ID with the structure 01.n.n. .. .n with n &isin; N
    * S is a list of statements pairs values (variable, value), for instance (X,1) or (W, rainy)
    * C is either null or, an ordered set of pairs (p, m) with p &isin; [0,1], a transition probability, an m &isin; &#990; a child node 
  * A *total realization* is a path from the root to a leaf.
  * An *event* (W, rainy) is a collection of total realizations traversing a node with the statement (W, rainy).
  * A min-cut &#948; is the minimum collection of nodes with probability summing up to 1 representing an event.

In this implementation I have implemented the following data structures 

```Kotlin
class PTree (val statement:Pair<String,String> = Pair("O","1"),
             val transitions: MutableList<Pair<Double, Node>>) {

    class Node (val statements: List<Pair<String,String>>,
                val transitions: MutableList<Pair<Double, Node>>?)  {

        var nodeID:String=""

    }
    val rootID:String="01"
}
```
Considering how similar PTree and Node are, clearly there are more efficient ways to implement them. 
However, this is for future refactoring. Both classes are initializated in the proper init sections where 
IDs, order and transitions checking is done.

```Kotlin
   init {
        if (transitions.isNotEmpty()){
            // Before to generate the IDs, let's order the transitions
            transitions.sortBy { it.first }

            // Transition verification: if transitions is not empty
            // FIXME: More complete verification needed
            var probability = 0.0

            transitions.forEach { probability+=it.first  }

            // FIXME: Sum of float doesn't have to be exact. By now, I consider discrete float probabilities
            if (probability != 1.0) {
                //transition probabilities must sum up to one
                throw Exception("CausalReasoning.PTree.Node: Transition Probabilities Must Sum Up To One ($probability)")
            }

            // Generating node IDs
            var i=0
            transitions.forEach { it.second.generateNodeIDs("$rootID.",i++) }

            // Building random variables list
            variables = getRandomVariables()
        }else
            variables = listOf(Pair("O","1"))
    }
```
In addition basic inference functions are also implemented:

  * Function *evaluateEvent(n, (X,V))* gets the events (min-cuts) &#948;<sub>T</sub>, &#948;<sub>F</sub> making 
  true and false the statements (X,V) for a probability tree with root n.
  * Function *evaluateAND(&#948;<sub>1</sub>, &#948;<sub>2</sub>)* evaluates the conjunction of the events (min-cuts) 
  &#948;<sub>1</sub> and &#948;<sub>2</sub> for a probability tree with root n.
  * Function *evaluateNOT(&#948;)* evaluates the not (negative) of the event (min-cut) 
    &#948; for a probability tree with root n.
  * Function *evaluateOR(&#948;<sub>1</sub>, &#948;<sub>2</sub>)* evaluates the join of the events (min-cuts) 
      &#948;<sub>1</sub> and &#948;<sub>2</sub> for a probability tree with root n.
  * Function *evaluatePREC(&#948;<sub>c</sub>, &#948;<sub>e</sub>)* evaluates the causal relationship between the events (min-cuts) 
    cause &#948;<sub>c</sub> and event &#948;<sub>e</sub> for a probability tree with root n.
    
NOTE: the specification of the algorithm of the evaluatePREC (figure 4) included in the paper has two bugs: it evaluates the
cause set of TRUE and FALSE when it should be evaluated the effect. In addition, the results shown in the figure 5 are 
wrong too. 

### Dynamic Programming

### Machine Learning

### Utils
[JGraphT](https://jgrapht.org/) s a free Java class library that provides mathematical graph-theory objects and algorithms.

### References

[1] [Kotlin Programming Language](https://kotlinlang.org/)

[2] [Develop Android apps with Kotlin](https://developer.android.com/kotlin)

[3] [Programming Languages You Should Learn in 2020](https://www.computer.org/publications/tech-news/trends/programming-languages-you-should-learn-in-2020)

[4] [Comparison to Java Programming Language](https://kotlinlang.org/docs/reference/comparison-to-java.html)

[5] [Causal Inference](https://en.wikipedia.org/wiki/Causal_inference)

[6] [Algorithms for Causal Reasoning in Probability Trees](https://arxiv.org/abs/2010.12237)

[7] [Tutorial: Causal Reasoning in Probability Trees](https://colab.research.google.com/github/deepmind/deepmind_research/blob/master/causal_reasoning/Causal_Reasoning_in_Probability_Trees.ipynb)