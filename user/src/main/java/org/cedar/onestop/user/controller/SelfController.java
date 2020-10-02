package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.JsonApiData;
import org.cedar.onestop.data.api.JsonApiResponse;
import org.cedar.onestop.data.api.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.service.OnestopUserService;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.awt.print.Pageable;
import java.util.Arrays;

@RestController
@RequestMapping("/v1")
public class SelfController {
  private static final Logger logger = LoggerFactory.getLogger(SelfController.class);

  @Autowired
  public OnestopUserService userService;

  @Autowired
  public SelfController(OnestopUserService userService) {
    this.userService = userService;
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_USER_PROFILE})
  @ApiOperation(value = "Get authenticated user data")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user data"),
      @ApiResponse(code = 401, message = "Access denied"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/self", produces = "application/json")
  public JsonApiResponse getAuthenticatedUser(
      final @AuthenticationPrincipal Authentication authentication,
      HttpServletResponse response)
      throws RuntimeException, ResourceNotFoundException {
    String userId = authentication.getName();
    logger.info("Retrieving user data for authenticated user with id : " + userId);
    var result = userService.findUserById(userId)
        .map(u -> new JsonApiData.Builder().setId(userId).setAttributes(u.toMap()).setType("user").build())
        .map(Arrays::asList)
        .orElseThrow(() -> new ResourceNotFoundException("No user found for with id: " + userId));
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_USER_PROFILE})
  @ApiOperation(value = "Get authenticated user data")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user data"),
      @ApiResponse(code = 401, message = "Access denied"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/self/role", produces = "application/json")
  public JsonApiResponse getAuthenticatedUserRoles(
      final @AuthenticationPrincipal Authentication authentication,
      Pageable pageable,
      HttpServletResponse response)
      throws RuntimeException, ResourceNotFoundException {
    String userId = authentication.getName();
    logger.info("Retrieving roles for authenticated user with id: " + userId);
    var result = userService.findUserById(userId)
        .map(u -> new JsonApiData.Builder().setId(userId).setAttributes(u.toMap()).setType("user").build())
        .map(Arrays::asList)
        .orElseThrow(() -> new ResourceNotFoundException("No user found for with id: " + userId));
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

}
