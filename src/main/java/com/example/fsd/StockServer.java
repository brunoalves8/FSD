package com.example.fsd;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface StockServer extends Remote {
    String stock_request() throws RemoteException;
    String stock_update(String id, int qtd) throws RemoteException;
    void registerClient(String clientEndId, DirectNotification client) throws RemoteException;
    void unregisterClient(String clientId) throws RemoteException;
}
