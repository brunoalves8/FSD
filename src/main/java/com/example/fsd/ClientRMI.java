package com.example.fsd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.UUID;

import static com.example.fsd.Client.*;
import static com.example.fsd.Server.RMI_PORT;

public class ClientRMI {

    private volatile String lastUpdateId; // Último ID de atualização recebido
    private static StockServer remoteServer;
    private DirectNotification clientStub;
    private String clientId;
    private String endIpFornc;

    public ClientRMI(String serverAddress, int rmiPort) {
        try {
            endIpFornc=serverAddress;
            // PONTO Localizar o RMI Registry no servidor
            Registry registry = LocateRegistry.getRegistry(serverAddress, rmiPort);

            // PONTO 2: O ClientRMI consegue obter a referência do objeto remoto
            remoteServer = (StockServer) registry.lookup("StockServer");

            // Gera um id para o objeto remoto do cliente
            clientId = UUID.randomUUID().toString();

            // Gera um stub para o objeto remoto do cliente (stub é um proxy local)
            clientStub = (DirectNotification) UnicastRemoteObject.exportObject(new DirectNotificationImpl(), 0);
            //clientStub permite que o servidor chame métodos no objeto remoto do cliente quando necessário.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String requestStock() {
        try {
            String response = remoteServer.stock_request();
            System.out.println(response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public  String addProductRMI(String productId, int quantity) {
        try {
            return remoteServer.stock_update(productId, quantity);
        } catch (Exception e) {
            return "Erro ao adicionar produto: " + e.getMessage();
        }
    }

    public  String removeProductRMI(String productId, int quantity) {
        try {
            return remoteServer.stock_update(productId, -quantity);  // Note o sinal negativo para indicar a remoção
        } catch (Exception e) {
            return "Erro ao remover produto: " + e.getMessage();
        }
    }

    public static ClientRMI connection() {

        String endIp = readString("Endereço IP: ");
        int porta = readInteger("Porta Servidor: ");

        ClientRMI client = new ClientRMI(endIp, porta);

        client.registerForNotifications();

        return client;
    }

    public void registerForNotifications() {
        try {
            Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
            StockServer server = (StockServer) registry.lookup("StockServer");
            DirectNotification notificationStub = (DirectNotification) UnicastRemoteObject.exportObject(new DirectNotificationImpl(), 0);
            server.subscribe(notificationStub);
            System.out.println("Registado para notificações no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se registrar para notificações: " + e.getMessage());
        }
    }

    public void unregisterForNotifications() {
        try {
            Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
            StockServer server = (StockServer) registry.lookup("StockServer");
            DirectNotification notificationStub = (DirectNotification) UnicastRemoteObject.exportObject(new DirectNotificationImpl(), 0);
            server.subscribe(notificationStub);
            System.out.println("Desregistado para notificações no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se desregistrar para notificações: " + e.getMessage());
        }
    }
    public void closeClient() {
        try {
            if (clientStub != null) { // 'clientStub' é a referência ao objeto remoto do cliente
                Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
                StockServer server = (StockServer) registry.lookup("StockServer");
                server.unsubscribe(clientStub);
                UnicastRemoteObject.unexportObject(clientStub, true);
                System.out.println("Desregistado do servidor e limpeza concluída.");
            }
        } catch (Exception e) {
            System.err.println("Erro durante o fechamento do cliente: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        ClientRMI rmiClient = connection();


        boolean continuar = true;

        rmiClient.requestStock();


        while (continuar) { // enquanto continuar for verdadeiro, o loop será executado

            String[] opcoesCliente = {
                    "Atualizar lista de stock",
                    "Acrescentar produto ao stock",
                    "Remover produto do stock",
                    "Sair"};

            int opcao = lerOpcoesMenusInteiros(opcoesCliente);

            switch (opcao) {
                case 1:
                    try {
                        String stockInfo = rmiClient.requestStock();
                    } catch(Exception e) {
                        System.out.println("Erro ao obter dados via RMI: " + e.getMessage());
                    }
                    break;
                case 2:
                    String productIdToAdd = readString("Qual o id do produto que pretende adicionar?");
                    Integer qtdToAdd = readInteger("Quantas unidades pretende adicionar desse produto?");
                    String responseAdd = rmiClient.addProductRMI(productIdToAdd, qtdToAdd);
                    System.out.println(responseAdd);
                    break;
                case 3:
                    String productIdToRemove = readString("Qual o id do produto que pretende remover?");
                    Integer qtdToRemove = readInteger("Quantas unidades pretende remover desse produto?");
                    String responseRemove = rmiClient.removeProductRMI(productIdToRemove, qtdToRemove);
                    System.out.println(responseRemove);
                    break;
                case 4:
                    continuar = false; // encerrar o loop
                    rmiClient.unregisterForNotifications();
                    rmiClient.closeClient();
                    break;
            }


        }
    }

}