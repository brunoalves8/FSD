package com.example.fsd;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

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
    public void subscribe(DirectNotification client) throws RemoteException {
        synchronized (objectClientRMIMap) {
            // Adicione a referência do cliente a uma lista ou mapa para futuras notificações
            String clientId = UUID.randomUUID().toString(); // Ou obtenha o ID do cliente de outra maneira
            objectClientRMIMap.put(clientId, client);
            System.out.println("Cliente " + clientId + " inscrito para atualizações.");
        }
    }

    @Override
    public void unsubscribe(DirectNotification client) throws RemoteException {
        synchronized (objectClientRMIMap) {
            // Encontre a chave do cliente baseada em seu valor e remova-a do mapa
            objectClientRMIMap.values().remove(client);
        }
        System.out.println("Cliente desinscrito com sucesso.");
    }

    @Override
    public void notifyClients(String message) {
        synchronized (objectClientRMIMap) {
            Iterator<Map.Entry<String, DirectNotification>> iterator = objectClientRMIMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, DirectNotification> entry = iterator.next();
                try {
                    boolean acknowledged = entry.getValue().Stock_updated(message);
                    if (!acknowledged) {
                        // Log de falha de confirmação, tente novamente ou tome uma ação apropriada
                        System.out.println("Cliente " + entry.getKey() + " não confirmou o recebimento da mensagem.");
                    }
                } catch (RemoteException e) {
                    System.out.println("Não foi possível notificar o cliente " + entry.getKey() + "; removendo-o da lista de notificações.");
                    iterator.remove(); // Remove o cliente que não pode ser notificado
                }
            }
        }
    }


}