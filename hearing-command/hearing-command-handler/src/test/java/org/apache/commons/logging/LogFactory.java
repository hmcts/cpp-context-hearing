package org.apache.commons.logging;

public class LogFactory {

    private static final Log NO_OP = new Log() {
        public boolean isDebugEnabled() { return false; }
        public boolean isErrorEnabled() { return false; }
        public boolean isFatalEnabled() { return false; }
        public boolean isInfoEnabled() { return false; }
        public boolean isTraceEnabled() { return false; }
        public boolean isWarnEnabled() { return false; }
        public void debug(Object message) {}
        public void debug(Object message, Throwable t) {}
        public void error(Object message) {}
        public void error(Object message, Throwable t) {}
        public void fatal(Object message) {}
        public void fatal(Object message, Throwable t) {}
        public void info(Object message) {}
        public void info(Object message, Throwable t) {}
        public void trace(Object message) {}
        public void trace(Object message, Throwable t) {}
        public void warn(Object message) {}
        public void warn(Object message, Throwable t) {}
    };

    public static Log getLog(final Class<?> clazz) {
        return NO_OP;
    }

    public static Log getLog(final String name) {
        return NO_OP;
    }
}
