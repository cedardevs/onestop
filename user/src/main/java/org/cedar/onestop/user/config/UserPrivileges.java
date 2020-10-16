package org.cedar.onestop.user.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum UserPrivileges {
  //--- default privileges (public users)
  READ_OWN_PROFILE(true),
  CREATE_SAVED_SEARCH(true),
  READ_SAVED_SEARCH(true),
  UPDATE_SAVED_SEARCH(true),
  DELETE_SAVED_SEARCH(true),

  //--- others
  READ_USER,
  CREATE_USER,
  UPDATE_USER,
  CREATE_ROLE,
  READ_ROLES_BY_USER_ID,
  DELETE_ROLE,

  CREATE_PRIVILEGE,
  DELETE_PRIVILEGE,
  READ_PRIVILEGE_BY_USER_ID,

  LIST_SAVED_SEARCH,
  READ_SAVED_SEARCH_BY_USER_ID,
  READ_SAVED_SEARCH_BY_ID,
  LIST_ALL_SAVED_SEARCHES;

  public static final String ROLE_PREFIX = "ROLE_";

  public final boolean isDefault;

  UserPrivileges(boolean isDefault) {
    this.isDefault = isDefault;
  }

  UserPrivileges() {
    this.isDefault = false;
  }

  @Override
  public String toString() {
    return ROLE_PREFIX + name();
  }

  public static List<UserPrivileges> defaults() {
    return Arrays.stream(values()).filter(p -> p.isDefault).collect(Collectors.toUnmodifiableList());
  }

  public static List<UserPrivileges> superusers() {
    return List.of(values());
  }
}
