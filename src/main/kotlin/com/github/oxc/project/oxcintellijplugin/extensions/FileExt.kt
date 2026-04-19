package com.github.oxc.project.oxcintellijplugin.extensions

import com.github.oxc.project.oxcintellijplugin.oxfmt.OxfmtPackage
import com.github.oxc.project.oxcintellijplugin.oxlint.OxlintPackage
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

/**
 * Find the nearest file that satisfies the predicate.
 * If `root` is not null, stops finding at the specified directory.
 */
private fun VirtualFile.findNearestFile(
    predicate: (file: VirtualFile) -> Boolean,
    root: VirtualFile? = null,
): VirtualFile? {
    var cur = this.parent
    while (cur != null && VfsUtil.isUnder(cur, mutableSetOf(root))) {
        val f = cur.children.find(predicate)
        if (f != null) {
            return f
        }
        cur = cur.parent
    }
    return null
}

private fun _isOxlintJsonConfigFile(file: VirtualFile): Boolean {
    return OxlintPackage.configValidJsonExtensions.map { "${OxlintPackage.CONFIG_NAME}.$it" }.contains(file.name)
}

fun VirtualFile.isOxlintJsonConfigFile(): Boolean = _isOxlintJsonConfigFile(this)

private fun _isOxlintConfigFile(file: VirtualFile): Boolean {
    return _isOxlintJsonConfigFile(file) || file.name == OxlintPackage.CONFIG_TS_NAME
}

fun VirtualFile.isOxlintConfigFile(): Boolean = _isOxlintConfigFile(this)

private fun _isOxfmtJsonConfigFile(file: VirtualFile): Boolean {
    return OxfmtPackage.CONFIG_VALID_JSON_EXTENSIONS.map { "${OxfmtPackage.CONFIG_NAME}.$it" }.contains(file.name)
}

fun VirtualFile.isOxfmtJsonConfigFile(): Boolean = _isOxfmtJsonConfigFile(this)

private fun _isOxfmtConfigFile(file: VirtualFile): Boolean {
    return _isOxfmtJsonConfigFile(file) || file.name == OxfmtPackage.CONFIG_TS_NAME
}

fun VirtualFile.isOxfmtConfigFile(): Boolean = _isOxfmtConfigFile(this)

private fun _isViteConfigFile(file: VirtualFile): Boolean {
    return file.name == "vite.config.js" || file.name == "vite.config.ts"
}

fun VirtualFile.isViteConfigFile(): Boolean = _isViteConfigFile(this)

fun VirtualFile.findNearestOxfmtJsonConfigFile(root: VirtualFile? = null): VirtualFile? {
    return this.findNearestFile({
        _isOxfmtJsonConfigFile(it)
    }, root)
}
