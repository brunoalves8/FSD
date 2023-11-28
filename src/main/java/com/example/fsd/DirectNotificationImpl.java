package com.example.fsd;

import java.rmi.RemoteException;

public class DirectNotificationImpl implements DirectNotification {
    @Override
    public String Stock_updated(String message) throws RemoteException {
        System.out.println("\n\n\n\nAtualização de stock: \n" + message);
        return "\n\n\n\nAtualização de stock: \n" + message;
    }
}

