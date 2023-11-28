package com.example.fsd;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;
import java.util.List;

public class StockServerImpl extends UnicastRemoteObject implements StockServer {

    private final ConcurrentHashMap<String, DirectNotification> objectClientRMIMap = new ConcurrentHashMap<>();
    public StockServerImpl() throws RemoteException {
        super();
    }


    private String generateSignature(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest = md.digest(message.getBytes());

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(Server.getPrivateKey());
        signature.update(messageDigest);
        byte[] digitalSignature = signature.sign();

        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    @Override
    public String stock_request() throws RemoteException {
        try {
            List<String> produtosEmStock = StockManagement.getAllStockProductsList("stock88.csv");
            StringBuilder response = new StringBuilder("Informação de stocks:\nID     NOME\n");
            produtosEmStock.forEach(produto -> response.append(produto).append("\n"));

            String signedMessage = response.toString() + "."+ generateSignature(response.toString());

            return signedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter informação de stock.";
        }
    }

    @Override
    public String stock_update(String id, int qtd) throws RemoteException { // a qtd recebe um valor inteiro positivo se for para adicionar,
        //e recebe um valor inteiro negativo, se for para remover
        String resultMessage;
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
                    resultMessage = "Não é possível adicionar mais unidades. Limite máximo atingido (10.000).";
                }

                StockManagement.addProductQuantity("stock88.csv", id, qtd);
                 String notificationMessage = "Produto (ID:" + id + ") foi atualizado.";   //Esta linha mostra qual o produto que foi atualizado
                // String notificationMessage = stock_request(); // esta liinha mostra o stock
                notifyClients(notificationMessage);
                resultMessage = "Quantidade adicionada com sucesso!";

            } else if (qtd < 0) { // Se a quantidade é negativa, remova do estoque
                if (Math.abs(qtd) > currentQuantity) {
                    resultMessage = "Não é possível remover mais unidades do que as que o produto possui.";
                }

                StockManagement.removeProductQuantity("stock88.csv", id, Math.abs(qtd));
                String notificationMessage = "Produto (ID:" + id + ") foi atualizado.";   //Esta linha mostra qual o produto que foi atualizado
               // String notificationMessage = stock_request(); // esta linha mostra o stock
                notifyClients(notificationMessage);
                resultMessage = "Quantidade removida com sucesso!";

            } else { // Se a quantidade é 0, não faça nada
                resultMessage = "Quantidade não modificada.";
            }

            String signedMessage = resultMessage + ".ASSINATURA:" + generateSignature(resultMessage);
            return signedMessage;


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
        for (DirectNotification client : objectClientRMIMap.values()) {
            try {
                client.Stock_updated(message);
            } catch (RemoteException e) {
                System.out.println("Não foi possivel notificar os clientes");
            }

          }
        }
    }

    @Override
    public PublicKey get_pubKey() {

        return Server.getPublicKey();
    }

}