package com.sun.tools.corba.ee.idl;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class UtilTest {
    private HashSet<Memento> mementos = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        mementos.add(StaticStubSupport.install(Util.class, "messages", null));
        mementos.add(StaticStubSupport.install(Util.class, "msgFiles", new Vector()));
    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void readVersionFromDefaultPropertyFile() throws Exception {
        assertThat(Util.getVersion(), containsString("4.02"));
    }

    @Test
    public void readVersionFromUnnamedPropertyFile() throws Exception {
        assertThat(Util.getVersion(""), containsString("4.02"));
    }
}