package com.example.fsd;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DirectNotification extends Remote {
    boolean  Stock_updated(String message) throws RemoteException;
}