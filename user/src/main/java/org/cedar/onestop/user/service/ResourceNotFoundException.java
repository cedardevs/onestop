package org.cedar.onestop.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception{

  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String message){
    super(message);
  }
}