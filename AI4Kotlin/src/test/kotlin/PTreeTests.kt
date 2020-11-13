import CausalReasoning.PTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PTreeTests {
    @Test
    fun testConstruction1(){
        val leaf10 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","0")),null)
        val leaf11 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","1")),null)
        val leaf12 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","0")),null)
        val leaf13 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","1")),null)

        val ptree1 =  PTree(transitions = mutableListOf(
                Pair(1.0/9.0,leaf10),
                Pair(2.0/9.0,leaf11),
                Pair(2.0/9.0,leaf12),
                Pair(4.0/9.0,leaf13)))

        assertEquals(ptree1.statement, Pair("O","1"))

        assertEquals(ptree1.variables.size,4)

        assertTrue(ptree1.variables.contains(Pair("Y","0")))
        assertTrue(ptree1.variables.contains(Pair("Y","1")))
        assertTrue(ptree1.variables.contains(Pair("X","0")))
        assertTrue(ptree1.variables.contains(Pair("X","1")))

    }

    @Test
    fun testConstruction2(){
        val leaf20 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf21 = PTree.Node(mutableListOf(Pair("Y","1")),null)
        val leaf22 = PTree.Node(mutableListOf(Pair("Y","0")),null)
        val leaf23 = PTree.Node(mutableListOf(Pair("Y","1")),null)

        val leaf24 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(1.0/3.0,leaf20),Pair(2.0/3.0,leaf21)))
        val leaf25 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/3.0,leaf23)))

        val ptree2 =  PTree(transitions = mutableListOf(
                Pair(1.0/3.0,leaf24),
                Pair(2.0/3.0,leaf25)))

        assertEquals(ptree2.statement, Pair("O","1"))

        assertEquals(ptree2.variables.size,4)

        assertTrue(ptree2.variables.contains(Pair("Y","0")))
        assertTrue(ptree2.variables.contains(Pair("Y","1")))
        assertTrue(ptree2.variables.contains(Pair("X","0")))
        assertTrue(ptree2.variables.contains(Pair("X","1")))
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

        val ptree3 =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertEquals(ptree3.statement, Pair("O","1"))

        assertEquals(ptree3.variables.size,6)

        assertTrue(ptree3.variables.contains(Pair("Y","0")))
        assertTrue(ptree3.variables.contains(Pair("Y","1")))
        assertTrue(ptree3.variables.contains(Pair("X","0")))
        assertTrue(ptree3.variables.contains(Pair("X","1")))
        assertTrue(ptree3.variables.contains(Pair("Z","0")))
        assertTrue(ptree3.variables.contains(Pair("Z","1")))
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
            val ptree2 = PTree(transitions = mutableListOf(
                    Pair(2.0 / 3.0, leaf24),
                    Pair(2.0 / 3.0, leaf25)))
        }

        // Transition probabilities must sum up to one (testing case less than 1)
        assertFails {
            val ptree2 = PTree(transitions = mutableListOf(
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

        val ptree3 =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertEquals(ptree3.rootID,"01")
        var i=0
        ptree3.transitions.forEach {
            println(it.second.nodeID)
            if (it.second.isLeaf)
                assertEquals(it.second.nodeID, ptree3.rootID + ".$i")
            else
                assertEquals(it.second.nodeID, ptree3.rootID + ".$i.")
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
}