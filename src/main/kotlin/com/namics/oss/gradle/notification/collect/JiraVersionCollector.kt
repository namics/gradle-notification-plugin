package com.namics.oss.gradle.notification.collect

import com.namics.oss.gradle.notification.Http
import com.namics.oss.gradle.notification.Property
import com.namics.oss.gradle.notification.Property.JiraVersionProperty
import com.namics.oss.gradle.notification.utils.getPathElement
import com.namics.oss.gradle.notification.utils.getPathString

class JiraVersionCollector(
    override var propertyKey: String = "jiraVersion",
    var host: String? = null,
    var version: String? = null,
    var authHeader: String? = null,
    var queryString: String = "/rest/api/2/search?jql=fixVersion=$version&fields=id,summary,issuetype",
    var issuesPath: String = "issues",
    var keyPath: String = "key",
    var summaryPath: String = "fields.summary",
    var typePath: String = "fields.issuetype.name",
    override var overwrite: Boolean = false
) : Collector {

    override fun collectProperty(): Property {
        requireNotNull(version)
        val uri = requireNotNull(host) + queryString
        val jsonElement = Http(authHeader).getJson(uri)
        val jsonIssues = jsonElement.jsonObject.getPathElement(issuesPath)
        val jiraIssues = jsonIssues?.jsonArray?.map {
            mapOf(
                "issueKey" to (it.jsonObject.getPathString(keyPath) ?: ""),
                "summary" to (it.jsonObject.getPathString(summaryPath) ?: ""),
                "type" to (it.jsonObject.getPathString(typePath) ?: "")
            )
        }?.groupBy { it.get("type") ?: "" }?.toMap()
        return JiraVersionProperty(key = propertyKey, value = jiraIssues ?: emptyMap())
    }
}