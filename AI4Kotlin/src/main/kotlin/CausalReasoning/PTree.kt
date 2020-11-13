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
            if (isLeaf)
                nodeID = "$rootID$index"
            else
                nodeID = "$rootID$index."
            var i=0
            transitions?.forEach { it.second.generateNodeIDs(nodeID,i++) }
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
    }
}