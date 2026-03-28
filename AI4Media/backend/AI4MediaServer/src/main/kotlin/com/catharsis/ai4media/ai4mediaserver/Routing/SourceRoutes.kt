package com.catharsis.ai4media.ai4mediaserver

import com.catharsis.ai4media.ai4mediaserver.model.SourceRequest
import com.catharsis.ai4media.ai4mediaserver.persistence.DatastoreSourceRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSourceRouting() {
    routing {
        authenticate("firebase-auth") {
            route("/api/sources") {

                // GET /api/sources - Get all sources for the user
                get {
                    try {
                        val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                        call.application.log.info("User ${user.userId} requested their sources")
                        val sources = DatastoreSourceRepository.findByUserId(user.userId)
                        call.application.log.info("Found ${sources.size} sources")
                        call.respond(sources)
                    } catch (e: Exception) {
                        call.application.log.error("Error fetching sources", e)
                        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    }
                }

                // POST /api/sources - Create a new source
                post {
                    try {
                        val user = call.principal<User>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                        val sourceRequest = call.receive<SourceRequest>()
                        call.application.log.info("User ${user.userId} creating new source: ${sourceRequest.name}")
                        val newSource = DatastoreSourceRepository.create(sourceRequest, user.userId)
                        call.respond(HttpStatusCode.Created, newSource)
                    } catch (e: Exception) {
                        call.application.log.error("Error creating source", e)
                        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    }
                }

                // GET /api/sources/{id} - Get a single source
                get("/{id}") {
                    try {
                        val user = call.principal<User>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing source ID")
    
                        call.application.log.info("User ${user.userId} requested source $id")
                        val source = DatastoreSourceRepository.findById(id)
                        if (source == null) {
                            call.application.log.warn("Source $id not found for user ${user.userId}")
                            return@get call.respond(HttpStatusCode.NotFound)
                        }
    
                        if (source.userId != user.userId) {
                            call.application.log.warn("User ${user.userId} attempted to access forbidden source $id")
                            return@get call.respond(HttpStatusCode.Forbidden, "You don't have permission to view this source")
                        }
    
                        call.respond(HttpStatusCode.OK, source)
                    } catch (e: Exception) {
                        call.application.log.error("Error fetching source", e)
                        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    }
                }

                // PUT /api/sources/{id} - Update a source
                put("/{id}") {
                    try {
                        val user = call.principal<User>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                        val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing source ID")
                        val sourceRequest = call.receive<SourceRequest>()
    
                        call.application.log.info("User ${user.userId} attempting to update source $id")
                        val existingSource = DatastoreSourceRepository.findById(id)
                        if (existingSource == null) {
                            call.application.log.warn("Source $id not found for update by user ${user.userId}")
                            return@put call.respond(HttpStatusCode.NotFound)
                        }
    
                        if (existingSource.userId != user.userId) {
                            call.application.log.warn("User ${user.userId} attempted to update forbidden source $id")
                            return@put call.respond(HttpStatusCode.Forbidden, "You don't have permission to update this source")
                        }
    
                        val updatedSource = DatastoreSourceRepository.update(id, sourceRequest)
                        call.application.log.info("Successfully updated source $id")
                        call.respond(HttpStatusCode.OK, updatedSource)
                    } catch (e: Exception) {
                        call.application.log.error("Error updating source", e)
                        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    }
                }

                // DELETE /api/sources/{id} - Delete a source
                delete("/{id}") {
                    try {
                        val user = call.principal<User>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                        val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing source ID")
    
                        call.application.log.info("User ${user.userId} attempting to delete source $id")
                        val existingSource = DatastoreSourceRepository.findById(id)
                        if (existingSource == null) {
                            call.application.log.warn("Source $id not found for deletion by user ${user.userId}")
                            return@delete call.respond(HttpStatusCode.NotFound)
                        }
    
                        if (existingSource.userId != user.userId) {
                            call.application.log.warn("User ${user.userId} attempted to delete forbidden source $id")
                            return@delete call.respond(HttpStatusCode.Forbidden, "You don't have permission to delete this source")
                        }
    
                        DatastoreSourceRepository.delete(id)
                        call.application.log.info("Successfully deleted source $id")
                        call.respond(HttpStatusCode.NoContent)
                    } catch (e: Exception) {
                        call.application.log.error("Error deleting source", e)
                        call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    }
                }
            }
        }
    }
}