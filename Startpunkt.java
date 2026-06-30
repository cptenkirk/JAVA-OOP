package com.mycompany.app;

public final class Startpunkt {

    public static void main(String[] args) {

        LKW mehllaster = new LKW(1, 
                                 "Mehl", 
                                 "HBKX440", 
                                 20000.0);

        LKW milchlaster = new LKW(2, 
                                  "Milch", 
                                  "HBKX441", 
                                  25000.0);
                                  

        System.out.println("1 fährt los: " + mehllaster.fracht());
        System.out.println("2 fährt los: " + milchlaster.fracht());

        Knotenpunkt fabrik = new Endpunkt("Fabrik ");

        fabrik.lkwEntladung(mehllaster);
        fabrik.lkwEntladung(milchlaster);

    }
}
