package com.example.fsd;

public class Product {

    private String productIP;
    private int stockOfProduct;

    public Product(String productIP, int stockOfProduct) {
        this.productIP = productIP;
        this.stockOfProduct = stockOfProduct;
    }

    public String getProductIP() {
        return productIP;
    }

    public void setProductIP(String productIP) {
        this.productIP = productIP;
    }

    public int getStockOfProduct() {
        return stockOfProduct;
    }

    public void setStockOfProduct(int stockOfProduct) {
        this.stockOfProduct = stockOfProduct;
    }
}
