package com.ae.app;

import com.ae.app.process.Migration;

public class App {

    public static void main(String[] args) {
        String pathname = args[0];
        Migration migration = new Migration(pathname);
        migration.start();
        System.out.println("End!");
    }
}
