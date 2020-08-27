package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.service.OneStopRoles;
import org.cedar.onestop.user.service.OnestopUser;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cedar.onestop.user.common.JsonApiData;
import org.cedar.onestop.user.common.JsonApiErrorResponse;
import org.cedar.onestop.user.common.JsonApiResponse;
import org.cedar.onestop.user.common.JsonApiSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    OnestopUserRepository onestopUserRepository;

    @Autowired
    public UserController(OnestopUserRepository onestopUserRepository) {
        this.onestopUserRepository = onestopUserRepository;
    }

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
            logger.info("getAuthenticatedUser : authentication != null");
            logger.info(authentication.getName());
            logger.info(authentication.getAuthorities().toString());
            String userId = authentication.getName();
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

    @ApiOperation(value = "Get authenticated user data", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user data"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/user", method = RequestMethod.POST, produces = "application/json")
    public  ResponseEntity<Map<String, ?>> createUser(@RequestBody OnestopUser user, final @AuthenticationPrincipal Authentication authentication)
            throws RuntimeException {
        if(authentication != null) {
            Map<String, OnestopUser> jsonSpecResponse = new HashMap<String, OnestopUser>();
            String userId = authentication.getName();
            user.setId(userId); // id must come from auth object (provided by the IdP)
//        OnestopUser user = userRepository.getOne(userId);
            //todo find user or create one if not found
            OnestopUser savedUser = onestopUserRepository.save(user);
            jsonSpecResponse.put("data" , savedUser);

            return new ResponseEntity<>(jsonSpecResponse, HttpStatus.OK);
        }
        else{
            Map<String, Map<String, String>> jsonSpecResponse = new HashMap<String, Map<String, String>>();
            Map<String, String> errorMap = new HashMap<String, String>();
            errorMap.put("reason", "Unauthorized");
            jsonSpecResponse.put("error", errorMap);
            return new ResponseEntity(jsonSpecResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "Get user roles (ADMIN)", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user roles"),
            @ApiResponse(code = 401, message = "Access denied"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/roles/{id}", method = RequestMethod.GET, produces = "application/json")
    public  ResponseEntity<Map<String, ?>> getUserRoles(@PathVariable(value = "id") String id, final @AuthenticationPrincipal Authentication authentication)
            throws ResourceNotFoundException {
        logger.info("getUserRoles");
        if(authentication != null) {
            logger.info("authentication != null");
            logger.info(authentication.getName());
            logger.info(authentication.getAuthorities().toString());

            Map<String, Collection<OneStopRoles>> jsonSpecResponse = new HashMap<String, Collection<OneStopRoles>>();
            OnestopUser user = onestopUserRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));
            logger.info("user.getRoles()");
            logger.info(user.getRoles().toString());

            jsonSpecResponse.put("data", user.getRoles());
            return new ResponseEntity(jsonSpecResponse, HttpStatus.OK);
        }else{
            Map<String, Map<String, String>> jsonErrorSpecResponse = new HashMap<String, Map<String, String>>();
            Map<String, String> errorMap = new HashMap<String, String>();
            errorMap.put("reason", "Unauthorized");
            jsonErrorSpecResponse.put("error", errorMap);
            return new ResponseEntity(jsonErrorSpecResponse, HttpStatus.UNAUTHORIZED);
        }
    }
}
