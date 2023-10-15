package com.example.fsd;
import java.io.*;
import java.net.*;

import static java.lang.System.out;

public class Server {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            out.println("Servidor iniciado na porta: " + PORT);

            while (true) {
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
