import CausalReasoning.PTree

fun main() {
    println("Probability Trees")

    val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),null)
    val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),null)
    val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),null)
    val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),null)
    val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),null)
    val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),null)
    val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),null)
    val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),null)

    val leaf38 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf(Pair(0.5,leaf30),Pair(0.5,leaf31)))
    val leaf39 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf(Pair(2.0/3.0,leaf32),Pair(1.0/3.0,leaf33)))
    val leaf40 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf(Pair(0.5,leaf34),Pair(0.5,leaf35)))
    val leaf41 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf(Pair(4.0/5.0,leaf36),Pair(1.0/5.0,leaf37)))

    val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(2.0/5.0,leaf38),Pair(3.0/5.0,leaf39)))
    val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(2.0/3.0,leaf41),Pair(1.0/3.0,leaf40)))

    val pTree =  PTree(transitions = mutableListOf(
            Pair(5.0/11.0,leaf42),
            Pair(6.0/11.0,leaf43)))

    pTree.print()

    println()
    val eventY1 = pTree.evaluateEvent(Pair("Y","1"))
    println("True Min Cut (Y=1): ${eventY1.first}")
    println("False Min Cut (Y=1): ${eventY1.second}")

    println()
    val eventZ0 = pTree.evaluateEvent(Pair("Z","0"))
    println("True Min Cut (Z=0): ${eventZ0.first}")
    println("False Min Cut (Z=0): ${eventZ0.second}")

    println()
    val eventY1andZ0 = pTree.evaluateAND(eventY1,eventZ0)
    println("True Min Cut (Y=1) && (Z=0): ${eventY1andZ0.first}")
    println("False Min Cut (Y=1) && (Z=0): ${eventY1andZ0.second}")

    println()
    val eventY1orZ0 = pTree.evaluateOR(eventY1,eventZ0)
    println("True Min Cut (Y=1) || (Z=0): ${eventY1orZ0.first}")
    println("False Min Cut (Y=1) || (Z=0): ${eventY1orZ0.second}")

    println()
    val eventY1PRECZ0 = pTree.evaluatePREC(eventY1,eventZ0)
    println("True Min Cut (Y=1) -> (Z=0): ${eventY1PRECZ0.first}")
    println("False Min Cut (Y=1) -> (Z=0): ${eventY1PRECZ0.second}")

    println()
    val eventY0 = pTree.evaluateEvent(Pair("Y","0"))
    println("True Min Cut (Y=0): ${eventY0.first}")
    println("False Min Cut (Y=0): ${eventY0.second}")

    val leaf1 = PTree.Node(mutableListOf(Pair("X","0")),null)
    val leaf2 = PTree.Node(mutableListOf(Pair("X","1")),null)
    val leaf3 = PTree.Node(mutableListOf(Pair("X","2")),null)

    val pTree1 =  PTree(transitions = mutableListOf(
        Pair(0.0,leaf1),
        Pair(0.0,leaf2),
        Pair(1.0,leaf3)))

    pTree1.print()

    val eventXNOT2 = pTree1.evaluateNOT(pTree1.evaluateEvent(Pair("X","2")))
    pTree1.filterCOND(eventXNOT2)
    pTree1.print()

    println()
    println("Testing Conditioning")
    pTree.evaluateCOND(eventY1).print()

    println()
    println("Testing Intervention")
    pTree.evaluateDO(eventY1).print()
}