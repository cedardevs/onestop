package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.cedar.onestop.user.common.JsonApiData;
import org.cedar.onestop.user.common.JsonApiErrorResponse;
import org.cedar.onestop.user.common.JsonApiResponse;
import org.cedar.onestop.user.common.JsonApiSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    public UserController() { }

    @ApiOperation(value = "Get authenticated user data", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user data"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = "application/json")
    public JsonApiResponse getAuthenticatedUser(final @AuthenticationPrincipal Authentication authentication)
            throws RuntimeException {
        if(authentication != null) {
            String userId = authentication.getName();
//        OnestopUser user = userRepository.getOne(userId);
            //todo find user or create one if not found?
            List<JsonApiData> dataList = new ArrayList<>();
            JsonApiData dataItem = new JsonApiData.Builder()
              .setId(userId)
              .setType("user").build();
            dataList.add(dataItem);
            return new JsonApiSuccessResponse.Builder()
              .setData(dataList).build();
        }
        else{
            return new JsonApiErrorResponse.Builder().setCode("Unauthorized").setStatus(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
