package com.example.fsd;

import java.rmi.RemoteException;

public class DirectNotificationImpl implements DirectNotification {
    @Override
    public boolean  Stock_updated(String message) throws RemoteException {
        System.out.println("Atualização de stock: " + message);
        return true;
    }
}

