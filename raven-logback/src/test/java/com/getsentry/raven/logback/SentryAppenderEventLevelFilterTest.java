package com.getsentry.raven.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Context;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import com.getsentry.raven.Raven;
import com.getsentry.raven.event.Event;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Felipe G Almeida
 */
public class SentryAppenderEventLevelFilterTest {
    @Tested
    private SentryAppender sentryAppender = null;
    @Injectable
    private Raven mockRaven = null;
    @Injectable
    private Context mockContext = null;

    @BeforeMethod
    public void setUp() throws Exception {
        new MockUpStatusPrinter();
        sentryAppender = new SentryAppender(mockRaven);
        sentryAppender.setContext(mockContext);
    }

    @DataProvider(name = "levels")
    private Object[][] levelConversions() {
        return new Object[][]{
                {"ALL", 5},
                {"TRACE", 5},
                {"DEBUG", 4},
                {"INFO", 3},
                {"WARN", 2},
                {"ERROR", 1},
                {"error", 1},
                {"xxx", 2},
                {null, 2}};
    }

    @Test(dataProvider = "levels")
    public void testLevelFilter(final String minLevel, final Integer expectedEvents) throws Exception {
        sentryAppender.setMinLevel(minLevel);
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.TRACE, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.DEBUG, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.INFO, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.WARN, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.ERROR, null, null, null).getMockInstance());

        new Verifications() {{
            mockRaven.sendEvent((Event) any);
            minTimes = expectedEvents;
            maxTimes = expectedEvents;
        }};
    }

    @Test
    public void testDefaultLevelFilter() throws Exception {
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.TRACE, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.DEBUG, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.INFO, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.WARN, null, null, null).getMockInstance());
        sentryAppender.append(new MockUpLoggingEvent(null, null, Level.ERROR, null, null, null).getMockInstance());

        new Verifications() {{
            mockRaven.sendEvent((Event) any);
            minTimes = 2;
            maxTimes = 2;
        }};
    }

}
