package com.swagger.generate.mapping;

import com.swagger.generate.model.BaseAnnotationMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiModelProperty2Schema extends BaseAnnotationMapping {
    public ApiModelProperty2Schema() {
        setOldAnnotation("ApiModelProperty");
        setNewAnnotation("Schema");
        Map<String, String> map = new HashMap<>();
        map.put("value", "title");
        map.put("required", "required");
        map.put("dataType", "type");
        map.put("notes", "description");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.ApiModelProperty"));
        setNewImport("io.swagger.v3.oas.annotations.media.Schema");
    }
}
