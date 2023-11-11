package com.example.fsd;
import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.*;

import static java.lang.System.out;

public class Server {
    private static final int PORT = 8888;
    public static final int RMI_PORT = 1099;
    public static final Map<String, DirectNotification> objectClientRMIMap = new HashMap<>();

    public static void main(String[] args) {
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
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}