# Swagger 2 自动升级到 OpenAPI 3 注解

```
注意!!!
请在开始使用之前备份你的项目。
这个工具将会在你项目中替换Swagger注解。
```

## 介绍

将SpringFox（Swagger2）的注解升级到Springdoc的注解。
你可以自定义注解替换规则，在`com.ae.app.mapping`包中定义
**欢迎贡献**

## 要求
- JDK 11
- Apache Maven 3.8.1

## 如何使用
- `mvn package`
- `java -jar target/swagger-annotation-migration-1.0-SNAPSHOT.jar {your-project-path-here}`
- You may need to reformat your code after migration.
  Using Spotless maven plugin to automatically format your code [HERE](https://github.com/diffplug/spotless/tree/main/plugin-maven)

## 支持的注解替换
@Api → @Tag

@ApiModel → @Schema

@ApiModelProperty → @Schema

@ApiOperation(value = "foo", notes = "bar") → @Operation(summary = "foo", description = "bar")

@ApiParam → @Parameter