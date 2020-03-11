package org.cedar.onestop.user.controller;

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

  @GetMapping("/savesearchs")
  public List<SaveSearch> getAll() {
    return saveSearchRepository.findAll();
  }

  @GetMapping("/savesearchs/{id}")
  public ResponseEntity<SaveSearch> getById(@PathVariable(value = "id") String id)
      throws ResourceNotFoundException {
    SaveSearch saveSearch = saveSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
    return ResponseEntity.ok().body(saveSearch);
  }

//  @GetMapping("/savesearchs/user/{userId}")
//  public ResponseEntity<SaveSearch> getByUserId(@PathVariable(value = "userId") String userId)
//      throws ResourceNotFoundException {
//    SaveSearch saveSearch = (SaveSearch) saveSearchRepository.findAllById(Long.parseLong(userId)
//        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + userId));
//    return ResponseEntity.ok().body(saveSearch);
//  }

  @PostMapping("/savesearchs")
  public SaveSearch create(@Valid @RequestBody SaveSearch saveSearch) {
    return saveSearchRepository.save(saveSearch);
  }

  @PutMapping("/savesearchs/{id}")
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

  @DeleteMapping("/savesearchs/{id}")
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
