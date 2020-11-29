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

        fun evaluateAND(eventEvaluation: Pair<MutableList<String>,MutableList<String>>,
                                event1: Pair<List<String>,List<String>>,
                                event2: Pair<List<String>,List<String>>,  match1: Boolean, match2: Boolean){

             var matchEvent1 = event1.first.contains(nodeID) || match1
             var matchEvent2 = event2.first.contains(nodeID) || match2

            if (event1.second.contains(nodeID) || event2.second.contains(nodeID))
                eventEvaluation.second.add(nodeID)
            else if (matchEvent1 && matchEvent2)
                eventEvaluation.first.add(nodeID)
            else if (!isLeaf)
                transitions?.forEach { it.second.evaluateAND(eventEvaluation, event1, event2, matchEvent1, matchEvent2) }
        }

        fun evaluatePREC(eventEvaluation: Pair<MutableList<String>,MutableList<String>>,
                        cause: Pair<List<String>,List<String>>,
                        effect: Pair<List<String>,List<String>>,  match: Boolean){

            var eventMatch=match

            if (eventMatch){
                if (effect.first.contains(nodeID) || effect.second.contains(nodeID) || cause.second.contains(nodeID)){
                    eventEvaluation.second.add(nodeID)
                }else {
                    if (cause.first.contains(nodeID))
                        eventMatch = false

                    if (!isLeaf)
                        transitions?.forEach { it.second.evaluatePREC(eventEvaluation, cause, effect, eventMatch) }
                }
            }else{
                // NOTE: the paper has some bug here. It searches on TRUE and FALSE set of the
                // "cause" and it should be the "effect"
                if (effect.first.contains(nodeID))
                    eventEvaluation.first.add(nodeID)
                else if (effect.second.contains(nodeID))
                    eventEvaluation.second.add(nodeID)
                else if (!isLeaf)
                    transitions?.forEach { it.second.evaluatePREC(eventEvaluation, cause, effect, eventMatch)  }
            }

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
        var eventTrue = mutableListOf<String>()
        var eventFalse = mutableListOf<String>()

        var eventEvaluation = Pair(eventTrue,eventFalse)

        if (this.statement.first == proposition.first){
            if (this.statement.second == proposition.second)
                eventTrue.add(this.rootID)
            else
                eventFalse.add(this.rootID)
        }else
            transitions.forEach { it.second.evaluateEvent(proposition,eventEvaluation) }

        return eventEvaluation
    }

    fun evaluateNOT(proposition:Pair<List<String>,List<String>>): Pair<List<String>,List<String>>{
        return Pair(proposition.second,proposition.first)
    }

    fun evaluateAND(event1: Pair<List<String>,List<String>>, event2: Pair<List<String>,List<String>>):Pair<List<String>,List<String>>{
        var eventTrue = mutableListOf<String>()
        var eventFalse = mutableListOf<String>()

        var eventEvaluation = Pair(eventTrue,eventFalse)

        var matchEvent1 = event1.first.contains(rootID)
        var matchEvent2 = event1.first.contains(rootID)

        //FIXME Consider the case where event1 and/or event2 are both the root: b and I == I and b = b
        if (event1.second.contains(rootID) || event2.second.contains(rootID))
            eventEvaluation.second.add(rootID)
        else if (matchEvent1 && matchEvent2)
            eventEvaluation.first.add(rootID)
        else {
            transitions.forEach { it.second.evaluateAND(eventEvaluation, event1, event2, matchEvent1, matchEvent2) }
        }

        return eventEvaluation
    }

    fun evaluatePREC(cause: Pair<List<String>,List<String>>, effect: Pair<List<String>,List<String>>):Pair<List<String>,List<String>>{
        var eventTrue = mutableListOf<String>()
        var eventFalse = mutableListOf<String>()
        var eventEvaluation = Pair(eventTrue,eventFalse)

        var matchEvent = !cause.first.contains(rootID)

        if (effect.first.contains(rootID) || effect.second.contains(rootID) || cause.second.contains(rootID)) {
            eventEvaluation.second.add(rootID)
        }
        else {
            transitions.forEach { it.second.evaluatePREC(eventEvaluation, cause, effect, matchEvent) }
        }

        return eventEvaluation
    }

    fun evaluateOR(event1: Pair<List<String>,List<String>>, event2: Pair<List<String>,List<String>>): Pair<List<String>,List<String>>{
        return evaluateNOT(evaluateAND(evaluateNOT(event1),evaluateNOT(event2)))
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