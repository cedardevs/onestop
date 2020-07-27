package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.repository.SavedSearchRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.cedar.onestop.user.service.SavedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class SavedSearchController {

  private SavedSearchRepository savedSearchRepository;

  @Autowired
  public SavedSearchController(SavedSearchRepository savedSearchRepository) {
    this.savedSearchRepository = savedSearchRepository;
  }

  @ApiOperation(value = "View all available save searches (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search/all", method = RequestMethod.GET, produces = "application/json")
  public List<SavedSearch> getAll() {
    return savedSearchRepository.findAll();
  }

  //TODO do we need it?
  @ApiOperation(value = "Search with an ID (ADMIN)", response = SavedSearch.class)
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.GET, produces = "application/json")
  public ResponseEntity<SavedSearch> getById(@PathVariable(value = "id") String id)
      throws ResourceNotFoundException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
    return ResponseEntity.ok().body(savedSearch);
  }

  @ApiOperation(value = "View all available save searches by UserId (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search/user/{userId}", method = RequestMethod.GET, produces = "application/json")
  public  List<SavedSearch> getByUserId(@PathVariable(value = "userId") String userId)
      throws RuntimeException {
    return savedSearchRepository.findAllByUserId(userId);
  }

  @ApiOperation(value = "View all user searches", response = Iterable.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successfully retrieved list"),
          @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search", method = RequestMethod.GET, produces = "application/json")
  public  ResponseEntity<?> getByUserId(final @AuthenticationPrincipal Authentication authentication)
          throws RuntimeException {
    if(authentication != null) {
      Map<String, List> jsonSpecResponse = new HashMap<>();
      String userId = authentication.getName();
      Map<String, String> userData = new HashMap<String, String>();
      jsonSpecResponse.put("data" , savedSearchRepository.findAllByUserId(userId));
      return new ResponseEntity<>(jsonSpecResponse, HttpStatus.OK);
    }
    else{
      Map<String, Map<String, String>> jsonSpecErrorResponse = new HashMap<>();
      Map<String, String> errorMap = new HashMap<String, String>();
      errorMap.put("reason", "Unauthorized");
      jsonSpecErrorResponse.put("error", errorMap);
      return new ResponseEntity<>(jsonSpecErrorResponse, HttpStatus.UNAUTHORIZED);
    }
  }
  @ApiOperation(value = "Add user search")
  @RequestMapping(value = "/saved-search", method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<?> create(@RequestBody SavedSearch savedSearch, final @AuthenticationPrincipal Authentication authentication) {
    if(authentication != null) {
      Map<String, SavedSearch> jsonSpecResponse = new HashMap<>();
      String userId = authentication.getName();
      savedSearch.setUserId(userId);
      jsonSpecResponse.put("data" , savedSearchRepository.save(savedSearch));
      return new ResponseEntity<>(jsonSpecResponse, HttpStatus.OK);
    }
    else{
      Map<String, Map<String, String>> jsonSpecErrorResponse = new HashMap<>();
      Map<String, String> errorMap = new HashMap<String, String>();
      errorMap.put("reason", "Unauthorized");
      jsonSpecErrorResponse.put("error", errorMap);
      return new ResponseEntity<>(jsonSpecErrorResponse, HttpStatus.UNAUTHORIZED);
    }
  }

  @ApiOperation(value = "Update user saved search")
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.PUT, produces = "application/json")
  public ResponseEntity<SavedSearch> update(@PathVariable(value = "id") String id,
                                            @Valid @RequestBody SavedSearch savedSearchDetails) throws ResourceNotFoundException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));

    savedSearch.setUserId(savedSearchDetails.getUserId());
    savedSearch.setName(savedSearchDetails.getName());
    savedSearch.setValue(savedSearchDetails.getValue());
    savedSearch.setCreatedOn(savedSearchDetails.getCreatedOn());
    savedSearch.setLastUpdatedOn(savedSearchDetails.getLastUpdatedOn());
    final SavedSearch updatedSavedSearch = savedSearchRepository.save(savedSearch);
    return ResponseEntity.ok(updatedSavedSearch);
  }

//todo more to do here so users cannot delete each others request
  @ApiOperation(value = "Delete saved search")
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.DELETE, produces = "application/json")
  public ResponseEntity<?> delete(@PathVariable(value = "id") String id, final @AuthenticationPrincipal Authentication authentication)
          throws ResourceNotFoundException {
    if(authentication != null) {
      Map<String, Map<String, Boolean>> jsonSpecResponse = new HashMap<>();
      SavedSearch savedSearch = savedSearchRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
      savedSearchRepository.delete(savedSearch);
      Map<String, Boolean> response = new HashMap<>();
      response.put("deleted", Boolean.TRUE);
      jsonSpecResponse.put("data" , response);
      return new ResponseEntity<>(jsonSpecResponse, HttpStatus.OK);
    }else{
      Map<String, Map<String, String>> jsonSpecErrorResponse = new HashMap<>();
      Map<String, String> errorMap = new HashMap<String, String>();
      errorMap.put("reason", "Unauthorized");
      jsonSpecErrorResponse.put("error", errorMap);
      return new ResponseEntity<>(jsonSpecErrorResponse, HttpStatus.UNAUTHORIZED);
    }
  }
}
