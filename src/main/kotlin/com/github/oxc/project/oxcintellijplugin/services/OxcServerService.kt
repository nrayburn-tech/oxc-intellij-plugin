@file:Suppress("UnstableApiUsage")

package com.github.oxc.project.oxcintellijplugin.services

import com.github.oxc.project.oxcintellijplugin.OxcBundle
import com.github.oxc.project.oxcintellijplugin.OxcPackage
import com.github.oxc.project.oxcintellijplugin.extensions.findNearestOxcConfig
import com.github.oxc.project.oxcintellijplugin.extensions.findNearestPackageJson
import com.github.oxc.project.oxcintellijplugin.lsp.OxcLspServerDescriptor
import com.github.oxc.project.oxcintellijplugin.lsp.OxcLspServerSupportProvider
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.customization.LspIntentionAction
import com.intellij.platform.lsp.util.getLsp4jRange
import java.io.File
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeActionTriggerKind

@Service(Service.Level.PROJECT)
class OxcServerService(private val project: Project) {
    private val groupId = "Oxc"

    companion object {
        fun getInstance(project: Project): OxcServerService = project.getService(OxcServerService::class.java)
    }

    private fun getServer(file: VirtualFile) =
        LspServerManager.getInstance(project).getServersForProvider(OxcLspServerSupportProvider::class.java)
            .firstOrNull { server -> server.descriptor.isSupportedFile(file) }

    suspend fun fixAll(document: Document) {
        val manager = FileDocumentManager.getInstance()
        val file = manager.getFile(document) ?: return

        fixAll(file, document)
    }

    suspend fun fixAll(file: VirtualFile, document: Document) {
        val server = getServer(file) ?: return

        val commandName = OxcBundle.message("oxc.run.quickfix")

        val codeActionParams = CodeActionParams(server.getDocumentIdentifier(file),
            getLsp4jRange(document, 0, document.textLength),
            CodeActionContext().apply {
                diagnostics = emptyList()
                only = listOf("source.fixAll.oxc")
                triggerKind = CodeActionTriggerKind.Automatic
            })

        val codeActionResults = server.sendRequest { it.textDocumentService.codeAction(codeActionParams) }

        WriteCommandAction.runWriteCommandAction(project, commandName, groupId, {
            codeActionResults?.forEach {
                // Only apply preferred actions which contain real fixes.
                // non-preferred options contain fixes such as disable-next-line.
                if (it.isRight && it.right.isPreferred) {
                    val action = LspIntentionAction(server, it.right)
                    if (action.isAvailable()) {
                        action.invoke(null)
                    }
                }
            }
        })
    }

    fun createLspServerDescriptor(project: Project, file: VirtualFile): OxcLspServerDescriptor? {
        val oxc = OxcPackage(project)
        if (!oxc.isEnabled()) {
            return null
        }
        val configPath = oxc.configPath()
        val executable = oxc.binaryPath(file) ?: return null

        val projectRootDir = project.guessProjectDir() ?: return null
        val root: VirtualFile
        if (configPath?.isNotEmpty() == true) {
            val configVirtualFile = VirtualFileManager.getInstance().findFileByNioPath(File(configPath).toPath()) ?: return null
            root = configVirtualFile.findNearestPackageJson(projectRootDir)?.parent ?: return null
        } else {
            root = file.findNearestOxcConfig(projectRootDir)?.parent ?: return null
        }

        return OxcLspServerDescriptor(project, root, executable)
    }

    fun restartServer() {
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(OxcLspServerSupportProvider::class.java)
    }

    fun ensureServerStarted(file: VirtualFile) {
        createLspServerDescriptor(project, file)?.let {
            LspServerManager.getInstance(project)
                .ensureServerStarted(OxcLspServerSupportProvider::class.java, it)
        }
    }

    fun stopServer() {
        LspServerManager.getInstance(project).stopServers(OxcLspServerSupportProvider::class.java)
    }

    fun notifyRestart() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Oxc")
            .createNotification(
                OxcBundle.message("oxc.language.server.restarted"),
                "",
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}
