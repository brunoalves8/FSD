package com.example.fsd;
import java.io.*;
import java.net.*;
import java.util.List;

public class StockServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);

            while (true) {
                new StockRequestHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("oi");
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

            String request = in.readLine();

            if ("STOCK_REQUEST".equals(request)) {

                String filePath = "stock88.csv";
                List<String> produtosEmStock = StockManagement.getAllStockProductsList(filePath);
                for (int i = 0; i < 100; i++) {
                    System.out.println();
                }

                System.out.println("Produtos em stock:");
                System.out.println("ID     NOME");
                for (String produto : produtosEmStock) {
                    System.out.println(produto);
                }

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

