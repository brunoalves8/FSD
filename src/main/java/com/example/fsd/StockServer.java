package com.example.fsd;
import java.io.*;
import java.net.*;

public class StockServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta: " + PORT);

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

            String request = in.readLine();

            if ("STOCK_REQUEST".equals(request)) {
                // Aqui, estamos enviando uma resposta mock, mas você pode buscar a informação real de stocks
                String stockInfo = "ProdutoA: 10, ProdutoB: 5, ProdutoC: 20";
                out.println("STOCK_RESPONSE " + stockInfo);
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

