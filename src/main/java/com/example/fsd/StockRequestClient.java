package com.example.fsd;
import java.io.*;
import java.net.*;
public class StockRequestClient {

    private String serverAddress;
    private int port;

    public StockRequestClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void sendStockRequest() {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Enviar a mensagem "STOCK_REQUEST" para o servidor
            out.println("STOCK_REQUEST");

            System.out.println("Mensagem enviada para o servidor.");

        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
        }
    }

    public static void main(String[] args) {
        StockRequestClient client = new StockRequestClient("127.0.0.1", 8080);
        client.sendStockRequest();
    }
}
