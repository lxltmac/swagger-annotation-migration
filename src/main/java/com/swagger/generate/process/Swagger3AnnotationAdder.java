package com.swagger.generate.process;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Swagger3AnnotationAdder {

    private static List<File> allFiles = new ArrayList<>();

    public static void main(String[] args) throws Exception {
//        addSwaggerAnnotationsToFile("/Users/lxltmac/Desktop/DTSpace/dm-order/src/main/java/com/pagoda/order/domain/controller/OrderController.java");
        setAllJavaFiles("/Users/lxltmac/Desktop/GitDemoSpace/sdk-order/src/main/java/com/pagoda/order/domain/dto/protocol/member/HeadReq.java");
        for (File file : allFiles) {
            addSwaggerAnnotationsToFile(file);
        }

    }


    public static void setAllJavaFiles(String pathname) {
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


    public static void addSwaggerAnnotationsToFile(File file) {
        String filePath = file.getAbsolutePath();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append(System.lineSeparator());
            }
            CompilationUnit compilationUnit = StaticJavaParser.parse(file);
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            if(ObjectUtil.isEmpty(types)){
                System.out.println(("当前执行的文件没有执行目标，不处理，文件名是: ")
                        .concat(file.getAbsolutePath()));
                return;
            }
            TypeDeclaration<?> typeDeclaration = types.get(0);
            if(!(typeDeclaration instanceof ClassOrInterfaceDeclaration)){
                System.out.println(String.format("当前执行的文件是%s类型，文件名是: ", typeDeclaration.getClass().getSimpleName())
                        .concat(file.getAbsolutePath()));
                return;
            }
            //如果当前执行的是接口则不处理
            if(((ClassOrInterfaceDeclaration) typeDeclaration).isInterface()){
                System.out.println("当前执行的文件是接口文件: " + typeDeclaration.getNameAsString());
                return;
            }
            String importPakage = "";
            boolean isRestController = false;
            boolean isEntity = false;
            if(typeDeclaration.getAnnotationByName("RestController").isPresent()){
                importPakage = "import io.swagger.v3.oas.annotations.tags.Tag;";
                isRestController = true;
            }
            if(typeDeclaration.getAnnotationByName("Data").isPresent() ||
                    (typeDeclaration.getAnnotationByName("Getter").isPresent() &&
                    typeDeclaration.getAnnotationByName("Setter").isPresent())){
                isEntity = true;
            }
            if(typeDeclaration.getAnnotationByName("ApiModel").isPresent() ||
                    typeDeclaration.getAnnotationByName("ApiModelProperty").isPresent() ||
                            typeDeclaration.getAnnotationByName("Schema").isPresent() ||
                    typeDeclaration.getAnnotationByName("Component").isPresent() ||
                    typeDeclaration.getAnnotationByName("ConfigurationProperties").isPresent()
            ){
                isEntity = false;
            }


            String modifiedContent = "";

            if (isRestController) {
                modifiedContent = addSwaggerAnnotations(fileContent.toString(), importPakage, file, compilationUnit);
            }

            if (isEntity) {
                modifiedContent = addEntitySwaggerAnnotations(file, compilationUnit);
            }

            if(StrUtil.isEmpty(modifiedContent)){
                System.out.println("Do not need to update file. file name: " + file.getAbsolutePath());
                return;
            }

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(modifiedContent);
            }

            System.out.println("Swagger3 annotations added successfully.file name: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("该文件执行异常，请检查。file name: " + file.getAbsolutePath());
        } finally {
            System.out.println("update finish!");
        }
    }

    private static String addEntitySwaggerAnnotations(File file, CompilationUnit compilationUnit) throws Exception {
        // 导入包语句
        ImportDeclaration schemaImport = new ImportDeclaration("io.swagger.v3.oas.annotations.media.Schema", false, false);
        compilationUnit.addImport(schemaImport);
        // 获取所有的类声明
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDeclaration -> {
            handleEntityClassComment(classDeclaration);

            // 获取类中的所有字段声明
            for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
                if(classDeclaration.isInterface()){
                    return;
                }
                handleEntityFieldComment(compilationUnit, fieldDeclaration);
            }
        });


        return compilationUnit.toString();
    }

    private static String addSwaggerAnnotations(String content,
                                                String importPackage,
                                                File file, CompilationUnit compilationUnit) throws Exception {
        // Assuming that each class and method is on a new line for simplicity
        Map<String, String> docMethodCommentMap = findMethodCommentContentMap(compilationUnit);
        String docClassComment = findClassComment(compilationUnit);
        Map<String, String> interFaceToMethodMap = findInterFaceToMethod(compilationUnit);

        String[] lines = content.split(System.lineSeparator());
        StringBuilder modifiedContent = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.contains("@Api") || line.contains("@Tag")){
                modifiedContent = new StringBuilder();
                break;
            }
            modifiedContent.append(line).append(System.lineSeparator());
            if(line.contains("package") && !StrUtil.isEmpty(importPackage)){
                modifiedContent.append(System.lineSeparator()).append(importPackage);
                modifiedContent.append(System.lineSeparator()).append("import io.swagger.v3.oas.annotations.Operation;");
            }
            // Check for class-level annotations
            if (line.contains("@RestController")) {
                String commentContent = null;
                try {
                    commentContent = findCommentContent(lines, i);
                } catch (Exception e) {
                    System.out.println("错误文件："+file.getAbsolutePath());
                    throw e;
                }
                if(StrUtil.isEmpty(commentContent)){
                    commentContent = docClassComment;
                }
                modifiedContent.append("@Tag(name = \"").append(commentContent).append("\", description = \"").append(commentContent).append("接口\")")
                        .append(System.lineSeparator());
            }

            // Check for method-level annotations
            if (line.contains("@GetMapping") || line.contains("@PostMapping")) {
                String commentContent = findCommentContent(lines, i);
                if(StrUtil.isEmpty(commentContent)){
                    String methodName = interFaceToMethodMap.get(line.trim());
                    if(!StrUtil.isEmpty(methodName)){
                        commentContent = docMethodCommentMap.get(methodName);
                        if(StrUtil.isEmpty(commentContent)){
                            commentContent = methodName;
                        }
                    }
                }
                modifiedContent.append("  @Operation(summary = \"").append(commentContent).append("\", description = \"").append(commentContent).append("接口\")")
                        .append(System.lineSeparator());
//                modifiedContent.append("@ApiResponses(@ApiResponse(responseCode = \"200\", description = \"Success\"))")
//                        .append(System.lineSeparator());
            }
        }

        return modifiedContent.toString();
    }

    public static String findCommentContent(String[] lines, int i){
        String commentContent = "";
        for(int k = Math.max(i - 6, 0); k < Math.min(i + 6, lines.length); k++){
            if(lines[k].contains("@Comment")){
                String line = lines[k];
                // 定义正则表达式
                Pattern pattern = Pattern.compile("@Comment\\(\"(.*)\"\\)");
                // 创建匹配器
                Matcher matcher = pattern.matcher(line);
                // 查找匹配项
                while (matcher.find()) {
                    commentContent = matcher.group(1);
                }
                break;
            }
        }
        return commentContent;
    }

    public static Map<String, String> findMethodCommentContentMap(CompilationUnit compilationUnit) throws Exception {
        Map<String, String> docMethodCommentMap = new HashMap<>();
        compilationUnit.findAll(MethodDeclaration.class).forEach(method -> {
            Optional<JavadocComment> javadocComment = method.getJavadocComment();
            javadocComment.ifPresent(comment -> {
                docMethodCommentMap.put(method.getNameAsString(), comment.parse().getDescription().toText());
            });
        });
        return docMethodCommentMap;
    }

    public static String findClassComment(CompilationUnit compilationUnit) throws Exception {
        AtomicReference<String> classComment = new AtomicReference<>("");

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDeclaration -> {
            Optional<JavadocComment> javadocComment = classDeclaration.getJavadocComment();
            javadocComment.ifPresent(comment -> {
                classComment.set(comment.parse().getDescription().toText());
            });
        });
        return classComment.get();
    }

    public static Map<String, String> findInterFaceToMethod(CompilationUnit compilationUnit) throws Exception {
        Map<String, String> map = new HashMap<>();
        compilationUnit.findAll(MethodDeclaration.class).forEach(method -> {
            // 获取方法上的注解
            List<AnnotationExpr> annotations = method.getAnnotations();

            for (AnnotationExpr annotation : annotations) {
                if (annotation.toString().startsWith("@PostMapping")) {
                    map.put(annotation.toString(), method.getNameAsString());
                } else if (annotation.toString().startsWith("@GetMapping")) {
                    map.put(annotation.toString(), method.getNameAsString());
                }
            }
        });
        return map;
    }


    public static Map<String, String> findFieldNameComment(CompilationUnit compilationUnit) throws Exception {
        Map<String, String> filedCommentMap = new HashMap<>();
    // 获取所有的类声明
        compilationUnit.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> {
        // 获取字段的名称
        String fieldName = fieldDeclaration.getVariables().get(0).getNameAsString();
        // 获取字段的注解
        List<AnnotationExpr> annotations = fieldDeclaration.getAnnotations();
        if (!ObjectUtil.isNotEmpty(annotations)) {
            annotations.forEach(annotation -> {
                if(annotation.getName().getIdentifier().equals("Comment")){
                    SingleMemberAnnotationExpr singleMemberAnnotation = (SingleMemberAnnotationExpr) annotation;
                    filedCommentMap.put(fieldName, singleMemberAnnotation.getMemberValue().toString());
                }
            });
        }
        if(StrUtil.isEmpty(filedCommentMap.get(fieldName))){
            // 获取字段的Javadoc注释
            Optional<JavadocComment> fieldJavadoc = fieldDeclaration.getJavadocComment();
            fieldJavadoc.ifPresent(comment -> {
                filedCommentMap.put(fieldName, comment.parse().getDescription().toText());
            });
        }

        if(StrUtil.isEmpty(filedCommentMap.get(fieldName))){
            filedCommentMap.put(fieldName, fieldName);

        }
    });
        return filedCommentMap;
}

    public static void handleEntityClassComment(ClassOrInterfaceDeclaration classDeclaration) {
        if(classDeclaration.isInterface()){
            return;
        }
        List<AnnotationExpr> annotations = classDeclaration.getAnnotations();
        List<String> annotationsName = annotations.stream().map(AnnotationExpr::getName)
                .map(Name::getIdentifier).collect(Collectors.toList());
        if (annotationsName.contains("ApiModel")
                || annotationsName.contains("ApiModelProperty")
                || annotationsName.contains("Schema")) {
            return;
        }

        AtomicReference<String> entityClassComment = new AtomicReference<>("");            // 如果字段没有注释，添加 @ApiModelProperty 注解
        NormalAnnotationExpr classSchemaAnnotation = new NormalAnnotationExpr();
        classSchemaAnnotation.setName("Schema");

        Optional<JavadocComment> javadocClassComment = classDeclaration.getJavadocComment();
        javadocClassComment.ifPresent(comment -> {
            entityClassComment.set(comment.parse().getDescription().toText().trim());
            if(!StrUtil.isEmpty(entityClassComment.get())){
                classSchemaAnnotation.addPair("title", new StringLiteralExpr(comment.parse().getDescription().toText().trim()));
            }
        });
        // 获取字段的注解
        if (StrUtil.isEmpty(entityClassComment.get())) {
            Optional<AnnotationExpr> classCommentAnnotation = classDeclaration.getAnnotationByName("Comment");
            if(classCommentAnnotation.isPresent()){
                SingleMemberAnnotationExpr singleMemberAnnotation = (SingleMemberAnnotationExpr) classCommentAnnotation.get();
                entityClassComment.set(singleMemberAnnotation.getMemberValue().toString());
                classSchemaAnnotation.addPair("title", singleMemberAnnotation.getMemberValue().toString());
            }
        }

        if(StrUtil.isEmpty(entityClassComment.get())){
            // 添加 value 属性
            classSchemaAnnotation.addPair("title", new StringLiteralExpr(classDeclaration.getNameAsString()));
        }

        // 将 @Schema 注解添加到字段上
        classDeclaration.addAnnotation(classSchemaAnnotation);
    }

    public static void handleEntityFieldComment(CompilationUnit compilationUnit, FieldDeclaration fieldDeclaration){
        List<String> fieldAnnotationsName = fieldDeclaration.getAnnotations().stream().map(AnnotationExpr::getName)
                .map(Name::getIdentifier).collect(Collectors.toList());
        if (fieldAnnotationsName.contains("ApiModel")
                || fieldAnnotationsName.contains("ApiModelProperty")
                || fieldAnnotationsName.contains("Schema")) {
            return;
        }
        AtomicReference<String> entityFieldComment = new AtomicReference<>("");
        // 如果字段没有注释，添加 @Schema 注解
        NormalAnnotationExpr schemaAnnotation = new NormalAnnotationExpr();
        schemaAnnotation.setName("Schema");

        Optional<JavadocComment> javadocComment = fieldDeclaration.getJavadocComment();
        javadocComment.ifPresent(comment -> {
            // 添加 value 属性
            StringLiteralExpr value = new StringLiteralExpr(comment.parse().getDescription().toText().trim());
            if(StrUtil.isNotBlank(value.toString()) && !"\"\"".equals(value.toString())){
                String annotationValue = handleValuePattern(value.toString());
                schemaAnnotation.addPair("title", annotationValue);
                schemaAnnotation.addPair("description", annotationValue);
            }
            entityFieldComment.set(comment.parse().getDescription().toText().trim());
        });
        // 获取字段的注解
        if (StrUtil.isEmpty(entityFieldComment.get())) {
            Optional<AnnotationExpr> commentAnnotation = fieldDeclaration.getAnnotationByName("Comment");
            if(commentAnnotation.isPresent()){
                if (commentAnnotation.get() instanceof SingleMemberAnnotationExpr) {
                    SingleMemberAnnotationExpr singleMemberAnnotation = (SingleMemberAnnotationExpr) commentAnnotation.get();
                    String value = singleMemberAnnotation.getMemberValue().toString();
                    String annotationValue = handleValuePattern(value);
                    entityFieldComment.set(annotationValue);
                    // 添加 value 属性
                    schemaAnnotation.addPair("title", annotationValue);
                    schemaAnnotation.addPair("description", annotationValue);
                }
                if (commentAnnotation.get() instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr)commentAnnotation.get();
                    String value = normalAnnotationExpr.getPairs().get(0).getValue().toString();
                    String annotationValue = handleValuePattern(value);
                    entityFieldComment.set(annotationValue);
                    // 添加 value 属性
                    schemaAnnotation.addPair("title", annotationValue);
                    schemaAnnotation.addPair("description", annotationValue);
                }
            }
        }

        if(StrUtil.isEmpty(entityFieldComment.get())){
            // 添加 value 属性
            schemaAnnotation.addPair("title", new StringLiteralExpr(fieldDeclaration.getVariable(0).getNameAsString()));
            schemaAnnotation.addPair("description", new StringLiteralExpr(fieldDeclaration.getVariable(0).getNameAsString()));
        }

        // 将 @Schema 注解添加到字段上
        fieldDeclaration.addAnnotation(schemaAnnotation);
    }

    public static String handleValuePattern(String value){
        Pattern pattern = Pattern.compile("(\\{[^{}]*\\})");
        Matcher matcher = pattern.matcher(value);

        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        while (matcher.find()) {
            result.append(value.substring(lastIndex, matcher.start()));
            String matchedJson = matcher.group(1);
            String escapedJson = matcher.group(1).replaceAll("\"", Matcher.quoteReplacement("\\\""));
            result.append(escapedJson);
            lastIndex = matcher.end();
        }

        result.append(value.substring(lastIndex));
        String annotationValue = result.toString();
        if (annotationValue.startsWith("\"\"") || annotationValue.endsWith("\"\"")) {
            annotationValue = annotationValue.replaceAll("\"\"", "\"");
        }
        return annotationValue;
    }
}
