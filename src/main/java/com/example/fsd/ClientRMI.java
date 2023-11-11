package com.example.fsd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.UUID;

import static com.example.fsd.Client.*;

public class ClientRMI {
    private static StockServer remoteServer;
    private DirectNotification clientStub;
    private String clientId;
    public ClientRMI(String serverAddress, int rmiPort) {
        try {
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

    public static ClientRMI connection() {

        String endIp = readString("Endereço IP: ");
        int porta = readInteger("Porta Servidor: ");

        ClientRMI client = new ClientRMI(endIp, porta);
        try {
            remoteServer.registerClient(client.clientId, client.clientStub);
        } catch (RemoteException e) {
            System.err.println("Erro ao conectar ao servidor: \n\n" + e);
        }

        return client;
    }

    public void close() {
        // Remove o cliente do servidor ao desconectar
        try {
            // Remove o cliente do servidor ao desconectar
            remoteServer.unregisterClient(clientId);

            // Desvincula o stub do cliente
            UnicastRemoteObject.unexportObject(clientStub, true);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void registerOnServer(String clientId, DirectNotification client) {
        try {
            remoteServer.registerClient(clientId, client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterFromServer(String clientId) {
        try {
            remoteServer.unregisterClient(clientId);
        } catch (RemoteException e) {
            e.printStackTrace();
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
                        System.out.println(stockInfo);
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
                    break;
            }


        }
        rmiClient.close();
    }

}
