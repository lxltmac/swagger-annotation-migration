package com.swagger.generate.model;

import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseAnnotationMapping {
    private String oldAnnotation;
    private String newAnnotation;
    private Map<String, String> mapping;
    private List<String> oldImport;
    private String newImport;


    public String getOldAnnotation() {
        return oldAnnotation;
    }

    public void setOldAnnotation(String oldAnnotation) {
        this.oldAnnotation = oldAnnotation;
    }

    public String getNewAnnotation() {
        return newAnnotation;
    }

    public void setNewAnnotation(String newAnnotation) {
        this.newAnnotation = newAnnotation;
    }

    /**
     * Returns parameters mapping {oldParameter, newParameter}
     */
    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public List<String> getOldImport() {
        return oldImport;
    }

    public void setOldImport(List<String> oldImport) {
        this.oldImport = oldImport;
    }

    public String getNewImport() {
        return newImport;
    }

    public void setNewImport(String newImport) {
        this.newImport = newImport;
    }


    public List<AnnotationInfo> getSubstitutionInfo(String line, int lineNum) {
        List<AnnotationInfo> result = new ArrayList<>();
        boolean found = false;
        // Use non-greedy quantifier to match the smallest parentheses
        // regex: @\bANNNOTATION\b\((.*)\) then we need to make sure the parentheses are not in ""
        // so we add (?=(?:[^"]*"[^"]*")*[^"]*$) for each parentheses matched
        Pattern pattern = Pattern.compile(String.format("@\\b%s\\b\\((?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)(.*?)\\)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", oldAnnotation));
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            AnnotationInfo info = new AnnotationInfo();
            found = true;
            String oldString = matcher.group();
            String oldParenthesesContent = matcher.group(1);
            Map<String, String> oldAnnotationParameter = getAnnotationParameter(oldParenthesesContent);
            Map<String, String> newAnnotationParameter = mapAnnotationParameter(oldAnnotationParameter);
            String newString = convertMap2String(newAnnotationParameter);
            info.setLine(lineNum).setOldString(oldString).setNewString(newString)
                    .setOldAnnotationParameters(oldAnnotationParameter)
                    .setNewAnnotationParameters(newAnnotationParameter);
            result.add(info);
        }
        // Annotation may not have parentheses
        Pattern patternNoParentheses = Pattern.compile(String.format("@\\b%s\\b(?!\\()", oldAnnotation));
        Matcher matcherNoParentheses = patternNoParentheses.matcher(line);
        while (matcherNoParentheses.find()) {
            AnnotationInfo info = new AnnotationInfo();
            found = true;
            String oldString = matcherNoParentheses.group();
            String newString = "@" + newAnnotation;
            info.setLine(lineNum).setOldString(oldString).setNewString(newString);
            result.add(info);
        }
        // No matches found
        if (!found) return null;
        return result;
    }

    private Map<String, String> getAnnotationParameter(String annotationString) {
        Map<String, String> map = new HashMap<>();
        // matches a comma (,) that is not enclosed within quotes
        String[] keyValuePairs = annotationString.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String keyValue : keyValuePairs) {
            String[] kvArr = keyValue.split("=");
            kvArr = Arrays.stream(kvArr).map(str -> str.trim()).toArray(String[]::new);
            String key, value;

            if (kvArr.length > 1) {
                key = kvArr[0];
                value = kvArr[1];
                if (value.matches(".*\\{.*\\}.*")) {
                    // 存在{}，进行处理
                    value = value.replaceAll("^.*\\{(.*)\\}.*$", "$1"); // 提取花括号内的内容
                }
            } else {
                // Default key is "value"
                key = "value";
                value = kvArr[0];
            }
            map.put(key, value);
        }

        return map;
    }

    protected Map<String, String> mapAnnotationParameter(Map<String, String> oldAnnotationParameter) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, String> entry : oldAnnotationParameter.entrySet()) {
            String key = mapping.get(entry.getKey());
            if (key == null) {
                key = entry.getKey();
            }
            String value = entry.getValue();
            map.put(key, value);
        }
        return map;
    }

    private String convertMap2String(Map<String, String> annotationParameter) {
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(newAnnotation).append("(");
        for (Map.Entry<String, String> entry : annotationParameter.entrySet()) {
            if(StrUtil.isEmpty(entry.getKey())){
                System.out.println(entry + "为空");
                continue;
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        if (annotationParameter.size() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }

}
