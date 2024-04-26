package com.swagger.generate.mapping;

import com.swagger.generate.model.BaseAnnotationMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Api2TagMapping extends BaseAnnotationMapping {
    public Api2TagMapping() {
        setOldAnnotation("Api");
        setNewAnnotation("Tag");
        Map<String, String> map = new HashMap<>();
        map.put("tags", "name");
        map.put("description", "description");
        map.put("value", "description");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.Api"));
        setNewImport("io.swagger.v3.oas.annotations.tags.Tag");
    }
}
