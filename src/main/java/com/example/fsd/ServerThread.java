package com.example.fsd;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;

public class ServerThread extends Thread{
    private Socket socket;
    private final StockServerImpl stockServerImpl; // Referência ao servidor RMI
    public ServerThread(Socket socket, StockServerImpl stockServerImpl) {
        this.socket = socket;
        this.stockServerImpl = stockServerImpl;
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


    private String convertPublicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Cliente conectado ao servidor no endereço " + socket.getInetAddress() + " na porta " + socket.getPort());

            String request = in.readLine();

            if ("STOCK_REQUEST".equals(request)) {

                String filePath = "stock88.csv";
                List<String> produtosEmStock = StockManagement.getAllStockProductsList(filePath);

                out.println("STOCK_RESPONSE");
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
            } else if ("GET_PUBKEY".equals(request)) {
            // Responda à solicitação da chave pública
            String publicKeyString = convertPublicKeyToString(Server.getPublicKey());
            out.println("PUBLIC_KEY " + publicKeyString);
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