package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.JsonApiData;
import org.cedar.onestop.data.api.JsonApiMeta;
import org.cedar.onestop.data.api.JsonApiResponse;
import org.cedar.onestop.data.api.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
public class PrivilegeController {
  private static final Logger logger = LoggerFactory.getLogger(PrivilegeController.class);

  private final OnestopPrivilegeRepository privilegeRepository;

  @Autowired
  public PrivilegeController(OnestopPrivilegeRepository privilegeRepository) {
    this.privilegeRepository = privilegeRepository;
  }

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_PRIVILEGE_BY_USER_ID)
  @ApiOperation(value = "Get user privileges (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user privileges"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/privilege", produces = "application/json")
  public JsonApiResponse getPrivileges(Pageable pageable, HttpServletResponse response) {
    logger.info("Retrieving privileges w/ page params: " + pageable);
    var page = privilegeRepository.findAll(pageable);
    var total = page.getTotalElements();
    logger.info("Retrieved " + page.getSize() + " of " + total + " total privileges");
    List<JsonApiData> dataList = page.get()
        .map(p -> new JsonApiData.Builder().setId(p.getId()).setType("privilege").setAttributes(p.toMap()).build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(dataList).build();
  }

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_PRIVILEGE_BY_USER_ID)
  @ApiOperation(value = "Get user privilege by id (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user privileges"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/privilege/{id}", produces = "application/json")
  public JsonApiResponse getPrivilege(@PathVariable(value = "id") String id, HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Retrieving privilege w/ id: " + id);
    var result = privilegeRepository.findById(id);
    List<JsonApiData> dataList = result
        .map(r -> new JsonApiData.Builder().setId(r.getId()).setType("privilege").setAttributes(r.toMap()).build())
        .map(Arrays::asList)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));
    logger.info("Retrieved privilege w/ id" + id);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(dataList).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.CREATE_PRIVILEGE})
  @ApiOperation(value = "Create a privilege")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully created privilege"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping(value = "/privilege", produces = "application/json")
  public JsonApiResponse createPrivilege(@RequestBody OnestopPrivilege privilege, HttpServletResponse response)
      throws RuntimeException {
    logger.info("Creating new privilege : " + privilege.getName());
    OnestopPrivilege savedPrivilege = privilegeRepository.save(privilege);
    logger.info("Created privilege : " + savedPrivilege.getId() + ":" + savedPrivilege.getName());
    List<JsonApiData> dataList = new ArrayList<>();
    JsonApiData dataItem = new JsonApiData.Builder()
        .setId(savedPrivilege.getId())
        .setAttributes(savedPrivilege.toMap())
        .setType("privilege").build();
    dataList.add(dataItem);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.CREATED.value(), response)
        .setData(dataList).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.DELETE_PRIVILEGE})
  @ApiOperation(value = "Delete privilege")
  @DeleteMapping(value = "/privilege/{id}", produces = "application/json")
  public JsonApiResponse deletePrivilege(@PathVariable(value = "id") String id, HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received privilege DELETE request for id: " + id);
    OnestopPrivilege privilege = privilegeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Privilege not found for requested id :: " + id));
    logger.info("Deleting privilege with id: " + privilege.getId());
    privilegeRepository.delete(privilege);
    logger.info("Delete complete for privilege id: " + id);
    Map<String, Boolean> result = new HashMap<>();
    result.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
  }

  @ApiOperation(value = "Get authenticated user privileges")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user privileges"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_OWN_PROFILE})
  @GetMapping("/self/privilege")
  public JsonApiResponse getAuthenticatedUserPrivileges(
      final @AuthenticationPrincipal Authentication authentication,
      Pageable pageable,
      HttpServletResponse response)
      throws RuntimeException {
    String userId = authentication.getName();
    logger.info("Retrieving privileges for authenticated user with id: " + userId);
    var result = privilegeRepository.findByRolesUsersId(userId, pageable).get()
        .map(it -> new JsonApiData.Builder().setId(it.getId()).setAttributes(it.toMap()).setType("privilege").build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

}
