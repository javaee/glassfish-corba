package org.glassfish.rmic.classes.covariantReturn;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnimalFinder extends Remote {
    Animal getAnimalWithName(String name) throws RemoteException;

    Animal[][] getZooCages(int[][] sizes) throws RemoteException;

    double getValues(short a, long b, float c, double d)  throws RemoteException;
}
