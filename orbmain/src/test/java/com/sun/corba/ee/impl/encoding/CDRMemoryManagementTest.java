package com.sun.corba.ee.impl.encoding;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CDRMemoryManagementTest extends EncodingTestBase {

    @Test
    public void whenFinishReadingFragment_dontReleaseIt() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();

        assertEquals(0, getNumBuffersReturned());
    }

    @Test
    public void whenStartReadingNextFragment_releasePreviousFragment() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().read_long();

        assertEquals(1, getNumBuffersReturned());
    }

    @Test
    public void whenStartReadingNextFragmentWhileMarkActive_dontReleasePreviousFragment() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().mark(0);
        getInputObject().read_long();

        assertEquals(0, getNumBuffersReturned());
    }

    @Test
    public void whenStartReadingNextFragmentWhileMarkActive_releasePreviousFragmentOnResetAndNewRead() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().mark(0);
        getInputObject().read_long();
        getInputObject().reset();
        getInputObject().read_long();

        assertEquals(1, getNumBuffersReturned());
    }

    @Test
    public void whenInputObjectClosed_releaseAllFragments() throws IOException {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_short();
        getInputObject().close();

        assertEquals(2, getNumBuffersReturned());
    }

    @Test
    public void whenInputObjectClosedWhileMarkActive_releaseAllFragments() throws IOException {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_short();
        getInputObject().mark(0);
        getInputObject().close();

        assertEquals(2, getNumBuffersReturned());
    }
}
