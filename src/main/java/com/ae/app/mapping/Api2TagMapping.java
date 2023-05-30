package com.ae.app.mapping;

import com.ae.app.model.BaseAnnotationMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Api2TagMapping extends BaseAnnotationMapping {
    public Api2TagMapping() {
        setOldAnnotation("Api");
        setNewAnnotation("Tag");
        Map<String, String> map = new HashMap<>();
        map.put("tag", "name");
        map.put("description", "description");
        setMapping(map);
        setOldImport(Arrays.asList("io.swagger.annotations.*", "io.swagger.annotations.Api"));
        setNewImport("io.swagger.v3.oas.annotations.tags.Tag");
    }
}
