package com.example.fsd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.UUID;
import static com.example.fsd.Client.*;


public class ClientRMI {

    private static StockServer remoteServer;
    private DirectNotification clientStub;
    private String clientId;
    private String endIpFornc;


    public ClientRMI(String serverAddress, int rmiPort) {
        try {
            endIpFornc=serverAddress;
            // Localizar o RMI Registry no servidor
            Registry registry = LocateRegistry.getRegistry(serverAddress, rmiPort);

            //obter a referência do objeto remoto
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

    public static ClientRMI connection() {

        String endIp = readString("Endereço IP: ");
        int porta = readInteger("Porta Servidor: ");

        ClientRMI client = new ClientRMI(endIp, porta);

        client.registerForNotifications();

        return client;
    }

    public void closeClient() {
        try {
            if (clientStub != null) { // 'clientStub' é a referência ao objeto remoto do cliente
                Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
                StockServer server = (StockServer) registry.lookup("StockServer");
                server.unsubscribe(clientStub);
                UnicastRemoteObject.unexportObject(clientStub, true);
                System.out.println("Client desresgistado do servidor.");
            }
        } catch (Exception e) {
            System.err.println("Erro durante o fechamento do cliente: " + e.getMessage());
        }
    }




    public PublicKey getServerPublicKey() {
        try {
            // Obtém a referência do objeto remoto do servidor
            Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
            StockServer server = (StockServer) registry.lookup("StockServer");

            // Chama o método para obter a chave pública
            return server.get_pubKey();
        } catch (Exception e) {
            System.err.println("Erro ao obter a chave pública do servidor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public void registerForNotifications() {
        try {
            Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
            StockServer server = (StockServer) registry.lookup("StockServer");
            DirectNotification notificationStub = (DirectNotification) UnicastRemoteObject.exportObject(new DirectNotificationImpl(), 0);
            server.subscribe(notificationStub);
            System.out.println("Cliente registado para notificações no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se registrar para notificações: " + e.getMessage());
        }
    }

    public void registerForSecureNotifications(PublicKey serverPublicKey, ClientRMI client) {
        try {
            SecureDirectNotificationImpl clientNotification = new SecureDirectNotificationImpl(serverPublicKey); // serverPublicKey deve ser obtido de alguma forma
            Registry registry = LocateRegistry.getRegistry(client.endIpFornc, 1099);
            StockServer server = (StockServer) registry.lookup("StockServer");
            server.subscribe((SecureDirectNotification) clientNotification);
            System.out.println("Cliente registado para notificações seguras no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se registrar para notificações: " + e.getMessage());
        }
    }

    public void unregisterForNotifications() {
        try {
            Registry registry = LocateRegistry.getRegistry(endIpFornc, Server.RMI_PORT);
            StockServer server = (StockServer) registry.lookup("StockServer");
            DirectNotification notificationStub = (DirectNotification) UnicastRemoteObject.exportObject(new DirectNotificationImpl(), 0);
            server.unsubscribe(notificationStub);
            System.out.println("Cliente removido receber para notificações no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se desregistrar para notificações: " + e.getMessage());
        }
    }

    public void unregisterForSecureNotifications(PublicKey serverPublicKey, ClientRMI client) {
        try {
            SecureDirectNotificationImpl clientNotification = new SecureDirectNotificationImpl(serverPublicKey); // serverPublicKey deve ser obtido de alguma forma
            Registry registry = LocateRegistry.getRegistry(client.endIpFornc, 1099);
            StockServer server = (StockServer) registry.lookup("StockServer");
            server.unsubscribe(clientNotification);
            System.out.println("Cliente removido para receber notificações seguras no servidor.");
        } catch (Exception e) {
            System.err.println("Erro ao se registrar para notificações: " + e.getMessage());
        }
    }

    public void requestStock() {
        try {
            String signedResponse = remoteServer.stock_request();

            // Separar a mensagem e a assinatura
            String[] parts = signedResponse.split("\\.");
            if (parts.length == 2) {
                String message = parts[0];
                String signature = parts[1];
                PublicKey pubKey = remoteServer.get_pubKey();
                SecureDirectNotificationImpl secureNotification = new SecureDirectNotificationImpl(pubKey);
                secureNotification.stock_updated_signed(message, signature);
            } else {
                System.err.println("Formato inválido da mensagem recebida.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao obter dados via RMI: " + e.getMessage());
        }
    }

    public  void addProductRMI(String productId, int quantity) {
        try {
            String signedResponse = remoteServer.stock_update(productId, quantity);

            // Separar a mensagem e a assinatura
            String[] parts = signedResponse.split("\\.");
            if (parts.length == 2) {
                String message = parts[0];
                String signature = parts[1];
                PublicKey pubKey = remoteServer.get_pubKey();
                SecureDirectNotificationImpl secureNotification = new SecureDirectNotificationImpl(pubKey);
                secureNotification.stock_updated_signed(message, signature);
            } else {
                System.err.println("Formato inválido da mensagem recebida.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao adicionar produto via RMI: " + e.getMessage());
        }
    }

    public  void removeProductRMI(String productId, int quantity) {
        try {
            String signedResponse = remoteServer.stock_update(productId, -quantity);

            // Separar a mensagem e a assinatura
            String[] parts = signedResponse.split("\\.");
            if (parts.length == 2) {
                String message = parts[0];
                String signature = parts[1];
                PublicKey pubKey = remoteServer.get_pubKey();

                SecureDirectNotificationImpl secureNotification = new SecureDirectNotificationImpl(pubKey);
                secureNotification.stock_updated_signed(message, signature);
            } else {
                System.err.println("Formato inválido da mensagem recebida.");

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao remover produto via RMI: " + e.getMessage());
        }
    }

    public boolean verifyIfExistsProductID(String filePath, String productID) {
        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : parser) {
                if (record.get("ID").equals(productID)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Retorna null se o ID não for encontrado
    }

    public static void main(String[] args) {
        ClientRMI rmiClient = connection();

        PublicKey serverPublicKey = rmiClient.getServerPublicKey();
        rmiClient.registerForSecureNotifications(serverPublicKey,rmiClient);

        boolean continuar = true;

        rmiClient.requestStock();


        while (continuar) { // enquanto continuar for verdadeiro, o loop será executado

            String[] opcoesCliente = {
                    "Atualizar lista de stock",
                    "Adicionar produto ao stock",
                    "Remover produto do stock",
                    "Sair"};

            int opcao = lerOpcoesMenusInteiros(opcoesCliente);

            switch (opcao) {
                case 1:
                    try {
                        rmiClient.requestStock();
                    } catch(Exception e) {
                        System.out.println("Erro ao obter dados via RMI: " + e.getMessage());
                    }
                    break;
                case 2:
                    boolean verified = false;
                    String productIdToAdd = null;
                    Integer qtdToAdd = null;
                    while(verified == false){
                        productIdToAdd = readString("Qual o id do produto que pretende adicionar?");
                        qtdToAdd = readInteger("Quantas unidades pretende adicionar desse produto?");
                        verified = rmiClient.verifyIfExistsProductID("stock88.csv",productIdToAdd);
                        if(verified == false){
                            System.err.println("ID inválido. Tente novamente");
                        }
                    }
                    rmiClient.addProductRMI(productIdToAdd, qtdToAdd);
                    break;
                case 3:
                    boolean verified2 = false;
                    String productIdToRemove = null;
                    Integer qtdToRemove = null;
                    while(verified2 == false){
                        productIdToRemove = readString("Qual o id do produto que pretende adicionar?");
                        qtdToRemove = readInteger("Quantas unidades pretende remover desse produto?");
                        verified2 = rmiClient.verifyIfExistsProductID("stock88.csv",productIdToRemove);
                        if(verified2 == false){
                            System.err.println("ID iválido. Tente novamente");
                        }
                    }

                    rmiClient.removeProductRMI(productIdToRemove, qtdToRemove);

                    break;
                case 4:
                    continuar = false; // encerrar o loop
                    rmiClient.unregisterForNotifications();
                    rmiClient.unregisterForSecureNotifications(serverPublicKey,rmiClient);
                    rmiClient.closeClient();
                    break;
            }


        }
    }

}