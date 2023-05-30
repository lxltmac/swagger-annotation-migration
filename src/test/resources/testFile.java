package test;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is just used for testing
 */

@Api(tags = "Api Tags")
@RestController
@RequestMapping("/user")
public class LoginController {

    @PostMapping("/login")
    @ApiOperation(value = "login", notes = "login")
    public Result login(@Validated LoginUser loginUser, @ApiParam(value = "account") String account, @ApiParam("code") String code) {
    }

    @PostMapping("/logout")
    @ApiOperation("logout")
    public Result loginout(@ApiParam("account") String account, @ApiParam(value = "code") String code) {
    }
}

@ApiModel("Api model info")
public class ApiModelInfo {

    private String projectId;
    private String projectName;

    @ApiModelProperty("createTime")
    private Date createTime;
}

@ApiModel(value = "Api model value", description = "api model desc")
public class ApiModelInfo {

    private String projectId;
    private String projectName;
    private String coverPicture;

    @ApiModelProperty(value = "createTime")
    private Date createTime;
}