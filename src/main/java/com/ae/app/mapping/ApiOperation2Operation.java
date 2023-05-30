package com.ae.app.mapping;

import com.ae.app.model.BaseAnnotationMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiOperation2Operation extends BaseAnnotationMapping {
    public ApiOperation2Operation() {
        setOldAnnotation("ApiOperation");
        setNewAnnotation("Operation");
        Map<String, String> map = new HashMap<>();
        map.put("value", "summary");
        map.put("notes", "description");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.ApiOperation"));
        setNewImport("io.swagger.v3.oas.annotations.Operation");
    }
}
