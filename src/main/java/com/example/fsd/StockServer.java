package com.example.fsd;
import java.io.*;
import java.net.*;
import java.util.List;

import static java.lang.System.out;

public class StockServer {
    private static final int PORT = 8889;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            out.println("Servidor iniciado na porta: " + PORT);

            while (true) {
                new StockRequestHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class StockRequestHandler extends Thread {
    private Socket socket;

    public StockRequestHandler(Socket socket) {
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