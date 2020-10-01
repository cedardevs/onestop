package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.*;
import org.cedar.onestop.user.config.SecurityConfig;
import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/v1")
public class PrivilegeController {
  Logger logger = LoggerFactory.getLogger(PrivilegeController.class);

  @Autowired
  public OnestopUserRepository onestopUserRepository;

  @Autowired
  public OnestopPrivilegeRepository onestopPrivilegeRepository;

  @Secured(SecurityConfig.ROLE_PREFIX + SecurityConfig.READ_PRIVILEGE_BY_USER_ID)
  @ApiOperation(value = "Get user privileges (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully retrieved user privileges"),
    @ApiResponse(code = 401, message = "Access denied"),
    @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/privilege/{id}", produces = "application/json")
  public JsonApiResponse getUserPrivileges(@PathVariable(value = "id") String id, final @AuthenticationPrincipal Authentication authentication, HttpServletResponse response)
    throws ResourceNotFoundException {
    logger.info("Retrieving user privileges for user id: " + id);
    OnestopUser user = onestopUserRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));
    Collection<OnestopPrivilege> privs = new ArrayList<>();
    Collection<OnestopRole> roles = user.getRoles();
    roles.forEach(role -> privs.addAll(role.getPrivileges()));
    logger.info("Retrieved " + privs.size() + " privileges for user id: " + id);
    List<JsonApiData> dataList = new ArrayList<>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("privileges", privs);

    JsonApiData dataItem = new JsonApiData.Builder()
      .setId(user.getId())
      .setAttributes(data)
      .setType("privileges").build();
    dataList.add(dataItem);
    return new JsonApiSuccessResponse.Builder()
      .setStatus(HttpStatus.OK.value(), response)
      .setData(dataList).build();
  }

  @Secured({SecurityConfig.ROLE_PREFIX + SecurityConfig.CREATE_PRIVILEGE})
  @ApiOperation(value = "Create a privilege", response = Iterable.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully created privilege"),
    @ApiResponse(code = 401, message = "Access denied"),
    @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping(value = "/privilege", produces = "application/json")
  public  JsonApiResponse createPrivilege(@RequestBody OnestopPrivilege privilege, HttpServletResponse response)
    throws RuntimeException {
    logger.info("Creating new privilege : " + privilege.getName());
    OnestopPrivilege savedPrivilege = onestopPrivilegeRepository.save(privilege);
    logger.info("Created privilege : "+ savedPrivilege.getId()+ ":" + savedPrivilege.getName());
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

  @Secured({SecurityConfig.ROLE_PREFIX + SecurityConfig.DELETE_PRIVILEGE})
  @ApiOperation(value = "Delete privilege")
  @DeleteMapping(value = "/privilege/{id}", produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id, HttpServletResponse response)
    throws ResourceNotFoundException {
    logger.info("Received privilege DELETE request");
    logger.info(id);
    OnestopPrivilege privilege = onestopPrivilegeRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Privilege not found for requested id :: " + id));
    logger.info("Deleting privilege with id: " + privilege.getId());
    onestopPrivilegeRepository.delete(privilege);
    logger.info("Delete complete for privilege id: " + id);
    Map<String, Boolean> result = new HashMap<>();
    result.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
      .setStatus(HttpStatus.OK.value(), response)
      .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
  }

}
