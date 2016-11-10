package com.sun.corba.ee.spi.presentation.rmi;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 */
public class PresentationDefaultsTest {

  private List<Memento> mementos = new ArrayList<>();

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {
    for (Memento memento : mementos) memento.revert();
  }

  @Test
  public void dynamicStubFactory_createsDynamicStubs() throws Exception {
    assertThat(PresentationDefaults.getDynamicStubFactoryFactory().createsDynamicStubs(), is(true));
  }

  @Test
  public void staticStubFactory_doesNotCreateDynamicStubs() throws Exception {
    assertThat(PresentationDefaults.getStaticStubFactoryFactory().createsDynamicStubs(), is(false));
  }

  @Test
  public void defaultOrbPresentationManager_createsDynamicStubs() throws Exception {
    assertThat(PresentationDefaults.makeOrbPresentationManager().useDynamicStubs(), is(true));
  }

  @Test
  public void whenSystemPropertyFalse_presentationManagerCreatesStaticStubs() throws Exception {
    mementos.add(SystemPropertySupport.install(ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "false"));

    assertThat(PresentationDefaults.makeOrbPresentationManager().useDynamicStubs(), is(false));
  }
}