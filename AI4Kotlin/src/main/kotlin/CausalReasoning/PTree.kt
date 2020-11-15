package CausalReasoning

// Probability Tree is
//        a root statement (no parents), usually random variable O (string) and value 1 (string)
//        and an ordered set of transitions, where a transition is
//                      transition probability (Double)
//                      child node (Node)
//        FIXME: transitions are MutableList
class PTree (val statement:Pair<String,String> = Pair("O","1"),
             val transitions: MutableList<Pair<Double, Node>>) {

    // Nodes
    // FIXME: transitions are MutableList
    class Node (val statements: List<Pair<String,String>>,
                val transitions: MutableList<Pair<Double, Node>>?)  {

        // Leaf is a node with no transitions.
        val isLeaf get() = transitions==null

        var nodeID:String=""

        //Return the list of all random variables and their values
        fun getRandomVariables(randomVariables: MutableList<Pair<String,String>>){
            if (!this.isLeaf)
                this.transitions?.forEach { it.second.getRandomVariables(randomVariables) }

            statements.forEach { if (!randomVariables.contains(it)) randomVariables.add(it) }
        }

        fun generateNodeIDs(rootID:String,index:Int){
            nodeID = "$rootID$index"
            if (!isLeaf){
                var i = 0
                transitions?.forEach { it.second.generateNodeIDs("$nodeID.", i++) }
            }
        }

        fun evaluateEvent(proposition:Pair<String,String>, minCut: Pair<MutableList<String>,MutableList<String>>){

            var match = false

            for (it in this.statements){
                if (it.first == proposition.first){
                    match=true
                    if (it.second == proposition.second)
                        minCut.first.add(this.nodeID)
                    else
                        minCut.second.add(this.nodeID)
                    break
                }
            }

            if (!match && !isLeaf)
                transitions?.forEach { it.second.evaluateEvent(proposition,minCut) }
        }

        fun print(level:Int, probability: Double){
           var padding = ""
           for (i in 1..level) {
               padding += "\t"
           }

           println("${padding}Statements:${statements}")
           println("${padding}NodeID:${nodeID}")
           println("${padding}P:"+ "%.4f".format(probability))
           if (!isLeaf)
               transitions?.forEach { it.second.print(level+1,it.first) }
        }

        init {
            // Statements verification: cannot be empty
            // FIXME: More complete verification needed
            if (statements.isEmpty())
                throw Exception("CausalReasoning.PTree.Node: Empty Statements List ")

            // Transition verification: if transitions is not empty
            // FIXME: More complete verification needed
            if (transitions!=null) {
                // First, let's order the transitions
                transitions.sortBy { it.first }

                var probability = 0.0
                // FIXME: Sum of float doesn't have to be exact. By now, I consider discrete float probabilities
                transitions.forEach { probability+=it.first  }

                if (probability != 1.0) {
                    //transition probabilities must sum up to one
                    throw Exception("CausalReasoning.PTree.Node: Transition Probabilities Must Sum Up To One ($probability)")
                }
            }
        }
    }

    fun evaluateEvent(proposition:Pair<String,String>): Pair<List<String>,List<String>>{
        var minCutTrue = mutableListOf<String>()
        var minCutFalse = mutableListOf<String>()

        var minCut = Pair(minCutTrue,minCutFalse)

        if (this.statement.first == proposition.first){
            if (this.statement.second == proposition.second)
                minCutTrue.add(this.rootID)
            else
                minCutFalse.add(this.rootID)
        }else
            transitions.forEach { it.second.evaluateEvent(proposition,minCut) }

        return minCut
    }

    fun evaluateNegEvent(proposition:Pair<String,String>): Pair<List<String>,List<String>>{
        val minCut = evaluateEvent(proposition)
        return Pair(minCut.second,minCut.first)
    }

    fun print(){
        println("Root\nNodeID:${rootID}\nStatement:[${statement.first},${statement.second}]")
        if (transitions.isNotEmpty()){
            println("Transitions")
            transitions.forEach { it.second.print(1, it.first) }
        }
    }

    // Return the list of all random variables and their values
    private fun getRandomVariables(): List<Pair<String,String>> {
        val randomVariables = mutableListOf<Pair<String,String>>()

        if (transitions.isNotEmpty())
            transitions.forEach { it.second.getRandomVariables(randomVariables) }

        return (randomVariables)
    }

    val variables:List<Pair<String,String>>

    val rootID:String="01"

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
}