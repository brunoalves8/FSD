package com.example.fsd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.*;
import java.util.*;

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

    private boolean lastRequestSuccessful = false;

    public boolean wasLastRequestSuccessful() {
        return lastRequestSuccessful;
    }
    public void sendStockRequest() {
        lastRequestSuccessful = false; // reset para false antes de cada pedido
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            out.println("STOCK_REQUEST");
            String response;

            while ((response = in.readLine()) != null && !response.isEmpty()) {
                if (response.equals("STOCK_RESPONSE")) {
                    for (int i = 0; i < 10; i++) {
                        System.out.println();
                    }
                    System.out.println("Informação de stocks:");
                    System.out.println("ID     NOME");
                } else {
                    System.out.println(response);
                }
            }
            lastRequestSuccessful = true; // pedido foi bem sucedido
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

    public static int lerOpcoesMenusInteiros(String[] opcoes) {
        Integer numero = null;
        String texto = "";

        do {
            escrever("\nSelecione uma das seguintes opcões:");
            for (int i = 0; i < opcoes.length; i++) {
                escrever((i + 1) + " - " + opcoes[i]);
            }

            try {
                texto = scan.nextLine();
                numero = Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é uma opção válida");
            }

            if (numero == null || numero <= 0 || numero > opcoes.length) {
                numero = null;
                escreverErro(texto + " não é uma opção válida");
            }

        } while (numero == null);

        return numero;
    }

    public void updateStock(String action, String productId, int quantity) {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("STOCK_UPDATE");
            out.println(action);  // "ADD" or "REMOVE"
            out.println(productId);
            out.println(quantity);

            String response = in.readLine();

            if ("STOCK_UPDATED".equals(response)) {
                System.out.println("Stock atualizado com sucesso!");
            } else {
                System.out.println("Erro ao atualizar o stock.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
        }
    }

    public static void addProduct(Client client) {
        String productID = lerString("Qual o id do produto que pretende consultar?");
        Integer qtd = lerInteiro("Quantas unidades pretende adicionar desse produto?");
        client.updateStock("ADD", productID, qtd);

    }

    public static void removeProduct(Client client) {
        String productID = lerString("Qual o id do produto que pretende consultar?");
        Integer qtd = lerInteiro("Quantas unidades pretende remover desse produto?");
        client.updateStock("REMOVE", productID, qtd);

    }

    public static void main(String[] args) {
        Client client = connection();
        boolean continuar = true;

        client.sendStockRequest();
        /*Timer timer = new Timer();
        timer.scheduleAtFixedRate(new StockRequestTask(client), 0, 5000); // 5000 ms = 5 segundos*/

        if (!client.wasLastRequestSuccessful()) {
            System.out.println("Não foi possível conectar ao servidor. Tente novamente mais tarde.");
            return; // Sai do programa se não puder conectar
        }

        while (continuar) { // enquanto continuar for verdadeiro, o loop será executado
            //client.sendStockRequest(); // Listar estoque antes do menu

            String[] opcoesCliente = {
                    "Atualizar lista de stock",
                    "Acrescentar produto ao stock",
                    "Remover produto do stock",
                    "Sair"};

            int opcao = lerOpcoesMenusInteiros(opcoesCliente);

            switch (opcao) {
                case 1:
                    client.sendStockRequest(); // Chamando diretamente o método run para executar imediatamente
                    break;
                case 2:
                    addProduct(client);
                    break;
                case 3:
                    removeProduct(client);
                    break;
                case 4:
                    continuar = false; // encerrar o loop
                    break;
            }

            //timer.cancel(); // Pare o timer quando terminar de executar o programa

        }
    }
}