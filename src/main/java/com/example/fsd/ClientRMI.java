package com.example.fsd;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;

import static com.example.fsd.Client.*;

public class ClientRMI {
    private StockServer remoteServer;

    public ClientRMI(String serverAddress, int rmiPort) {
        try {
            // Localizar o RMI Registry no servidor
            Registry registry = LocateRegistry.getRegistry(serverAddress, rmiPort);

            // PONTO 2: O ClientRMI consegue obter a referência do objeto remoto
            remoteServer = (StockServer) registry.lookup("StockServer");
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

    public void updateStock(String id, int quant){
        try {
            String response = remoteServer.stock_update(id,quant);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ClientRMI connection() {

        String endIp = readString("Endereço IP: ");
        int porta = readInteger("Porta Servidor: ");

        ClientRMI client = new ClientRMI(endIp, porta);

        return client;
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
    }

}
