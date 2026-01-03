package com.github.oxc.project.oxcintellijplugin.extensions

import com.github.oxc.project.oxcintellijplugin.oxfmt.OxfmtPackage
import com.github.oxc.project.oxcintellijplugin.oxlint.OxlintPackage
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

/**
 * Find the nearest file that satisfies the predicate that is within `root`.
 */
private fun VirtualFile.findNearestFile(
    predicate: (file: VirtualFile) -> Boolean,
    root: VirtualFile,
): VirtualFile? {
    val roots = mutableSetOf(root)
    var cur = this.parent
    while (cur != null && VfsUtil.isUnder(cur, roots)) {
        val f = cur.children.find(predicate)
        if (f != null) {
            return f
        }
        cur = cur.parent
    }
    return null
}

fun VirtualFile.isOxlintConfigFile(): Boolean =
    OxlintPackage.configValidExtensions.map { "${OxlintPackage.CONFIG_NAME}.$it" }.contains(this.name)

fun VirtualFile.isOxfmtConfigFile(): Boolean =
    OxfmtPackage.CONFIG_VALID_EXTENSIONS.map { "${OxfmtPackage.CONFIG_NAME}.$it" }.contains(this.name)

fun VirtualFile.isPackageJsonFile(): Boolean = this.name == "package.json"

fun VirtualFile.findNearestOxfmtConfig(root: VirtualFile): VirtualFile? =
    this.findNearestFile({ f -> f.isOxfmtConfigFile() }, root)

fun VirtualFile.findNearestOxlintConfig(root: VirtualFile): VirtualFile? =
    this.findNearestFile({ f -> f.isOxlintConfigFile() }, root)

fun VirtualFile.findNearestPackageJson(root: VirtualFile): VirtualFile? =
    this.findNearestFile({ f -> f.isPackageJsonFile() }, root)
