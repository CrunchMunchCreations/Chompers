package xyz.bluspring.sprinkles.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

// Custom highlighter class so Portainer's INFO level is actually readable
class LogbackHighlighting : ForegroundCompositeConverterBase<ILoggingEvent>() {
    override fun getForegroundColorCode(event: ILoggingEvent): String {
        return when (event.level.levelInt) {
            Level.INFO_INT -> ANSIConstants.GREEN_FG
            Level.WARN_INT -> ANSIConstants.RED_FG
            Level.ERROR_INT -> ANSIConstants.BOLD + ANSIConstants.RED_FG
            else -> ANSIConstants.DEFAULT_FG
        }
    }
}