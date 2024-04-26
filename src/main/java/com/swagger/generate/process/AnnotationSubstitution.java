package com.swagger.generate.process;

import com.swagger.generate.model.AnnotationInfo;
import com.swagger.generate.model.BaseAnnotationMapping;
import com.swagger.generate.mapping.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class AnnotationSubstitution {
    public AnnotationSubstitution(File file) {
        this.file = file;
    }

    private File file;

    static List<BaseAnnotationMapping> mapping;

    static {
        mapping = new ArrayList<>();
        // Add new mappings here
        mapping.add(new Api2TagMapping());
        mapping.add(new ApiModel2Schema());
        mapping.add(new ApiModelProperty2Schema());
        mapping.add(new ApiOperation2Operation());
        mapping.add(new ApiParam2ParameterMapping());
    }

    public void start() {
        System.out.println(String.format("Processing %s", file.getAbsolutePath()));
        boolean hasChanges = false;
        Set<String> oldImports = new HashSet<>();
        Set<String> newImports = new HashSet<>();
        try {
            List<String> fileContent = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            List<String> newLines = new ArrayList<>();

            for (int i = 0; i < fileContent.size(); i++) {
                String line = fileContent.get(i);

                for (BaseAnnotationMapping mapping : AnnotationSubstitution.mapping) {
                    List<AnnotationInfo> infoList = mapping.getSubstitutionInfo(line, i);
                    if (infoList == null) {
                        continue;
                    }

                    // Replace
                    for (AnnotationInfo info : infoList) {
                        line = line.replace(info.getOldString(), info.getNewString());
                        oldImports.addAll(mapping.getOldImport());
                        newImports.add(mapping.getNewImport());
                        hasChanges = true;
                    }
                }
                newLines.add(line);
            }

            // Exit if no changes
            if (!hasChanges) return;

            // Process imports
            // Remove old imports
            for (int i = newLines.size() - 1; i >= 0; i--) {
                String item = newLines.get(i);
                boolean canDel = false;
                for (String oldImport : oldImports) {
                    if (item.contains(oldImport)) {
                        canDel = true;
                        break;
                    }
                }
                if (canDel) {
                    newLines.remove(i);
                }
            }

            // Insert new imports
            int importLineNo = 0;
            for (int i = 0; i < newLines.size(); i++) {
                if (newLines.get(i).startsWith("package")) {
                    importLineNo = i + 1;
                    break;
                }
                if (newLines.get(i).startsWith("import")) {
                    importLineNo = i;
                    break;
                }
            }
            for (String newImport : newImports) {
                String newImportString = String.format("import %s;", newImport);
                newLines.add(importLineNo, newImportString);
            }

            // Write back
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
                for (String line : newLines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.close();
                System.out.println("File: " + file.getAbsolutePath() + " updated successfully.");
            } catch (IOException e) {
                System.out.println("An error occurred while writing to file: " + file.getAbsolutePath());
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
