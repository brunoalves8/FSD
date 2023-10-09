package com.example.fsd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


@SpringBootApplication
public class MAIN {
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

    private void estabelecerLigacaoServidor(String endIp, int porta){
        Socket ligacao = null;

        try
        {
            // Cria uma conexão Socket com o servidor
            ligacao = new Socket(endIp, porta);

            // Cria um BufferedReader para leitura do Socket
            BufferedReader in = new BufferedReader(new InputStreamReader(ligacao.getInputStream()));

            // Cria um PrintWriter para escrita no Socket
            PrintWriter out = new PrintWriter(ligacao.getOutputStream(), true);

            // Monta a requisição
            String request = "get " + endIp + porta;

            // Envia a requisição para o servidor
            out.println(request);

            // Lê a resposta do servidor
            String msg;
            while ((msg = in.readLine()) != null)
            {
                System.out.println("Resposta do servidor: " + msg);
            }

            // Fecha a conexão
            ligacao.close();

            System.out.println("Terminou a ligacao!");
        }
        catch (IOException e)
        {
            System.out.println("Erro ao comunicar com o servidor: " + e);
            System.exit(1);
        }
    }
    private static void autenticar() {

        String endIp = lerString("Endereço IP: ");
        int porta = lerInteiro("Porta Servidor: ");
        Socket ligacao = null;

        try {
            // Cria uma conexão Socket com o servidor
            ligacao = new Socket(endIp, porta);

            // Cria um BufferedReader para leitura do Socket
            BufferedReader in = new BufferedReader(new InputStreamReader(ligacao.getInputStream()));

            // Cria um PrintWriter para escrita no Socket
            PrintWriter out = new PrintWriter(ligacao.getOutputStream(), true);

            // Monta a requisição
            String request = "get " + endIp + porta;

            // Envia a requisição para o servidor
            out.println(request);

            // Lê a resposta do servidor
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Resposta do servidor: " + msg);
            }

            // Fecha a conexão
            ligacao.close();

            System.out.println("Terminou a ligacao!");
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o servidor: " + e);
            System.exit(1);
        }

    }

    public static void main(String[] args) {

        SpringApplication.run(MAIN.class, args);

      /*  autenticar();


        String filePath = "stock88.csv";
        List<String> produtosEmStock = StockManagement.getAllStockProductsList(filePath);

        System.out.println("Produtos em stock:");
        for (String produto : produtosEmStock) {
            System.out.println(produto);
        }
        */
    }


}

