package com.example.fsd;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
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
                String qtd = record.get("Quantidade");
                int quantidade = Integer.parseInt(record.get("Quantidade"));

                if (quantidade > 0) {
                    produtos.add(id+"   "+nome+" -> QTD:"+qtd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return produtos;
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
}

