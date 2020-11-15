import CausalReasoning.PTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

// FIXME Define Probability Tree Example 3 as class attribute to improve performance of some tests
class PTreeTests {
    @Test
    fun testConstruction1(){
        val leaf10 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","0")),null)
        val leaf11 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","1")),null)
        val leaf12 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","0")),null)
        val leaf13 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","1")),null)

        val pTree =  PTree(transitions = mutableListOf(
                Pair(1.0/9.0,leaf10),
                Pair(2.0/9.0,leaf11),
                Pair(2.0/9.0,leaf12),
                Pair(4.0/9.0,leaf13)))

        assertEquals(pTree.statement, Pair("O","1"))

        assertEquals(pTree.variables.size,4)

        assertTrue(pTree.variables.contains(Pair("Y","0")))
        assertTrue(pTree.variables.contains(Pair("Y","1")))
        assertTrue(pTree.variables.contains(Pair("X","0")))
        assertTrue(pTree.variables.contains(Pair("X","1")))

    }

    @Test
    fun testConstruction2(){
        val leaf20 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf21 = PTree.Node(mutableListOf(Pair("Y","1")),null)
        val leaf22 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf23 = PTree.Node(mutableListOf(Pair("Y","1")),null)

        val leaf24 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(1.0/3.0,leaf20),Pair(2.0/3.0,leaf21)))
        val leaf25 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/3.0,leaf23)))

        val pTree =  PTree(transitions = mutableListOf(
                Pair(1.0/3.0,leaf24),
                Pair(2.0/3.0,leaf25)))

        assertEquals(pTree.statement, Pair("O","1"))

        assertEquals(pTree.variables.size,4)

        assertTrue(pTree.variables.contains(Pair("Y","0")))
        assertTrue(pTree.variables.contains(Pair("Y","1")))
        assertTrue(pTree.variables.contains(Pair("X","0")))
        assertTrue(pTree.variables.contains(Pair("X","1")))
    }

    @Test
    fun testConstruction3(){
        // Example 3
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

        val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf38),Pair(2.0/5.0,leaf39)))
        val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf41),Pair(2.0/3.0,leaf40)))

        val pTree =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertEquals(pTree.statement, Pair("O","1"))

        assertEquals(pTree.variables.size,6)

        assertTrue(pTree.variables.contains(Pair("Y","0")))
        assertTrue(pTree.variables.contains(Pair("Y","1")))
        assertTrue(pTree.variables.contains(Pair("X","0")))
        assertTrue(pTree.variables.contains(Pair("X","1")))
        assertTrue(pTree.variables.contains(Pair("Z","0")))
        assertTrue(pTree.variables.contains(Pair("Z","1")))
    }

    @Test
    fun testTransitions(){
        val leaf20 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf21 = PTree.Node(mutableListOf(Pair("Y","1")),null)
        val leaf22 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf23 = PTree.Node(mutableListOf(Pair("Y","1")),null)

        // Transition probabilities must sum up to one (testing case more than 1)
        assertFails { val leaf24 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(2.0/3.0,leaf20),Pair(2.0/3.0,leaf21))) }

        // Transition probabilities must sum up to one (testing case less than 1)
        assertFails { val leaf25 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/5.0,leaf23))) }

        val leaf24 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(2.0/3.0,leaf20),Pair(1.0/3.0,leaf21)))
        val leaf25 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/3.0,leaf23)))

        // Transition probabilities must sum up to one (testing case more than 1)
        assertFails {
            val pTree = PTree(transitions = mutableListOf(
                    Pair(2.0 / 3.0, leaf24),
                    Pair(2.0 / 3.0, leaf25)))
        }

        // Transition probabilities must sum up to one (testing case less than 1)
        assertFails {
            val pTree = PTree(transitions = mutableListOf(
                    Pair(2.0 / 5.0, leaf24),
                    Pair(2.0 / 3.0, leaf25)))
        }
    }

    @Test
    fun testTreeIDs(){
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

        val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf38),Pair(2.0/5.0,leaf39)))
        val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf41),Pair(2.0/3.0,leaf40)))

        val pTree =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertEquals(pTree.rootID,"01")
        var i=0
        pTree.transitions.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, pTree.rootID + ".$i")
            else
                assertEquals(it.second.nodeID, pTree.rootID + ".$i.")
            i++
        }

        i=0
        leaf42.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf42.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf42.nodeID + "$i.")
            i++
        }

        i=0
        leaf43.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf43.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf43.nodeID + "$i.")
            i++
        }

        i=0
        leaf38.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf38.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf38.nodeID + "$i.")
            i++
        }

        i=0
        leaf39.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf39.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf39.nodeID + "$i.")
            i++
        }

        i=0
        leaf40.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf40.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf40.nodeID + "$i.")
            i++
        }

        i=0
        leaf41.transitions?.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, leaf41.nodeID + "$i")
            else
                assertEquals(it.second.nodeID, leaf41.nodeID + "$i.")
            i++
        }
    }

    @Test
    fun testEvaluateEvent(){
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

        val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf38),Pair(2.0/5.0,leaf39)))
        val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf41),Pair(2.0/3.0,leaf40)))

        val pTree =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        val rootProposition = Pair("O","1")
        val minCut1 = pTree.evaluateEvent(rootProposition)

        println("True Min-Cut of $rootProposition is ${minCut1.first}")
        println("False Min-Cut of $rootProposition is ${minCut1.second}")

        val proposition = Pair("Y","1")
        val minCut2 = pTree.evaluateEvent(proposition)

        println("True Min-Cut of $proposition is ${minCut2.first}")
        println("False Min-Cut of $proposition is ${minCut2.second}")
    }
}