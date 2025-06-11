package com.github.oxc.project.oxcintellijplugin.lsp

import com.github.oxc.project.oxcintellijplugin.OxcBundle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.customization.LspDiagnosticsSupport
import org.eclipse.lsp4j.Diagnostic

@Suppress("UnstableApiUsage")
class OxcLspDiagnosticsSupport : LspDiagnosticsSupport() {

    override fun shouldAskServerForDiagnostics(file: VirtualFile): Boolean {
        return super.shouldAskServerForDiagnostics(file)
    }

    override fun createAnnotation(holder: AnnotationHolder, diagnostic: Diagnostic,
        textRange: TextRange, quickFixes: List<IntentionAction>) {
        thisLogger().debug(
            "Creating annotation from LSP\nMessage: ${diagnostic.message}\nFixes:${
                quickFixes.filter { it.text.isNotBlank() }.joinToString { it.text }
            }")
        super.createAnnotation(holder, diagnostic, textRange, quickFixes)
    }

    override fun getHighlightSeverity(diagnostic: Diagnostic): HighlightSeverity? {
        return super.getHighlightSeverity(diagnostic)
    }

    override fun getMessage(diagnostic: Diagnostic): String {
        thisLogger().debug("Creating message for diagnostic: $diagnostic")
        return "${diagnostic.source}: ${diagnostic.message} ${
            diagnostic.code?.get() ?: OxcBundle.message("oxc.diagnostic.unknown.code")
        }"
    }

    override fun getTooltip(diagnostic: Diagnostic): String {
        thisLogger().debug("Creating tooltip for diagnostic: $diagnostic")
        var rule = diagnostic.code?.get() ?: OxcBundle.message("oxc.diagnostic.unknown.code")
        if (diagnostic.codeDescription?.href != null) {
            rule = "<a href=\"${diagnostic.codeDescription.href}\">${rule}</a>"
        }

        return "${diagnostic.source}: ${diagnostic.message} $rule"
    }

    override fun getSpecialHighlightType(diagnostic: Diagnostic): ProblemHighlightType? {
        return super.getSpecialHighlightType(diagnostic)
    }
}
