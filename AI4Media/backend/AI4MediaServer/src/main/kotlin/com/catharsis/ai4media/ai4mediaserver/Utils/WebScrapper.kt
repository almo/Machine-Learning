import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ImageResolver {

    fun getBestImage(url: String): String? {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            // Ejecutar cadena de heurísticas por orden de relevancia
            findOgImage(doc)
                ?: findTwitterImage(doc)
                ?: findJsonLdImage(doc)
                ?: findHighResIcon(doc)
                ?: findMainBodyImage(doc)
        } catch (e: Exception) {
            println("Error al extraer imagen: ${e.message}")
            null
        }
    }

    // 1. Estándar Open Graph
    private fun findOgImage(doc: Document) = doc.selectFirst("meta[property=og:image]")?.attr("abs:content")

    // 2. Estándar Twitter
    private fun findTwitterImage(doc: Document) = doc.selectFirst("meta[name=twitter:image]")?.attr("abs:content")

    // 3. Heurística avanzada: JSON-LD (Datos estructurados)
    private fun findJsonLdImage(doc: Document): String? {
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val content = script.data()
            // Buscamos patrones de URL de imagen dentro del JSON sin necesidad de una librería pesada de parsing
            val match = """"image"\s*:\s*"([^"]+)"""".toRegex().find(content)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    // 4. Heurística de Iconos (favicons de alta resolución)
    private fun findHighResIcon(doc: Document): String? {
        return doc.selectFirst("link[rel=apple-touch-icon]")?.attr("abs:href")
            ?: doc.selectFirst("link[rel=icon][sizes=192x192]")?.attr("abs:href")
    }

    // 5. Heurística de Cuerpo: Filtrado por dimensiones y relevancia
    private fun findMainBodyImage(doc: Document): String? {
        // Priorizamos imágenes dentro de etiquetas de contenido principal
        val mainImages = doc.select("article img, main img, #content img")
        
        return mainImages.asSequence()
            .map { it to it.attr("abs:src") }
            .filter { (_, src) -> 
                src.isNotBlank() && !src.contains("ads") && !src.contains("pixel") 
            }
            .find { (element, _) ->
                // Heurística visual: ¿Tiene atributos de tamaño que sugieran que es grande?
                val width = element.attr("width").toIntOrNull() ?: 0
                val height = element.attr("height").toIntOrNull() ?: 0
                
                // Si no tiene dimensiones, confiamos en que al estar en 'article' es relevante
                (width == 0 || width > 200) && (height == 0 || height > 200)
            }?.second
    }
}