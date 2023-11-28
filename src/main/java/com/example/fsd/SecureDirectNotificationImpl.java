package com.example.fsd;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.security.Signature;
import java.util.Base64;

public class SecureDirectNotificationImpl extends UnicastRemoteObject implements SecureDirectNotification {


    public SecureDirectNotificationImpl() throws RemoteException {
        super();
    }

    @Override
    public String Stock_updated(String message) throws RemoteException {
        System.out.println("\n\n\n\nAtualização de stock: \n" + message);
        return "\n\n\n\nAtualização de stock: \n" + message;
    }

    @Override
    public String stock_updated_signed(String message, String signature) throws RemoteException {

        boolean signatureIsValid = verifySignature(message, signature);
        if (signatureIsValid) {
            // Assinatura verificada com sucesso
            return "Atualização assinada recebida e verificada: " + message;
        } else {
            // Assinatura inválida
            return "Falha na verificação da assinatura.";
        }
    }

    private boolean verifySignature(String message, String encodedSignature) {
        try {
            // Decodifica a assinatura usando Base64
            byte[] signatureBytes = Base64.getDecoder().decode(encodedSignature);

            // Inicializa o objeto Signature para verificação e passa a chave pública
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(Server.getPublicKey());

            // Atualiza a assinatura com os dados da mensagem
            signature.update(message.getBytes());

            // Verifica a assinatura
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Em caso de erro, retorna falso
        }
    }


}

