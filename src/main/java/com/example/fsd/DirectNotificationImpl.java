package com.example.fsd;

import java.rmi.RemoteException;

public class DirectNotificationImpl implements DirectNotification {
    @Override
    public String Stock_updated(String message) throws RemoteException {
        System.out.println("Atualização de stock: " + message);
        return "Atualização de stock: " + message;
    }
}

