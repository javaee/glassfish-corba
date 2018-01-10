package org.glassfish.rmic.classes.covariantReturn;

import java.io.Serializable;

public interface Animal extends Serializable {
    String getName();

    String speak();
}
