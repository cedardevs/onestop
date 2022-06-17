package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.JsonApiData;
import org.cedar.onestop.data.api.JsonApiResponse;
import org.cedar.onestop.data.api.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.service.OnestopUserService;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final OnestopUserService userService;

  @Autowired
  public UserController(OnestopUserService userService) {
    this.userService = userService;
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_USER})
  @ApiOperation(value = "Get users (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user data"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/user", produces = "application/json")
  public JsonApiResponse getUsers(
      Pageable pageable,
      HttpServletResponse response)
      throws RuntimeException {
    logger.info("Retrieving users with page params: " + pageable);
    var page = userService.findAll(pageable);
    var results = page.get()
        .map(it -> new JsonApiData.Builder().setId(it.getId()).setAttributes(it.toMap()).setType("user").build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(results).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.CREATE_USER})
  @ApiOperation(value = "Create a user")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully created a user"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping(value = "/user", produces = "application/json")
  public JsonApiResponse createUser(
      @RequestBody OnestopUser user,
      HttpServletResponse response)
      throws RuntimeException {
    logger.info("Creating new user with data: " + user);
    var savedUser = userService.save(user);
    var dataItem = new JsonApiData.Builder()
        .setId(savedUser.getId())
        .setAttributes(savedUser.toMap())
        .setType("user").build();
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.CREATED.value(), response)
        .setData(Collections.singletonList(dataItem)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.UPDATE_USER})
  @ApiOperation(value = "Update user")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully updated the user"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PutMapping(value = "/user/{id}", produces = "application/json")
  public JsonApiResponse updateUser(
      @RequestBody OnestopUser user,
      @PathVariable String id,
      HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Updating user with id : " + id);
    if (!userService.exists(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }
    user.setId(id); // id must come from path
    var savedUser = userService.save(user);
    var dataItem = new JsonApiData.Builder()
        .setId(savedUser.getId())
        .setAttributes(savedUser.toMap())
        .setType("user").build();
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.CREATED.value(), response)
        .setData(Collections.singletonList(dataItem)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_OWN_PROFILE})
  @ApiOperation(value = "Get authenticated user data")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user data"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/self", produces = "application/json")
  public JsonApiResponse getAuthenticatedUser(
      Principal authentication,
      HttpServletResponse response)
      throws RuntimeException, ResourceNotFoundException {
    var userId = authentication.getName();
    logger.info("Retrieving user data for authenticated user with id : " + userId);
    var result = userService.findById(userId)
        .map(u -> new JsonApiData.Builder().setId(u.getId()).setAttributes(u.toMap()).setType("user").build())
        .map(Arrays::asList)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_OWN_PROFILE})
  @ApiOperation(value = "Create or update authenticated user")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully updated self"),
      @ApiResponse(code = 201, message = "Successfully created self"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/self", method = {RequestMethod.POST, RequestMethod.PUT}, produces = "application/json")
  @PreAuthorize("#userInput == null || #userInput.id == null || #userInput.id == #authentication.name")
  public JsonApiResponse upsertAuthenticatedUser(
      @RequestBody(required = false) OnestopUser userInput,
      Principal authentication,
      HttpServletResponse response)
      throws RuntimeException {
    var userId = authentication.getName();
    var userExists = userService.exists(userId);
    logger.info(userExists ? "Updating " : "Creating new " + "user with id: " + userId);
    var userData = userService.findOrCreateUser(userId);
    userData.setId(userId); // ensure id matches authentication name (IdP ID)
    // if/when a user can edit additional attributes of their info, set them here
    var savedUser = userService.save(userData);
    logger.info("Saved user: " + savedUser);
    var dataItem = new JsonApiData.Builder()
        .setId(userId)
        .setAttributes(savedUser.toMap())
        .setType("user").build();
    return new JsonApiSuccessResponse.Builder()
        .setStatus(userExists ? HttpStatus.OK.value() : HttpStatus.CREATED.value(), response)
        .setData(Collections.singletonList(dataItem)).build();
  }

}
