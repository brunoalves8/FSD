package com.example.fsd;

public class Login {

    private String enderecoIP;
    private int porta;

    public Login(String enderecoIP, int porta) {
        this.enderecoIP = enderecoIP;
        this.porta = porta;
    }

    public String getEnderecoIP() {
        return enderecoIP;
    }

    public void setEnderecoIP(String enderecoIP) {
        this.enderecoIP = enderecoIP;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }
}
