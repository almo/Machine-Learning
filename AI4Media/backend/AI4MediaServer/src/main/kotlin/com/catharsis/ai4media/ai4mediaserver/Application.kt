package com.catharsis.ai4media.ai4mediaserver

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    routing {
        get("/") {
             try {
                // Load the native OR-Tools library.
                Loader.loadNativeLibraries()

                // Create the linear solver with the GLOP backend.
                val solver: MPSolver = MPSolver.createSolver("GLOP")

                // Create the variables x and y.
                val x = solver.makeNumVar(0.0, 1.0, "x")
                val y = solver.makeNumVar(0.0, 2.0, "y")

                // Create a linear constraint, 0 <= x + y <= 2.
                val ct = solver.makeConstraint(0.0, 2.0, "ct")
                ct.setCoefficient(x, 1.0)
                ct.setCoefficient(y, 1.0)

                // Create the objective function, 3 * x + y.
                val objective = solver.objective()
                objective.setCoefficient(x, 3.0)
                objective.setCoefficient(y, 1.0)
                objective.setMaximization()

                // Solve the problem AND check the result.
                val resultStatus = solver.solve()

                // Create the response text based on the solver's status.
                val responseText = if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
                    """
                    Solution found! ✅
                    Objective value = ${objective.value()}
                    x = ${x.solutionValue()}
                    y = ${y.solutionValue()}
                    """.trimIndent()
                } else {
                    """
                    Could not solve the problem. 😢
                    Solver status: $resultStatus
                    """.trimIndent()
                }

                call.respondText(responseText)

            } catch (e: Exception) {
                call.respondText("An error occurred: ${e.message}")
            }
        }
    }
}
