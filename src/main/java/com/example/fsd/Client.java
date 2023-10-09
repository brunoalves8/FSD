package com.example.fsd;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private String serverAddress;
    private int port;

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }
    private static final Scanner scan = new Scanner(System.in);

    public static void escrever(String mensagem) {
        System.out.println(mensagem);
    }

    public static void escreverErro(String mensagem) {
        System.err.println(mensagem);
    }
    public static int lerInteiro(String mensagem) {
        Integer numero = null;
        String texto;

        do {
            escrever(mensagem);
            texto = scan.nextLine();

            try {
                numero = Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número inteiro válido.");
            }

        } while (numero == null);

        return numero;
    }
    public static String lerString(String mensagem) {
        escrever(mensagem);
        return scan.nextLine();
    }

    public static Client connection() {

        String endIp = lerString("Endereço IP: ");
        int porta = lerInteiro("Porta Servidor: ");

        Client client = new Client(endIp, porta);

        return client;
    }
    public void sendStockRequest() {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            out.println("STOCK_REQUEST");
            String response;

            while ((response = in.readLine()) != null && !response.isEmpty()) {
                if (response.equals("STOCK_RESPONSE")) {
                    for (int i = 0; i < 100; i++) {
                        System.out.println();
                    }
                    System.out.println("Informação de stocks:");
                    System.out.println("ID     NOME");
                } else {
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
        }
    }


    static class StockRequestTask extends TimerTask {
        private Client client;

        public StockRequestTask(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.sendStockRequest();
        }
    }


    public static void  main(String[] args) {
        Client client = connection();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new StockRequestTask(client), 0, 10000); // 5000 ms = 5 seconds
    }
}