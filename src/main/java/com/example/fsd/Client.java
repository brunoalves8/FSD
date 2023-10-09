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

    public void sendStockRequest() {
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

    public static void addProductQuantity(String filePath, String productId, int quantityToAdd) {
        List<CSVRecord> recordsList = new ArrayList<>();

        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            recordsList.addAll(parser.getRecords());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Adicionar quantidade baseado no ID do produto
        List<Map<String, String>> updatedRecordsMap = new ArrayList<>();
        for (CSVRecord record : recordsList) {
            Map<String, String> recordMap = record.toMap();
            if (record.get("ID").equals(productId)) {
                int currentQuantity = Integer.parseInt(record.get("Quantidade"));
                int updatedQuantity = currentQuantity + quantityToAdd;
                recordMap.put("Quantidade", String.valueOf(updatedQuantity));
            }
            updatedRecordsMap.add(recordMap);
        }

// Reescrever o arquivo CSV
        try (Writer out = new FileWriter(filePath)) {
            CSVPrinter printer = CSVFormat.DEFAULT.withHeader("ID", "Nome", "Quantidade").print(out);
            for (Map<String, String> recordMap : updatedRecordsMap) {
                printer.printRecord(recordMap.values());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addProduct() {
        String productID = lerString("Qual o id do produto que pretende consultar?");
        Integer qtd = lerInteiro("Quantas unidades pretende adicionar desse produto?");
        addProductQuantity("stock88.csv", productID, qtd);

    }


    public static void removeProductQuantity(String filePath, String productId, int quantityToAdd) {
        List<CSVRecord> recordsList = new ArrayList<>();

        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            recordsList.addAll(parser.getRecords());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Adicionar quantidade baseado no ID do produto
        List<Map<String, String>> updatedRecordsMap = new ArrayList<>();
        for (CSVRecord record : recordsList) {
            Map<String, String> recordMap = record.toMap();
            if (record.get("ID").equals(productId)) {
                int currentQuantity = Integer.parseInt(record.get("Quantidade"));
                int updatedQuantity = currentQuantity - quantityToAdd;
                recordMap.put("Quantidade", String.valueOf(updatedQuantity));
            }
            updatedRecordsMap.add(recordMap);
        }

// Reescrever o arquivo CSV
        try (Writer out = new FileWriter(filePath)) {
            CSVPrinter printer = CSVFormat.DEFAULT.withHeader("ID", "Nome", "Quantidade").print(out);
            for (Map<String, String> recordMap : updatedRecordsMap) {
                printer.printRecord(recordMap.values());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeProduct() {
        String productID = lerString("Qual o id do produto que pretende consultar?");
        Integer qtd = lerInteiro("Quantas unidades pretende remover desse produto?");
        removeProductQuantity("stock88.csv", productID, qtd);

    }

    public static void main(String[] args) {
        Client client = connection();
        boolean continuar = true;

        while (continuar) { // enquanto continuar for verdadeiro, o loop será executado
            client.sendStockRequest(); // Listar estoque antes do menu

            String[] opcoesCliente = {
                    "Atualizar lista de stock",
                    "Acrescentar produto ao stock",
                    "Remover produto do stock",
                    "Sair"};

            int opcao = lerOpcoesMenusInteiros(opcoesCliente);

            switch (opcao) {
                case 1:
                    new StockRequestTask(client).run(); // Chamando diretamente o método run para executar imediatamente
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    removeProduct();
                    break;
                case 4:
                    continuar = false; // encerrar o loop
                    break;
            }


       /* Timer timer = new Timer();
        timer.scheduleAtFixedRate(new StockRequestTask(client), 0, 10000); // 5000 ms = 5 seconds*/
        }
    }
}