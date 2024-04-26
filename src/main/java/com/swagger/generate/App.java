package com.swagger.generate;

import com.swagger.generate.process.Migration;

public class App {

    public static void main(String[] args) {
//        String pathname = args[0];
        String pathname = "/Users/lxltmac/Desktop/DTSpace/dm-order-info/src/main/java/com/pagoda/dto";
        Migration migration = new Migration(pathname);
        migration.start();
        System.out.println("End!");
    }


}
