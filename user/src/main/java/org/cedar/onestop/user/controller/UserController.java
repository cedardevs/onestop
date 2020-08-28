package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cedar.onestop.user.common.JsonApiData;
import org.cedar.onestop.user.common.JsonApiResponse;
import org.cedar.onestop.user.common.JsonApiSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public OnestopUserRepository onestopUserRepository;

    @Autowired
    public UserController(OnestopUserRepository onestopUserRepository) {
        this.onestopUserRepository = onestopUserRepository;
    }

    @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
    @ApiOperation(value = "Get authenticated user data", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user data"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/user", produces = "application/json")
    public JsonApiResponse getAuthenticatedUser(final @AuthenticationPrincipal Authentication authentication)
            throws RuntimeException {
        String userId = authentication.getName();
        logger.info("Retrieving user data for authenticated user with id : " + userId);
        List<JsonApiData> dataList = new ArrayList<>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("userId", userId);
        JsonApiData dataItem = new JsonApiData.Builder()
          .setId(userId)
          .setAttributes(data)
          .setType("user").build();
        dataList.add(dataItem);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK)
                .setData(dataList).build();
    }

    @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
    @ApiOperation(value = "Create a user", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user data"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/user", produces = "application/json")
    public  JsonApiResponse createUser(@RequestBody OnestopUser user, final @AuthenticationPrincipal Authentication authentication)
            throws RuntimeException {
        String userId = authentication.getName();
        logger.info("Creating new user with id : " + userId);
        user.setId(userId); // id must come from auth object (provided by the IdP)
        OnestopUser savedUser = onestopUserRepository.save(user);
        List<JsonApiData> dataList = new ArrayList<>();
        JsonApiData dataItem = new JsonApiData.Builder()
                .setId(userId)
                .setAttributes(savedUser.toMap())
                .setType("user").build();
        dataList.add(dataItem);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.CREATED)
                .setData(dataList).build();
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "Get user roles (ADMIN)", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user roles"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/roles/{id}", produces = "application/json")
    public  JsonApiResponse getUserRoles(@PathVariable(value = "id") String id, final @AuthenticationPrincipal Authentication authentication)
            throws ResourceNotFoundException {
        logger.info("Retrieving user roles for user id: " + id);
        OnestopUser user = onestopUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));
        logger.info("Retrieved " + user.getRoles().size() + " roles for user id: " + id);
        List<JsonApiData> dataList = new ArrayList<>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("roles", user.getRoles());

        JsonApiData dataItem = new JsonApiData.Builder()
                .setId(user.getId())
                .setAttributes(data)
                .setType("roles").build();
        dataList.add(dataItem);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK)
                .setData(dataList).build();
    }
}
