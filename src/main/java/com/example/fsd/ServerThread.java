package com.example.fsd;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.List;

import static java.lang.System.out;

public class ServerThread extends Thread{
    private Socket socket;
    private final StockServerImpl stockServerImpl; // Referência ao servidor RMI
    public ServerThread(Socket socket, StockServerImpl stockServerImpl) {
        this.socket = socket;
        this.stockServerImpl = stockServerImpl;
    }

    private String generateMessageSummary(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

            // Converte o resumo (hash) para uma representação hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Ou lance uma exceção adequada para indicar o erro
        }
    }
    private String signMessage(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(message.getBytes());
            byte[] messageDigest = md.digest();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(Server.getPrivateKey());
            signature.update(messageDigest);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendSignedMessage(String message) {
        String messageSummary = generateMessageSummary(message);
        String signature = signMessage(messageSummary);
        String messageWithSignature = messageSummary + "." + signature;

        out.println(messageWithSignature);
    }

    private String convertPublicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Cliente conectado ao servidor no endereço " + socket.getInetAddress() + " na porta " + socket.getPort());

            String request = in.readLine();
            String msg = null;

            if ("STOCK_REQUEST".equals(request)) {

                List<String> produtosEmStock = StockManagement.getAllStockProductsList("stock88.csv");
                StringBuilder response = new StringBuilder("Informação de stocks:,");
                response.append("ID     NOME").append(",");
                produtosEmStock.forEach(produto -> response.append(produto).append(","));
                out.println("STOCK_RESPONSE");
                out.flush();

                String signedMessage = response.toString() + "." + Server.generateSignature(response.toString());
                out.println(signedMessage);
                out.flush();
            }
            else if ("STOCK_UPDATE".equals(request)) {
                String action = in.readLine();
                String productId = in.readLine();
                int quantity = Integer.parseInt(in.readLine());

                if ("ADD".equals(action)) {
                    StockManagement.addProductQuantity("stock88.csv", productId, quantity);
                    stockServerImpl.notifyClients("Produto (ID:" + productId + ") foi atualizado.");
                    msg = "Quantidade adicionada com sucesso!";
                } else if ("REMOVE".equals(action)) {
                    StockManagement.removeProductQuantity("stock88.csv", productId, quantity);
                    stockServerImpl.notifyClients("Produto (ID:" + productId + ") foi atualizado.");
                    msg = "Quantidade removida com sucesso!";
                }
                String signedMessage = msg + "." + Server.generateSignature(msg);
                out.println(signedMessage);

            } else if ("GET_PUBKEY".equals(request)) {

            String publicKeyString = convertPublicKeyToString(Server.getPublicKey());
            out.println("PUBLIC_KEY " + publicKeyString);
            sendSignedMessage("PUBLIC_KEY " + publicKeyString);
        }

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
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