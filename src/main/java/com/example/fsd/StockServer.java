package com.example.fsd;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface StockServer extends Remote {
    String stock_request() throws RemoteException;
    String stock_update(String id, int qtd) throws RemoteException;
    void registerClientRMI(String clientEndId, DirectNotification client) throws RemoteException;
    void registerClientSocket(Socket clientSocket, DirectNotification client) throws RemoteException;
    void unregisterClient(String clientId) throws RemoteException;
    void notifyClients(String message) throws RemoteException;

}