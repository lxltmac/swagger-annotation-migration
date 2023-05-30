package com.ae.app.mapping;

import com.ae.app.model.BaseAnnotationMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiModel2Schema extends BaseAnnotationMapping {
    public ApiModel2Schema() {
        setOldAnnotation("ApiModel");
        setNewAnnotation("Schema");
        Map<String, String> map = new HashMap<>();
        map.put("value", "name");
        map.put("description", "description");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.ApiModel"));
        setNewImport("io.swagger.v3.oas.annotations.media.Schema");
    }
}
