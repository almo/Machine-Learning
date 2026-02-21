import com.google.cloud.tasks.v2.*
import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import java.nio.charset.StandardCharsets
import java.time.Instant

/**
 * Utility object for interacting with Google Cloud Tasks.
 */
object CloudTasks {

    /**
     * Creates an HTTP Task and adds it to the specified Cloud Tasks queue.
     *
     * @param projectId The Google Cloud Project ID.
     * @param locationId The Cloud region (e.g., "us-central1").
     * @param queueId The ID of the Cloud Tasks queue.
     * @param url The target URL for the HTTP request.
     * @param scheduleTime The time at which the task should be executed.
     */
    fun createHttpTask(
            projectId: String,
            locationId: String,
            queueId: String,
            url: String,
            scheduleTime: Instant
    ) {
        // Initialize the Cloud Tasks client
        val client = CloudTasksClient.create()
        // Construct the fully qualified queue name
        val queuePath = QueueName.of(projectId, locationId, queueId).toString()

        // Create the payload (JSON)
        val jsonPayload = """{"url": "$url"}"""
        val body = ByteString.copyFrom(jsonPayload, StandardCharsets.UTF_8)

        // Build the HTTP Request object for the task
        val httpRequest =
                HttpRequest.newBuilder()
                        .setHttpMethod(HttpMethod.POST)
                        .setUrl(url)
                        .setBody(body)
                        .putHeaders("Content-Type", "application/json")
                        .build()

        // Convert Java Instant to Protobuf Timestamp
        val protoTimestamp =
                Timestamp.newBuilder()
                        .setSeconds(scheduleTime.epochSecond)
                        .setNanos(scheduleTime.nano)
                        .build()

        // Build the Task object with the schedule time
        val task =
                Task.newBuilder()
                        .setHttpRequest(httpRequest)
                        .setScheduleTime(protoTimestamp)
                        .build()

        // Send the task creation request
        val response = client.createTask(queuePath, task)
        println("Created task: ${response.name}")
    }
}
