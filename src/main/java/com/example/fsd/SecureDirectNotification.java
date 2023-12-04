package com.example.fsd;

import java.rmi.RemoteException;
public interface SecureDirectNotification extends DirectNotification{
    void stock_updated_signed(String message, String signature) throws RemoteException;
}

