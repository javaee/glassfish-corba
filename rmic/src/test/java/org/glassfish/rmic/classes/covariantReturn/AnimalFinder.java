package org.glassfish.rmic.classes.covariantReturn;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnimalFinder extends Remote {
    Animal getAnimalWithName(String name) throws RemoteException;
}
