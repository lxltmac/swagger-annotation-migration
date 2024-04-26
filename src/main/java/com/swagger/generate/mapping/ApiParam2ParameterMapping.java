package com.swagger.generate.mapping;

import com.swagger.generate.model.BaseAnnotationMapping;

import java.util.*;

public class ApiParam2ParameterMapping extends BaseAnnotationMapping {
    public ApiParam2ParameterMapping() {
        setOldAnnotation("ApiParam");
        setNewAnnotation("Parameter");
        Map<String, String> map = new HashMap<>();
        map.put("name", "name");
        map.put("value", "description");
        map.put("defaultValue", "");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.ApiParam"));
        setNewImport("io.swagger.v3.oas.annotations.Parameter");
    }
}
