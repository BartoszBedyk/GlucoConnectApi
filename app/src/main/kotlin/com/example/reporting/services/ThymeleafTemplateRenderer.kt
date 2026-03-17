package com.example.reporting.services

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

class ThymeleafTemplateRenderer {
    private val templateEngine: TemplateEngine

    init {
        val templateResolver = ClassLoaderTemplateResolver().apply {
            prefix = "/templates/"
            suffix = ".html"
            characterEncoding = "UTF-8"
            templateMode = TemplateMode.HTML
        }
        templateEngine = TemplateEngine().apply {
            setTemplateResolver(templateResolver)
        }
    }

    fun render(templateName: String, data: Map<String, Any>): String {
        val context = Context().apply {
            setVariables(data)
        }
        return templateEngine.process(templateName, context)
    }
}
