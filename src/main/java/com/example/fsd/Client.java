package com.example.fsd;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Client {
    private final String serverAddress;
    private final int port;
    private PublicKey serverPublicKey;

    private Socket socket;

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    private static final Scanner scan = new Scanner(System.in);

    public static void write(String mensagem) {
        System.out.println(mensagem);
    }

    public static void writeError(String mensagem) {
        System.err.println(mensagem);
    }

    public static int readInteger(String mensagem) {
        Integer numero = null;
        String texto;

        do {
            write(mensagem);
            texto = scan.nextLine();

            try {
                numero = Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                writeError(texto + " não é um número inteiro válido.");
            }

        } while (numero == null);

        return numero;
    }

    public static String readString(String mensagem) {
        write(mensagem);
        return scan.nextLine();
    }

    private boolean verifySignature(String message, String encodedSignature, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());
            return signature.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (Exception e) {
            System.err.println("Erro ao verificar a assinatura: " + e.getMessage());
            return false;
        }
    }


    private static Client login(){
        String endIp = readString("Endereço IP: ");
        int porta = readInteger("Porta Servidor: ");

        Client client = new Client(endIp, porta);
        return client;
    }
    private void connect() {
        try {
            socket = new Socket(serverAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar comando para obter a chave pública
            out.println("GET_PUBKEY");

            // Receber a resposta do servidor
            String response = in.readLine();
            if (response != null && response.startsWith("PUBLIC_KEY")) {
                String publicKeyEncoded = response.substring("PUBLIC_KEY ".length());
                // Aqui você precisaria converter a string codificada da chave pública de volta para um objeto PublicKey
                serverPublicKey = convertStringToPublicKey(publicKeyEncoded);
            }

            stateOfConnection = true;
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PublicKey convertStringToPublicKey(String publicKeyEncoded) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyEncoded);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void disconnect() {
        try {
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar a conexão: " + e);
        }
    }

    private boolean stateOfConnection = false;

    public boolean wasLastRequestSuccessful()
    {
        return stateOfConnection;
    }
    public void sendStockRequest() {
        connect();
        stateOfConnection = false; // reset para false antes de cada pedido
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            out.println("STOCK_REQUEST");
            String response;

            while ((response = in.readLine()) != null && !response.isEmpty()) {
                if (response.equals("STOCK_RESPONSE")) {
                    String[] parts = response.split("\\.", 2);
                    String message = parts[0];
                    String signature = parts[1];

                    if (verifySignature(message, signature, serverPublicKey)) {
                        for (int i = 0; i < 10; i++) {
                            System.out.println();
                        }
                        System.out.println("Informação de stocks:");
                        System.out.println("ID     NOME");
                    } else {
                        System.err.println("Assinatura inválida.");
                    }

                } else {
                    System.out.println(response);
                }
            }
            stateOfConnection = true; // pedido foi bem sucedido
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
        }
    }


    public static int lerOpcoesMenusInteiros(String[] opcoes) {
        Integer numero = null;
        String texto = "";

        do {
            write("\nSelecione uma das seguintes opcões:");
            for (int i = 0; i < opcoes.length; i++) {
                write((i + 1) + " - " + opcoes[i]);
            }

            try {
                texto = scan.nextLine();
                numero = Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                writeError(texto + " não é uma opção válida");
            }

            if (numero == null || numero <= 0 || numero > opcoes.length) {
                numero = null;
                writeError(texto + " não é uma opção válida");
            }

        } while (numero == null);

        return numero;
    }

    public void updateStock(String action, String productId, int quantity) {
        connect();
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("STOCK_UPDATE");
            out.println(action);  // "ADD" or "REMOVE"
            out.println(productId);
            out.println(quantity);

            String response = in.readLine();

            if (response != null) {
                // Separa a mensagem da assinatura
                String[] parts = response.split("\\.", 2);
                String message = parts[0];
                String signature = parts[1];

                if (verifySignature(message, signature, serverPublicKey)) {
                    // Assinatura verificada
                    System.out.println("Resposta do servidor: " + message);
                    if ("STOCK_UPDATED".equals(response)) {
                        System.out.println("Stock atualizado com sucesso!");
                    } else {
                        System.out.println("Erro ao atualizar o stock.");
                    }
                } else {
                    // Assinatura inválida
                    System.err.println("Assinatura da resposta do servidor inválida.");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
        }
    }

    public static Integer getCurrentQuantityFromCSV(String filePath, String productID) {
        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : parser) {
                if (record.get("ID").equals(productID)) {
                    return Integer.parseInt(record.get("Quantidade"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Retorna null se o ID não for encontrado
    }

    public static void addProduct(Client client) throws RemoteException, NotBoundException {
        boolean validInput = false;
        while (!validInput) {
            String productID = readString("Qual o id do produto que pretende adicionar?");
            Integer qtd = readInteger("Quantas unidades pretende adicionar desse produto?");

            // Verificar a existência do ID e a quantidade no CSV
            Integer currentQuantity = getCurrentQuantityFromCSV("stock88.csv", productID);

            if (currentQuantity != null) {
                // Se o ID foi encontrado e a quantidade é válida
                if (qtd > 0 && (currentQuantity + qtd) <= 10000) {
                    client.updateStock("ADD", productID, qtd);
                    //Remover as próximas três linhas se for preciso ou adicionar
                    String notificationMessage = "Produto (ID:" + productID + ") foi atualizado.";
                    StockServer stockServer = (StockServer) LocateRegistry.getRegistry(Server.RMI_PORT).lookup("StockServer");
                    stockServer.notifyClients(notificationMessage);

                    validInput = true; // Sai do loop
                } else if(qtd<0) {
                    System.out.println("Quantidade inválida! A quantidade inserida não pode ser negativa");

                }else{
                    System.out.println("Quantidade inválida! A quantidade total não pode exceder 10.000. Tente novamente.");

                }
            } else {
                System.out.println("ID do produto não encontrado. Por favor, insira um ID válido.");
            }
        }
    }

    public static void removeProduct(Client client) throws RemoteException, NotBoundException {
        boolean validInput = false;
        while (!validInput) {
            String productID = readString("Qual o id do produto que pretende remover?");
            Integer qtd = readInteger("Quantas unidades pretende remover desse produto?");

            // Verificar a existência do ID e a quantidade no CSV
            Integer currentQuantity = getCurrentQuantityFromCSV("stock88.csv", productID);

            if (currentQuantity != null) {
                // Se o ID foi encontrado e há unidades suficientes em stock para remover
                if (qtd > 0 && currentQuantity >= qtd) {
                    client.updateStock("REMOVE", productID, qtd);
                    //Remover as próximas três linhas se for preciso ou adicionar
                    String notificationMessage = "Produto (ID:" + productID + ") foi atualizado.";
                    StockServer stockServer = (StockServer) LocateRegistry.getRegistry(Server.RMI_PORT).lookup("StockServer");
                    stockServer.notifyClients(notificationMessage);

                    validInput = true; // Sai do loop
                } else if(qtd<0) {
                    System.out.println("Quantidade inválida! A quantidade inserida não pode ser negativa");

                } else {
                    System.out.println("Quantidade inválida! Não há unidades suficientes em stock para remover. Tente novamente.");
                }
            } else {
                System.out.println("ID do produto não encontrado. Por favor, insira um ID válido.");
            }
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
    public static void main(String[] args) throws IOException, NotBoundException {
        Client client = login();
        client.connect();
        boolean continuar = true;

        client.sendStockRequest();
        //Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new StockRequestTask(client), 0, 5000); // 5000 ms = 5 segundos

        if (!client.wasLastRequestSuccessful()) {
            System.out.println("Não foi possível conectar ao servidor. Tente novamente mais tarde.");
            return; // Sai do programa se não puder conectar
        }

        while (continuar) { // enquanto continuar for verdadeiro, o loop será executado

            String[] opcoesCliente = {
                    "Atualizar lista de stock",
                    "Adicionar produto ao stock",
                    "Remover produto do stock",
                    "Parar Timer",
                    "Sair"};

            int opcao = lerOpcoesMenusInteiros(opcoesCliente);

            switch (opcao) {
                case 1:
                    client.sendStockRequest();
                    break;
                case 2:
                    addProduct(client);
                    break;
                case 3:
                    removeProduct(client);
                    break;
                case 4:
                    //timer.cancel();
                    break;
                case 5:
                    continuar = false; // encerrar o loop
                  //  timer.cancel();
                    client.disconnect();
                    break;
            }
        }
    }
}