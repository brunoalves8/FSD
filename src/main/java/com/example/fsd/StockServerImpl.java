package com.example.fsd;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.example.fsd.Server.objectClientRMIMap;

public class StockServerImpl extends UnicastRemoteObject implements StockServer {


    public StockServerImpl() throws RemoteException {
        super();
    }

    @Override
    public String stock_request() throws RemoteException {
        try {
            List<String> produtosEmStock = StockManagement.getAllStockProductsList("stock88.csv");

            // Criar um StringBuilder para construir a resposta
            StringBuilder response = new StringBuilder();

            // Adicionar os cabeçalhos à resposta
            response.append("Informação de stocks:\n");
            response.append("ID     NOME\n");

            // Adicionar cada produto à resposta
            for (String produto : produtosEmStock) {
                response.append(produto).append("\n");
            }

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter informação de stock.";
        }
    }

    @Override
    public String stock_update(String id, int qtd) throws RemoteException { // a qtd recebe um valor inteiro positivo se for para adicionar,
        //e recebe um valor inteiro negativo, se for para remover
        try {
            // Verificar se o produto existe
            Integer currentQuantity = StockManagement.getCurrentQuantity("stock88.csv",id);
            if (currentQuantity == null) {
                return "Produto não encontrado.";
            }

            // Se a quantidade é positiva, adicione ao estoque
            if (qtd > 0) {
                int newQuantity = currentQuantity + qtd;
                if (newQuantity > 10000) {
                    return "Não é possível adicionar mais unidades. Limite máximo atingido (10.000).";
                }

                StockManagement.addProductQuantity("stock88.csv", id, qtd);
                String notificationMessage = "Produto (ID:" + id + ") foi atualizado.";
                notifyClients(notificationMessage);
                return "Quantidade adicionada com sucesso.";

            } else if (qtd < 0) { // Se a quantidade é negativa, remova do estoque
                if (Math.abs(qtd) > currentQuantity) {
                    return "Não é possível remover mais unidades do que as que o produto possui.";
                }

                StockManagement.removeProductQuantity("stock88.csv", id, Math.abs(qtd));
                String notificationMessage = "Produto (ID:" + id + ") foi atualizado.";
                notifyClients(notificationMessage);
                return "Quantidade removida com sucesso.";

            } else { // Se a quantidade é 0, não faça nada
                return "Quantidade não modificada.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao atualizar o stock.";
        }
    }


    @Override
    public void registerClient(String clientId, DirectNotification client) throws RemoteException {
        synchronized (objectClientRMIMap) {
            objectClientRMIMap.put(clientId, client);
        }
        System.out.println("Cliente RMI " + clientId + " registou-se");
    }
    @Override
    public void unregisterClient(String clientId) throws RemoteException {
        synchronized (Server.objectClientRMIMap) {
            Server.objectClientRMIMap.remove(clientId);
        }
        System.out.println("Cliente RMI " + clientId + " desconectou-se");
    }
    @Override
    public void notifyClients(String message) throws RemoteException {
        // Notificar clientes RMI
        synchronized (Server.objectClientRMIMap) {
            for (DirectNotification client : Server.objectClientRMIMap.values()) {
                client.Stock_updated(message);
            }
        }



    }


}