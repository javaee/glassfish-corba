package org.glassfish.rmic.classes.covariantReturn;

import java.rmi.RemoteException;

public interface DogFinder extends AnimalFinder {

    @Override
    Dog getAnimalWithName(String name) throws RemoteException;
}
