package com.catharsis.ai4media.ai4mediaserver.content

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.meta
import it.skrape.selects.html5.p
import it.skrape.selects.html5.title

object NewsScraper {

    fun scrape(urlToScrape: String): ScrapedContent? {
        return try {
            skrape(HttpFetcher) {
                request {
                    url = urlToScrape
                    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) KtorScraper/1.0"
                }
                response {
                    htmlDocument {
                        val scrapedTitle = title { findAll { firstOrNull()?.text ?: "" } }
                        
                        // Extraemos Meta Description u OpenGraph Description
                        var scrapedSummary = meta {
                            withAttributes = listOf("property" to "og:description")
                            findAll { firstOrNull()?.attribute("content") ?: "" }
                        }
                        
                        if (scrapedSummary.isBlank()) {
                            scrapedSummary = meta {
                                withAttributes = listOf("name" to "description")
                                findAll { firstOrNull()?.attribute("content") ?: "" }
                            }
                        }
                        
                        if (scrapedSummary.isBlank()) {
                            scrapedSummary = p { findAll { firstOrNull()?.text ?: "" } }
                        }

                        // Extraemos la imagen OpenGraph
                        val scrapedImage = meta {
                            withAttributes = listOf("property" to "og:image")
                            findAll { firstOrNull()?.attribute("content") ?: "" }
                        }
                        
                        // Extract the main body of the news by joining all paragraph texts
                        val scrapedBody = selection("article, .main-content, main") {
                            findAll { filter { it.text.isNotBlank() }.joinToString("\n\n") { it.text }.take(3000) }
                        }.ifBlank { 
                            // Fallback to all paragraphs joined if no main container is found
                            p { findAll { joinToString("\n") { it.text } } }
                        }

                        ScrapedContent(
                            title = scrapedTitle,
                            summary = scrapedSummary,
                            body = scrapedBody,
                            imageUrl = if (scrapedImage.startsWith("http")) scrapedImage else null,
                            originalUrl = urlToScrape
                        )
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class ScrapedContent(
    val title: String,
    val summary: String,
    val body: String,
    val imageUrl: String?,
    val originalUrl: String
)