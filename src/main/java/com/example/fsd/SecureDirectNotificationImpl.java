package com.example.fsd;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;


public class SecureDirectNotificationImpl extends UnicastRemoteObject implements SecureDirectNotification {
    private PublicKey serverPublicKey;

    public SecureDirectNotificationImpl(PublicKey serverPublicKey) throws RemoteException {
        super();
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    public void stock_updated_signed(String message, String signature) throws RemoteException {

        if (verifySignature(message, signature, serverPublicKey)) {
            System.out.println("Mensagem recebida e assinatura verificada com sucesso.\n" + message);
        } else {
            System.err.println("Assinatura Inválida.");
        }

    }

    private boolean verifySignature(String message, String encodedSignature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(message.getBytes());
            return sig.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (Exception e) {
            System.err.println("Erro ao verificar a assinatura: " + e.getMessage());
            return false;
        }
    }


    @Override
    public String Stock_updated(String message) throws RemoteException {
        return null;
    }
}

