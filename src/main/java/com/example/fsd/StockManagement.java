package com.example.fsd;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

public class StockManagement {

    public static List<String> getAllStockProductsList(String filePath) {
        List<String> produtos = new ArrayList<>();

        try (Reader in = new FileReader(filePath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader() // Assume que a primeira linha é o cabeçalho
                    .parse(in);

            for (CSVRecord record : records) {
                String id = record.get("ID");
                String nome = record.get("Nome");
                int quantidade = Integer.parseInt(record.get("Quantidade"));

                if (quantidade > 0) {
                    produtos.add(id+" "+nome);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return produtos;
    }

    }

