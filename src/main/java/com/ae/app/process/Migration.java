package com.ae.app.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Migration {
    public Migration(String pathname) {
        this.pathname = pathname;
    }

    private String pathname;
    private List<File> allFiles = new ArrayList<>();


    public void start() {
        setAllJavaFiles(pathname);
        allFiles.stream().forEach(this::processFile);
    }

    private void setAllJavaFiles(String pathname) {
        File folder = new File(pathname);
        if (folder.isFile()) {
            String[] fileSplit = folder.getName().split("\\.");
            if (fileSplit.length > 1 && fileSplit[1].equals("java")) {
                allFiles.add(folder);
            }
            return;
        }

        File[] listFiles = folder.listFiles();

        for (File file : listFiles) {
            if (file.isFile()) {
                String[] fileSplit = file.getName().split("\\.");
                if (fileSplit.length > 1 && fileSplit[1].equals("java")) {
                    allFiles.add(file);
                }
            } else if (file.isDirectory()) {
                setAllJavaFiles(file.getAbsolutePath());
            }
        }
    }

    private void processFile(File file) {
        AnnotationSubstitution as = new AnnotationSubstitution(file);
        as.start();
    }
}
