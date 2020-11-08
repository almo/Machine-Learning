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
the reference "Algorithms for Causal Reasoning in Probably Trees"[6], porting
the notebook[7] developed for the authors of the paper. 

The paper proposes **discrete probability trees** as an alternative representation
of causal dependencies. Using this model, each node represents a state of the 
stochastic process, and the arrows connecting nodes causal dependencies between 
states, indicating probabilistic transitions. 

### Dynamic Programming

### Machine Learning

### References

[1] [Kotlin Programming Language](https://kotlinlang.org/)

[2] [Develop Android apps with Kotlin](https://developer.android.com/kotlin)

[3] [Programming Languages You Should Learn in 2020](https://www.computer.org/publications/tech-news/trends/programming-languages-you-should-learn-in-2020)

[4] [Comparison to Java Programming Language](https://kotlinlang.org/docs/reference/comparison-to-java.html)

[5] [Causal Inference](https://en.wikipedia.org/wiki/Causal_inference)

[6] [Algorithms for Causal Reasoning in Probability Trees](https://arxiv.org/abs/2010.12237)

[7] [Tutorial: Causal Reasoning in Probability Trees](https://colab.research.google.com/github/deepmind/deepmind_research/blob/master/causal_reasoning/Causal_Reasoning_in_Probability_Trees.ipynb)