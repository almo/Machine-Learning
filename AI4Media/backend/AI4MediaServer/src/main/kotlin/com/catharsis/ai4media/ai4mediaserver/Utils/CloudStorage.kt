import com.google.auth.ServiceAccountSigner
import com.google.auth.oauth2.UserCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.concurrent.TimeUnit

// 1. Make this a Singleton service. Instantiate it ONCE when Ktor starts.
object ImageUrlSignerService{
   private val storage: Storage = StorageOptions.getDefaultInstance().service

    // 2. Use a suspend function and Dispatchers.IO for Ktor thread safety
    suspend fun generateSignedImageUrl(imageUri: String): String = withContext(Dispatchers.IO) {
        
        val (bucketName, objectName) = extractBucketAndObject(imageUri)

        // 3. Reuse credentials straight from the Storage client
        val credentials = storage.options.credentials

        // Local user credentials cannot sign URLs directly. Return raw URI.
        if (credentials is UserCredentials) {
            return@withContext imageUri
        }

        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build()

        val signOptions = mutableListOf(Storage.SignUrlOption.withV4Signature())
        if (credentials is ServiceAccountSigner) {
            signOptions.add(Storage.SignUrlOption.signWith(credentials))
        }

        val signedUrl = storage.signUrl(
            blobInfo, 
            1, 
            TimeUnit.DAYS, 
            *signOptions.toTypedArray()
        )

        signedUrl.toString()
    }

    // 4. More robust URI parsing
    private fun extractBucketAndObject(imageUri: String): Pair<String, String> {
        // Handle standard GCS format: "gs://bucket-name/path/to/image.png"
        if (imageUri.startsWith("gs://")) {
            val uri = URI(imageUri)
            val bucket = uri.authority ?: throw IllegalArgumentException("Invalid URI: Missing bucket.")
            val obj = uri.path?.removePrefix("/") ?: throw IllegalArgumentException("Invalid URI: Missing object path.")
            return Pair(bucket, obj)
        }

        // Fallback for your original custom format: "bucket-name/path/to/image.png"
        val uri = URI(imageUri)
        val path = uri.path?.removePrefix("/") ?: throw IllegalArgumentException("Invalid URI format.")

        if (!path.contains("/")) {
            throw IllegalArgumentException("Invalid URI: Missing object name.")
        }

        val bucketName = path.substringBefore("/")
        val objectName = path.substringAfter("/")
        
        return Pair(bucketName, objectName)
    }
}