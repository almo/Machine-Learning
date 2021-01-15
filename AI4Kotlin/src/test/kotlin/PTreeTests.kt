import CausalReasoning.PTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

// FIXME Define Probability Tree Example 3 as class attribute to improve performance of some tests
class PTreeTests {
    @Test
    fun testConstruction1(){
        val leaf10 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","0")),mutableListOf())
        val leaf11 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","1")),mutableListOf())
        val leaf12 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","0")),mutableListOf())
        val leaf13 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","1")),mutableListOf())

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
        val leaf20 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf21 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf22 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf23 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

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
        val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

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
        val leaf20 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf21 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf22 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf23 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

        // Transition probabilities must sum up to one (testing case more than 1)
        assertFails { PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(2.0/3.0,leaf20),Pair(2.0/3.0,leaf21))) }

        // Transition probabilities must sum up to one (testing case less than 1)
        assertFails {  PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/5.0,leaf23))) }

        val leaf24 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(2.0/3.0,leaf20),Pair(1.0/3.0,leaf21)))
        val leaf25 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf22),Pair(2.0/3.0,leaf23)))

        // Transition probabilities must sum up to one (testing case more than 1)
        assertFails {
             PTree(transitions = mutableListOf(
                    Pair(2.0 / 3.0, leaf24),
                    Pair(2.0 / 3.0, leaf25)))
        }

        // Transition probabilities must sum up to one (testing case less than 1)
        assertFails {
             PTree(transitions = mutableListOf(
                    Pair(2.0 / 5.0, leaf24),
                    Pair(2.0 / 3.0, leaf25)))
        }
    }

    @Test
    fun testTreeIDs(){
        val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

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

            assertEquals(it.second.nodeID, pTree.rootID + ".$i")

            i++
        }

        i=0
        leaf42.transitions.forEach {

            assertEquals(it.second.nodeID, leaf42.nodeID + ".$i")

            i++
        }

        i=0
        leaf43.transitions.forEach {

            assertEquals(it.second.nodeID, leaf43.nodeID + ".$i")

            i++
        }

        i=0
        leaf38.transitions.forEach {

            assertEquals(it.second.nodeID, leaf38.nodeID + ".$i")

            i++
        }

        i=0
        leaf39.transitions.forEach {

            assertEquals(it.second.nodeID, leaf39.nodeID + ".$i")

            i++
        }

        i=0
        leaf40.transitions.forEach {

            assertEquals(it.second.nodeID, leaf40.nodeID + ".$i")

            i++
        }

        i=0
        leaf41.transitions.forEach {

            assertEquals(it.second.nodeID, leaf41.nodeID + ".$i")

            i++
        }
    }

    @Test
    fun testEvaluateEvent(){
        val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

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

    @Test
    fun testEqual(){
        val leaf10 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","0")),mutableListOf())
        val leaf11 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","1")),mutableListOf())
        val leaf12 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","0")),mutableListOf())
        val leaf13 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","1")),mutableListOf())

        val pTree1 =  PTree(transitions = mutableListOf(
                Pair(1.0/9.0,leaf10),
                Pair(2.0/9.0,leaf11),
                Pair(2.0/9.0,leaf12),
                Pair(4.0/9.0,leaf13)))

        assertTrue { pTree1.equal(pTree1) }

        val pTree2 = pTree1.copy()

        assertTrue { pTree2.equal(pTree2) }

        assertTrue { pTree1.equal(pTree2) }
        assertTrue { pTree2.equal(pTree1) }

        val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

        val leaf38 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf(Pair(0.5,leaf30),Pair(0.5,leaf31)))
        val leaf39 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf(Pair(2.0/3.0,leaf32),Pair(1.0/3.0,leaf33)))
        val leaf40 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf(Pair(0.5,leaf34),Pair(0.5,leaf35)))
        val leaf41 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf(Pair(4.0/5.0,leaf36),Pair(1.0/5.0,leaf37)))

        val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf38),Pair(2.0/5.0,leaf39)))
        val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf41),Pair(2.0/3.0,leaf40)))

        val pTree3 =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertTrue { pTree3.equal(pTree3) }
        assertTrue { !pTree1.equal(pTree3) }
        assertTrue { !pTree2.equal(pTree3) }
        assertTrue { !pTree3.equal(pTree1) }
        assertTrue { !pTree3.equal(pTree2) }

        val leaf030 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf031 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf032 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf033 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf034 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf035 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf036 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf037 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf0371 = PTree.Node(mutableListOf(Pair("W","0")),mutableListOf())
        val leaf0372 = PTree.Node(mutableListOf(Pair("W","1")),mutableListOf())

        val leaf038 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf(Pair(0.4,leaf030),Pair(0.5,leaf031),Pair(0.1,leaf0371)))
        val leaf039 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf(Pair(2.0/3.0,leaf032),Pair(1.0/3.0,leaf033)))
        val leaf040 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf(Pair(1.0/3.0,leaf034),Pair(1.0/3.0,leaf035),Pair(1.0/3.0,leaf0372)))
        val leaf041 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf(Pair(4.0/5.0,leaf036),Pair(1.0/5.0,leaf037)))

        val leaf042 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf038),Pair(2.0/5.0,leaf039)))
        val leaf043 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf041),Pair(2.0/3.0,leaf040)))

        val pTree4 =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf042),
                Pair(6.0/11.0,leaf043)))

        assertTrue {  pTree4.equal(pTree4) }

        assertTrue { !pTree1.equal(pTree4) }
        assertTrue { !pTree2.equal(pTree4) }
        assertTrue { !pTree3.equal(pTree4) }

        assertTrue { !pTree4.equal(pTree1) }
        assertTrue { !pTree4.equal(pTree2) }
        assertTrue { !pTree4.equal(pTree3) }
    }

    @Test
    fun testCopy(){
        val leaf10 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","0")),mutableListOf())
        val leaf11 = PTree.Node(mutableListOf(Pair("X","0"),Pair("Y","1")),mutableListOf())
        val leaf12 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","0")),mutableListOf())
        val leaf13 = PTree.Node(mutableListOf(Pair("X","1"),Pair("Y","1")),mutableListOf())

        val pTree1 =  PTree(transitions = mutableListOf(
                Pair(1.0/9.0,leaf10),
                Pair(2.0/9.0,leaf11),
                Pair(2.0/9.0,leaf12),
                Pair(4.0/9.0,leaf13)))

        assertTrue { pTree1.equal(pTree1) }

        val pTree2 = pTree1.copy()

        assertTrue { pTree2.equal(pTree2) }

        assertTrue { pTree1.equal(pTree2) }
        assertTrue { pTree2.equal(pTree1) }

        val leaf30 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf31 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf32 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf())
        val leaf33 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf())
        val leaf34 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf35 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())
        val leaf36 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf())
        val leaf37 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf())

        val leaf38 = PTree.Node(mutableListOf(Pair("Y","0")),mutableListOf(Pair(0.5,leaf30),Pair(0.5,leaf31)))
        val leaf39 = PTree.Node(mutableListOf(Pair("Y","1")),mutableListOf(Pair(2.0/3.0,leaf32),Pair(1.0/3.0,leaf33)))
        val leaf40 = PTree.Node(mutableListOf(Pair("Z","0")),mutableListOf(Pair(0.5,leaf34),Pair(0.5,leaf35)))
        val leaf41 = PTree.Node(mutableListOf(Pair("Z","1")),mutableListOf(Pair(4.0/5.0,leaf36),Pair(1.0/5.0,leaf37)))

        val leaf42 = PTree.Node(mutableListOf(Pair("X","0")),mutableListOf(Pair(3.0/5.0,leaf38),Pair(2.0/5.0,leaf39)))
        val leaf43 = PTree.Node(mutableListOf(Pair("X","1")),mutableListOf(Pair(1.0/3.0,leaf41),Pair(2.0/3.0,leaf40)))

        val pTree3 =  PTree(transitions = mutableListOf(
                Pair(5.0/11.0,leaf42),
                Pair(6.0/11.0,leaf43)))

        assertTrue { pTree3.equal(pTree3) }
        assertTrue { !pTree1.equal(pTree3) }
        assertTrue { !pTree2.equal(pTree3) }
        assertTrue { !pTree3.equal(pTree1) }
        assertTrue { !pTree3.equal(pTree2) }

        val pTree4 = pTree3.copy()

        assertTrue { pTree4.equal(pTree4) }
        assertTrue { !pTree4.equal(pTree1) }
        assertTrue { !pTree4.equal(pTree2) }
        assertTrue { pTree3.equal(pTree4) }
        assertTrue { pTree4.equal(pTree3) }

    }
}