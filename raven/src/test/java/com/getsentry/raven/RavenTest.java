package com.getsentry.raven;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import com.getsentry.raven.connection.Connection;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.helper.EventBuilderHelper;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RavenTest {
    @Tested
    private Raven raven = null;
    @Injectable
    private Connection mockConnection = null;
    @Injectable
    private Event mockEvent = null;
    @Injectable
    private EventBuilderHelper mockEventBuilderHelper = null;

    @Test
    public void testSendEvent() throws Exception {
        raven.sendEvent(mockEvent);

        new Verifications() {{
            mockConnection.send(mockEvent);
        }};
    }

    @Test
    public void testSendEventFailingIsCaught() throws Exception {
        new NonStrictExpectations() {{
            mockConnection.send((Event) any);
            result = new RuntimeException();
        }};

        raven.sendEvent(mockEvent);

        new Verifications() {{
            mockConnection.send(mockEvent);
        }};
    }

    @Test
    public void testSendMessage() throws Exception {
        final String message = "e960981e-656d-4404-9b1d-43b483d3f32c";
        raven.addBuilderHelper(mockEventBuilderHelper);

        raven.sendMessage(message);

        new Verifications() {{
            Event event;
            mockEventBuilderHelper.helpBuildingEvent((EventBuilder) any);
            mockConnection.send(event = withCapture());
            assertThat(event.getLevel(), equalTo(Event.Level.INFO));
            assertThat(event.getMessage(), equalTo(message));
        }};
    }

    @Test
    public void testSendException() throws Exception {
        final String message = "7b61ddb1-eb32-428d-bad9-a7d842605ba7";
        final Exception exception = new Exception(message);
        raven.addBuilderHelper(mockEventBuilderHelper);

        raven.sendException(exception);

        new Verifications() {{
            Event event;
            mockEventBuilderHelper.helpBuildingEvent((EventBuilder) any);
            mockConnection.send(event = withCapture());
            assertThat(event.getLevel(), equalTo(Event.Level.ERROR));
            assertThat(event.getMessage(), equalTo(message));
            assertThat(event.getSentryInterfaces(), hasKey(ExceptionInterface.EXCEPTION_INTERFACE));
        }};
    }

    @Test
    public void testChangeConnection(@Injectable final Connection mockNewConnection) throws Exception {
        raven.setConnection(mockNewConnection);

        raven.sendEvent(mockEvent);

        new Verifications() {{
            mockConnection.send((Event) any);
            times = 0;
            mockNewConnection.send(mockEvent);
        }};
    }

    @Test
    public void testAddRemoveBuilderHelpers(@Injectable final EventBuilderHelper mockBuilderHelper) throws Exception {
        assertThat(raven.getBuilderHelpers(), not(contains(mockBuilderHelper)));

        raven.addBuilderHelper(mockBuilderHelper);
        assertThat(raven.getBuilderHelpers(), contains(mockBuilderHelper));
        raven.removeBuilderHelper(mockBuilderHelper);
        assertThat(raven.getBuilderHelpers(), not(contains(mockBuilderHelper)));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testCantModifyBuilderHelpersDirectly(@Injectable final EventBuilderHelper mockBuilderHelper) throws Exception {
        raven.getBuilderHelpers().add(mockBuilderHelper);
    }

    @Test
    public void testRunBuilderHelpers(@Injectable final EventBuilderHelper mockBuilderHelper,
                                      @Injectable final EventBuilder mockEventBuilder) throws Exception {
        raven.addBuilderHelper(mockBuilderHelper);

        raven.runBuilderHelpers(mockEventBuilder);

        new Verifications() {{
            mockBuilderHelper.helpBuildingEvent(mockEventBuilder);
        }};
    }

    @Test
    public void testCloseConnectionSuccessful() throws Exception {
        raven.closeConnection();

        new Verifications() {{
            mockConnection.close();
        }};
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCloseConnectionFailed() throws Exception {
        new NonStrictExpectations() {{
            mockConnection.close();
            result = new IOException();
        }};

        raven.closeConnection();
    }
}
