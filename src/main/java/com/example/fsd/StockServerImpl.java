package com.example.fsd;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

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
    public String stock_update(String id, int qtd) throws RemoteException {
        try {
            // Se a quantidade é positiva, adicione ao estoque
            if (qtd > 0) {
                StockManagement.addProductQuantity("stock88.csv", id, qtd);
                return "Quantidade adicionada com sucesso.";

            } else if (qtd < 0) { // Se a quantidade é negativa, remova do estoque
                StockManagement.removeProductQuantity("stock88.csv", id, Math.abs(qtd));
                return "Quantidade removida com sucesso.";

            } else { // Se a quantidade é 0, não faça nada
                return "Quantidade não modificada.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao atualizar o stock.";
        }
    }
}

