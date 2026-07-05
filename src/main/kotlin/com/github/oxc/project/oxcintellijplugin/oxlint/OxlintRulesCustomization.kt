package com.github.oxc.project.oxcintellijplugin.oxlint

import com.intellij.openapi.components.BaseState

class OxlintRulesCustomization() :
    BaseState() {

    var autofix: AutofixOption by enum(AutofixOption.UNSET)
    var severity: RuleSeverity by enum(RuleSeverity.UNSET)

    constructor(autofix: AutofixOption, severity: RuleSeverity) : this() {
        this.autofix = autofix
        this.severity = severity
    }
}

enum class AutofixOption {
    DISABLED,
    ENABLED,
    UNSET,
}

enum class RuleSeverity {
    OFF,
    WARN,
    ERROR,
    UNSET;

    fun toLspValue(): String {
        return when (this) {
            OFF -> "off"
            WARN -> "warn"
            ERROR -> "error"
            // This isn't an actual LSP value.
            UNSET -> "unset"
        }
    }
}
