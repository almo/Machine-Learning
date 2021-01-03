package CausalReasoning

import java.security.KeyStore

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
            nodeID = "${rootID}.$index"
            if (!isLeaf){
                var i = 0
                transitions?.forEach { it.second.generateNodeIDs("$nodeID", i++) }
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

             val matchEvent1 = event1.first.contains(nodeID) || match1
             val matchEvent2 = event2.first.contains(nodeID) || match2

            if (event1.second.contains(nodeID) || event2.second.contains(nodeID))
                eventEvaluation.second.add(nodeID)
            else if (matchEvent1 && matchEvent2)
                eventEvaluation.first.add(nodeID)
            else if (!isLeaf)
                transitions?.forEach { it.second.evaluateAND(eventEvaluation, event1, event2, matchEvent1, matchEvent2) }
        }

        fun filterCOND(event:Pair<List<String>,List<String>>,probability:Double):Pair<Double,Double>{
            var updatedProbs = Pair(0.0,0.0)

            when {
                // Node belonging to event's TRUE min cut
                event.first.contains(nodeID) -> updatedProbs=Pair(1.0,probability)

                // Node belonging to event's FALSE min cut
                event.second.contains(nodeID) -> updatedProbs=Pair(0.0,0.0)

                else -> {
                    var regularP = 0.0
                    var specialP = 0.0
                    var nodeProbs: Pair<Double,Double>
                    val tempTransitions = mutableMapOf<String,Pair<Double,Double>>()

                    if (!isLeaf) {
                        transitions?.forEach {
                            nodeProbs= it.second.filterCOND(event,it.first*probability)
                            tempTransitions[it.second.nodeID] = nodeProbs
                            specialP += nodeProbs.first
                            regularP += nodeProbs.second
                        }
                        val transitionsIterator = transitions?.listIterator()

                        if (transitionsIterator != null) {
                            while (transitionsIterator.hasNext()) {
                                var newTransition: Pair<Double, Node>
                                val oldTransition = transitionsIterator.next()

                                if (regularP > 0)
                                    newTransition = oldTransition.copy(
                                        first = (tempTransitions[oldTransition.second.nodeID]?.second?.div(regularP)!!)
                                    )
                                else
                                    newTransition = oldTransition.copy(
                                        first = (tempTransitions[oldTransition.second.nodeID]?.first?.div(specialP)!!)
                                    )

                                transitionsIterator.set(newTransition)
                            }
                        }
                        updatedProbs=Pair(1.0,regularP)
                    }
                }
            }

            return updatedProbs
        }

        fun filterDO(event:Pair<List<String>,List<String>>):Boolean{
            var bFiltered = true

            when {
                // Node belonging to event's TRUE min cut
                event.first.contains(nodeID) -> bFiltered=true

                // Node belonging to event's FALSE min cut
                event.second.contains(nodeID) -> bFiltered=false

                else -> {
                    var regularP = 0.0
                    var specialP = 0.0
                    val tempTransitions = mutableMapOf<String,Pair<Double,Double>>()

                    if (!isLeaf) {

                        transitions?.forEach {
                            if (it.second.filterDO(event)){
                                tempTransitions[it.second.nodeID] = Pair(1.0,it.first)
                                specialP += 1.0
                                regularP += it.first
                            }else
                                tempTransitions[it.second.nodeID] = Pair(0.0,it.first)
                        }

                        val transitionsIterator = transitions?.listIterator()

                        if (transitionsIterator != null) {
                            while (transitionsIterator.hasNext()) {
                                var newTransition: Pair<Double, Node>
                                val oldTransition = transitionsIterator.next()

                                if (regularP > 0)
                                    newTransition = oldTransition.copy(
                                        first = (tempTransitions[oldTransition.second.nodeID]?.second?.div(regularP)!!)
                                    )
                                else
                                    newTransition = oldTransition.copy(
                                        first = (tempTransitions[oldTransition.second.nodeID]?.first?.div(specialP)!!)
                                    )

                                transitionsIterator.set(newTransition)
                            }
                        }
                        bFiltered=true
                    }
                }
            }

            return bFiltered
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

        fun copy():Node{
            val node:Node
            val tempStatements = mutableListOf<Pair<String,String>>()

            statements.forEach { tempStatements.add(Pair(it.first,it.second)) }

            if (isLeaf)
                node = Node(tempStatements,null)
            else {
                val tempTransition = mutableListOf<Pair<Double,Node>>()

                transitions?.forEach { tempTransition.add(Pair(it.first,it.second.copy())) }
                node = Node(tempStatements,tempTransition)
            }

            return node
        }

        fun sameStatements(statements: List<Pair<String,String>>):Boolean{
            var aretheSame = true

            when {
                this.statements.size != statements.size -> aretheSame = false
                else -> {
                    this.statements.forEach { aretheSame = aretheSame && statements.contains(it) }
                }
            }

            return aretheSame
        }

        fun isEqual(node: Node):Boolean{
            var isEqual = true

            when{
                (node.nodeID != this.nodeID)  -> isEqual=false
                (!this.sameStatements(node.statements)) -> isEqual=false
                (node.transitions != this.transitions) -> isEqual=false
                else -> {
                    if (this.transitions!=null){
                        when {
                                (node.transitions?.size != this.transitions?.size) -> isEqual=false
                            else -> {
                                node?.transitions?.forEach { it ->
                                val nodeID = it.second.nodeID

                                val auxNode = this.transitions?.find { ti ->
                                    (it.first == ti.first && ti.second.nodeID == nodeID && ti.second.sameStatements(it.second.statements))
                                }

                                isEqual = if (auxNode != null)
                                    isEqual && auxNode?.second.isEqual(it.second)
                                else
                                    false
                                }
                            }
                        }
                    }
                }
            }

            return isEqual
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
        val eventTrue = mutableListOf<String>()
        val eventFalse = mutableListOf<String>()

        val eventEvaluation = Pair(eventTrue,eventFalse)

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
        val eventTrue = mutableListOf<String>()
        val eventFalse = mutableListOf<String>()

        val eventEvaluation = Pair(eventTrue,eventFalse)

        val matchEvent1 = event1.first.contains(rootID)
        val matchEvent2 = event1.first.contains(rootID)

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

    /**
     * evaluateCOND: updates the transition probabilities of a COPY after an event is revealed to
     * be TRUE i.e. P (A | B). This doesn't modify the calling instance i.e. it makes a copy,
     *
     * @param: event i.e. pair of lists corresponding to the nodes for which the event is TRUE and those for
     * which the event is FALSE
     * @return a copy of the calling instance with the probabilities updates according the event.
     */
    fun evaluateCOND(event:Pair<List<String>,List<String>>): PTree {
        val condTree = this.copy()

        condTree.filterCOND(event)

        return condTree
    }

    /**
     * filterCOND: updates the transition probabilities after an event is revealed to
     * be TRUE i.e. P (A | B). This modifies the calling instance.
     *
     * @param event: pair of lists corresponding to the nodes for which the event is TRUE and those for which the
     * event is FALSE
     */
    fun filterCOND(event:Pair<List<String>,List<String>>){
        var regularP = 0.0
        var specialP = 0.0
        var nodeProbs: Pair<Double,Double>
        val tempTransitions = mutableMapOf<String,Pair<Double,Double>>()

        transitions.forEach {
            nodeProbs= it.second.filterCOND(event,it.first)
            tempTransitions[it.second.nodeID] = nodeProbs
            specialP += nodeProbs.first
            regularP += nodeProbs.second
        }

        val transitionsIterator = transitions.listIterator()

        while (transitionsIterator.hasNext()) {
            var newTransition: Pair<Double, Node>
            val oldTransition = transitionsIterator.next()

            if (regularP > 0)
                newTransition = oldTransition.copy(first = (tempTransitions[oldTransition.second.nodeID]?.second?.div(regularP)!!))
            else
                newTransition = oldTransition.copy(first = (tempTransitions[oldTransition.second.nodeID]?.first?.div(specialP)!!))

            transitionsIterator.set(newTransition)
        }
    }

    /**
     * evaluateDo: do an intervention according to an event (min-cut) without modifying the instance i.e. it
     * makes a copy
     *
     * @param: event i.e. pair of lists corresponding to the nodes for which the event is TRUE and those for
     * which the event is FALSE
     * @return a copy of the calling instance with the probabilities updates according the intervention.
     */
    fun evaluateDO(event:Pair<List<String>,List<String>>): PTree {
        val doTree = this.copy()

        doTree.filterDO(event)

        return doTree
    }

    /**
     * filterDO: updates the transition probabilities in order to bring about a desired event with probability one.
     * This operation allows answering questions of the form P(A | do(B))
     *
     * @param event: pair of lists corresponding to the nodes for which the event is TRUE and those for which the
     * event is FALSE
     */
    fun filterDO(event:Pair<List<String>,List<String>>){
        var regularP = 0.0
        var specialP = 0.0
        val tempTransitions = mutableMapOf<String,Pair<Double,Double>>()

        transitions.forEach {
            if (it.second.filterDO(event)){
                tempTransitions[it.second.nodeID] = Pair(1.0,it.first)
                specialP += 1.0
                regularP += it.first
            }else
                tempTransitions[it.second.nodeID] = Pair(0.0,it.first)
        }

        val transitionsIterator = transitions.listIterator()

        while (transitionsIterator.hasNext()) {
            var newTransition: Pair<Double, Node>
            val oldTransition = transitionsIterator.next()

            if (regularP > 0)
                newTransition = oldTransition.copy(first = (tempTransitions[oldTransition.second.nodeID]?.second?.div(regularP)!!))
            else
                newTransition = oldTransition.copy(first = (tempTransitions[oldTransition.second.nodeID]?.first?.div(specialP)!!))

            transitionsIterator.set(newTransition)
        }
    }

    fun evaluatePREC(cause: Pair<List<String>,List<String>>, effect: Pair<List<String>,List<String>>):Pair<List<String>,List<String>>{
        val eventTrue = mutableListOf<String>()
        val eventFalse = mutableListOf<String>()
        val eventEvaluation = Pair(eventTrue,eventFalse)

        val matchEvent = !cause.first.contains(rootID)

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

    fun copy():PTree {
        val tempTransitions = mutableListOf<Pair<Double, Node>>()

        transitions.forEach { tempTransitions.add(it.copy()) }

        return PTree(transitions = tempTransitions)
    }

    fun equal(pTree: PTree): Boolean{
        var isEqual = true

        when{
            (pTree.statement != this.statement) || (pTree.rootID != this.rootID) || (pTree.transitions.size != this.transitions.size) -> isEqual=false
            else -> {
                pTree.transitions.forEach { it ->
                    val nodeID= it.second.nodeID

                    val node = this.transitions.find { ti ->
                        (it.first==ti.first && ti.second.nodeID == nodeID && ti.second.sameStatements(it.second.statements))
                    }

                    isEqual = if (node!=null)
                        isEqual && node?.second.isEqual(it.second)
                    else
                        false
                }
            }
        }

        return isEqual
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
            transitions.forEach { it.second.generateNodeIDs("$rootID",i++) }

            // Building random variables list
            variables = getRandomVariables()
        }else
            variables = listOf(Pair("O","1"))
    }
}