package com.example.fsd;

import java.util.*;

public class ClientConnection {
    private static Hashtable<String, IPInfo> clientIPs = new Hashtable<String, IPInfo>();
    private static int cont = 0;
    public static Vector<String> getStock(String IPAddress) {

        long actualTime = new Date().getTime();
        cont = cont+1;

        System.out.println("cont = "+ cont);

        //Assume-se que o IP e valido!!!!!
        synchronized(this) {
            if (clientIPs.containsKey(IPAddress)) {
                IPInfo newIp = clientIPs.get(IPAddress);
                newIp.setLastSeen(actualTime);
            }
            else {
                IPInfo newIP = new IPInfo(IPAddress, actualTime);
                clientIPs.put(IPAddress,newIP);
            }
        }
        return getStockList();
    }

    private static Vector<String> getStockList() {
        Vector<String> result = new Vector<String>();
        for (Enumeration<IPInfo> e = clientIPs.elements(); e.hasMoreElements(); ) {
            IPInfo element = e.nextElement();
            if (!element.timeOutPassed(180*1000)) {
                result.add(element.getIP());
            }
        }
        return result;
    }

}
