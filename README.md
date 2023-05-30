# OpenAPI 2 / Swagger 2 to OpenAPI 3 Annotations Migration

```
WARNING!!!
PLEASE BACKUP YOUR PROJECT BEFORE USING.
THIS WILL REPLACE YOUR SWAGGER ANNOTATION IN PLACE
```
## Why
Springdoc doesn't provide any tools for refactoring from SpringFox annotation to Springdoc annotation
https://springdoc.org/migrating-from-springfox.html

So I've created a tool to make migration easy.


## Introduction
Upgrade Swagger 2 (aka OpenAPI 2) annotations to OpenAPI 3 annotations.  
You can define your own annotation replace rules in `com.ae.app.mapping` package

**Welcome for contribute**

## How to use
- `mvn package`
- `java -jar target/swagger-annotation-migration-1.0-SNAPSHOT.jar {your-project-path-here}`
- You may need to reformat your code after migration.
  Using Spotless maven plugin to automatically format your code [HERE](https://github.com/diffplug/spotless/tree/main/plugin-maven)

## Supported annotation replacement
@Api → @Tag

@ApiModel → @Schema

@ApiModelProperty → @Schema

@ApiOperation(value = "foo", notes = "bar") → @Operation(summary = "foo", description = "bar")


---Done---
Number means changes
@ApiParam → @Parameter

## Limitations
Does NOT support multi-line annotations.

## TODO
Add Test Cases