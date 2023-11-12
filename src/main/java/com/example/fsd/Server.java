package com.example.fsd;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.out;

public class Server {
    private static final int PORT = 5000;
    public static final int RMI_PORT = 1099;
    private ConcurrentHashMap<String, DirectNotification> objectClientRMIMap = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        StockServerImpl stockServerImpl = null;
        try {
            stockServerImpl = new StockServerImpl();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            out.println("Servidor Socket iniciado na porta: " + PORT);
            // Inicie o RMI Registry programaticamente
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("RMI Registry iniciado na porta: " + RMI_PORT);

            // Cria uma instância do objeto remoto
            StockServer stockServer = new StockServerImpl();

            // Regista o objeto remoto no RMI Registry
            Registry registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.rebind("StockServer", stockServer);
            //System.out.println("Objeto remoto registado.");


            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket, stockServerImpl).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}