package org.glassfish.rmic.classes.nestedClasses;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class TwoLevelNested {

    public class Level1 {

        public class Level2 implements Remote, Cloneable {
            public void level2Execute() throws RemoteException {
                System.out.println("Level2.level2Execute executed");
            }
        }
    }
/*

    void tryThis() {
        new Runnable() {
            @Override
            public void run() {
                System.out.println("Called inner class");
            }
        };
    }
*/
}
