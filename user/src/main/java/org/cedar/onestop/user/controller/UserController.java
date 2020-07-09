package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.service.OnestopUser;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public  ResponseEntity<Map<String, Map<String, String>>> getAuthenticatedUser(final @AuthenticationPrincipal Authentication authentication)
            throws RuntimeException {
        Map<String, Map<String, String>> jsonSpecResponse = new HashMap<String, Map<String, String>>();
        if(authentication != null) {
            String userId = authentication.getName();
            Map<String, String> userData = new HashMap<String, String>();
            userData.put("userId", userId);
//        OnestopUser user = userRepository.getOne(userId);
            //todo find user or create one if not found
            jsonSpecResponse.put("data" , userData);
            return new ResponseEntity(jsonSpecResponse, HttpStatus.OK);
        }
        else{
            Map<String, String> errorMap = new HashMap<String, String>();
            errorMap.put("reason", "Unauthorized");
            jsonSpecResponse.put("error", errorMap);
        }
        return new ResponseEntity(jsonSpecResponse, HttpStatus.UNAUTHORIZED);
//        return jsonSpecResponse;
    }


}
