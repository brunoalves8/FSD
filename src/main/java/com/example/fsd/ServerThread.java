package com.example.fsd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ServerThread extends Thread{
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Cliente conectado ao servidor no endereço " + socket.getInetAddress() + " na porta " + socket.getPort());

            String request = in.readLine();

            if ("STOCK_REQUEST".equals(request)) {

                String filePath = "stock88.csv";
                List<String> produtosEmStock = StockManagement.getAllStockProductsList(filePath);

                out.println("STOCK_RESPONSE");  // Start of the response
                for (String produto : produtosEmStock) {
                    out.println(produto);
                }
            } else if ("STOCK_UPDATE".equals(request)) {
                String action = in.readLine();
                String productId = in.readLine();
                int quantity = Integer.parseInt(in.readLine());

                if ("ADD".equals(action)) {
                    StockManagement.addProductQuantity("stock88.csv", productId, quantity);
                } else if ("REMOVE".equals(action)) {
                    StockManagement.removeProductQuantity("stock88.csv", productId, quantity);
                }
                out.println("STOCK_UPDATED");
            }
        } catch (SocketException e) {
            // Ignora a exceção SocketException ao cliente fechar abruptamente a conexão
            System.out.println("Cliente desconectado abruptamente.");
            try {
                Thread.sleep(5000); // aguarda 5 segundos antes de tentar reconectar
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}