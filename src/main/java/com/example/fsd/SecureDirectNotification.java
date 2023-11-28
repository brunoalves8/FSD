package com.example.fsd;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface SecureDirectNotification extends DirectNotification{
    String stock_updated_signed(String message, String signature) throws RemoteException;
}

