package com.example.fsd;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface StockServer extends Remote {
    String stock_request() throws RemoteException;
    String stock_update(String id, int qtd) throws RemoteException;
    void subscribe(DirectNotification client) throws RemoteException; // método para o cliente se registrar para notificações
    void unsubscribe(DirectNotification client) throws RemoteException;
    void notifyClients(String message) throws RemoteException;
}