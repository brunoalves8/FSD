package com.example.fsd;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.*;
import java.util.Base64;


import static java.lang.System.out;

public class Server {
    private static final int PORT = 8888;
    public static final int RMI_PORT = 1099;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    public static void main(String[] args) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        StockServerImpl stockServerImpl = null;
        try {
            stockServerImpl = new StockServerImpl();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            out.println("Servidor Socket iniciado na porta: " + PORT);
            // Inicie o RMI Registry programaticamente
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("RMI Registry iniciado na porta: " + RMI_PORT);

            // Cria uma instância do objeto remoto
            StockServer stockServer = new StockServerImpl();

            // Regista o objeto remoto no RMI Registry
            Registry registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.rebind("StockServer", stockServer);
            //System.out.println("Objeto remoto registado.");

            //out.println("Privada: " + privateKey);
            //out.println("Publica: " + publicKey);

            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket, stockServerImpl).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static String generateSignature(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            try {
                // Crie um objeto de assinatura com o algoritmo SHA256withRSA
                Signature signature = Signature.getInstance("SHA256withRSA");

                // Inicialize o objeto de assinatura com a chave privada
                signature.initSign(privateKey);

                // Atualize o objeto de assinatura com os bytes da mensagem
                signature.update(message.getBytes("UTF-8"));

                // Assine os dados e obtenha a assinatura
                byte[] digitalSignature = signature.sign();

                // Codifique a assinatura em base64 e retorne
                return Base64.getEncoder().encodeToString(digitalSignature);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }
