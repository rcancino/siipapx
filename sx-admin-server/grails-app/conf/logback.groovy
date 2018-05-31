import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{dd-MM-yy HH:mm}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        //'%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)

    logger("org.springframework.security", OFF, ['STDOUT'], false)
    logger("grails.plugin.springsecurity", OFF, ['STDOUT'], false)

    logger("org.pac4j", OFF, ['STDOUT'], false)


    logger("sx.core", DEBUG, ['STDOUT'], false)
    logger("sx.tesoreria", DEBUG, ['STDOUT'], false)
    logger("sx.cxc", DEBUG, ['STDOUT'], false)
    logger("sx.cfdi", DEBUG, ['STDOUT'], false)
    logger("sx.reports", DEBUG, ['STDOUT'], false)
    logger("com.luxsoft.cfdix.v33", DEBUG, ['STDOUT'], false)
    logger("com.luxsoft", DEBUG, ['STDOUT'], false)

}

if (Environment.isDevelopmentMode()) {

    logger("sx.core", DEBUG, ['STDOUT'], false)
    logger("sx.tesoreria", DEBUG, ['STDOUT'], false)
    logger("sx.cxc", DEBUG, ['STDOUT'], false)
    logger("sx.cfdi", DEBUG, ['STDOUT'], false)
    logger("sx.reports", DEBUG, ['STDOUT'], false)
    logger("com.luxsoft.cfdix.v33", DEBUG, ['STDOUT'], false)
    logger("com.luxsoft", DEBUG, ['STDOUT'], false)

}
root(ERROR, ['STDOUT'])








