package com.example.fsd;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface SecureDirectNotification extends Remote{
    void stock_updated_signed(String message, String signature) throws RemoteException;
}

