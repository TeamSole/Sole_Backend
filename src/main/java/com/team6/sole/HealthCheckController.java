package com.team6.sole;

import com.team6.sole.global.config.CommonApiResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
@Api(tags = "EB 헬스체크 컨트롤러(무시해주세용)")
public class HealthCheckController {
    @GetMapping
    public CommonApiResponse<Boolean> healthCheck() {
        return CommonApiResponse.of(true);
    }
}
