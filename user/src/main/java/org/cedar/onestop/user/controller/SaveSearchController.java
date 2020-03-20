package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.cedar.onestop.user.service.SaveSearch;
import org.cedar.onestop.user.service.SaveSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SaveSearchController {

  @Autowired
  SaveSearchRepository saveSearchRepository;

  @ApiOperation(value = "View all available save searches (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/savesearches", method = RequestMethod.GET, produces = "application/json")
  public List<SaveSearch> getAll() {
    return saveSearchRepository.findAll();
  }

  //TODO do we need it?
  @ApiOperation(value = "Search with an ID (ADMIN)", response = SaveSearch.class)
  @RequestMapping(value = "/savesearches/{id}", method = RequestMethod.GET, produces = "application/json")
  public ResponseEntity<SaveSearch> getById(@PathVariable(value = "id") String id)
      throws ResourceNotFoundException {
    SaveSearch saveSearch = saveSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
    return ResponseEntity.ok().body(saveSearch);
  }

  @ApiOperation(value = "View all available save searches by UserId", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/savesearches/user/{userId}", method = RequestMethod.GET, produces = "application/json")
  public  List<SaveSearch> getByUserId(@PathVariable(value = "userId") String userId)
      throws RuntimeException {
    return saveSearchRepository.findAllByUserId(userId);
  }

  @ApiOperation(value = "Add user searches")
  @RequestMapping(value = "/savesearches", method = RequestMethod.POST, produces = "application/json")
  public SaveSearch create(@Valid @RequestBody SaveSearch saveSearch) {
    return saveSearchRepository.save(saveSearch);
  }


  @ApiOperation(value = "Update user saved search")
  @RequestMapping(value = "/savesearches/{id}", method = RequestMethod.PUT, produces = "application/json")
  public ResponseEntity<SaveSearch> update(@PathVariable(value = "id") String id,
                                                 @Valid @RequestBody SaveSearch saveSearchDetails) throws ResourceNotFoundException {
    SaveSearch saveSearch = saveSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));

    saveSearch.setUserId(saveSearchDetails.getUserId());
    saveSearch.setName(saveSearchDetails.getName());
    saveSearch.setValue(saveSearchDetails.getValue());
    saveSearch.setCreatedOn(saveSearchDetails.getCreatedOn());
    saveSearch.setLastUpdatedOn(saveSearchDetails.getLastUpdatedOn());
    final SaveSearch updatedSaveSearch = saveSearchRepository.save(saveSearch);
    return ResponseEntity.ok(updatedSaveSearch);
  }

  @ApiOperation(value = "Delete saved search")
  @RequestMapping(value = "/savesearches/{id}", method = RequestMethod.DELETE, produces = "application/json")
  public Map<String, Boolean> deleteById(@PathVariable(value = "id") String id)
      throws ResourceNotFoundException {
    SaveSearch saveSearch = saveSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));

    saveSearchRepository.delete(saveSearch);
    Map<String, Boolean> response = new HashMap<>();
    response.put("deleted", Boolean.TRUE);
    return response;
  }

}
