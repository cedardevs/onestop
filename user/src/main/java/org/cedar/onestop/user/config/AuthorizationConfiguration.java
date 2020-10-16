package org.cedar.onestop.user.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

//
//@ConfigurationProperties('login-gov')
//class LoginGovConfiguration {
//
//  static class Keystore {
//    String alias
//    String file
//    String password
//    String type
//  }
//
//  Keystore keystore
//  String allowedOrigin
//  String loginSuccessRedirect
//  String loginFailureRedirect
//  String logoutSuccessRedirect
//}
@Configuration
public class AuthorizationConfiguration {

  final public static String ROLE_PREFIX = "ROLE_";
  //the role and privilege assigned to new users
  final public static String PUBLIC_ROLE = "PUBLIC";
  final public static String READ_OWN_PROFILE = "READ_OWN_PROFILE";
  final public static String CREATE_SAVED_SEARCH = "CREATE_SAVED_SEARCH";
  final public static String READ_SAVED_SEARCH = "READ_SAVED_SEARCH";
  final public static String UPDATE_SAVED_SEARCH = "UPDATE_SAVED_SEARCH";
  final public static String DELETE_SAVED_SEARCH = "DELETE_SAVED_SEARCH";
  final public static List<String> NEW_USER_PRIVILEGES = Arrays.asList(
      READ_OWN_PROFILE,
      CREATE_SAVED_SEARCH,
      READ_SAVED_SEARCH,
      DELETE_SAVED_SEARCH
  );

  //the role and privileges of the admin user
  final public static String ADMIN_ROLE = "ADMIN";
  final public static String READ_USER = "READ_USER";
  final public static String CREATE_USER = "CREATE_USER";
  final public static String UPDATE_USER = "UPDATE_USER";
  final public static String CREATE_ROLE = "CREATE_ROLE";
  final public static String READ_ROLES_BY_USER_ID = "READ_ROLES_BY_USER_ID";
  final public static String DELETE_ROLE = "DELETE_ROLE";

  final public static String CREATE_PRIVILEGE = "CREATE_PRIVILEGE";
  final public static String DELETE_PRIVILEGE = "DELETE_PRIVILEGE";
  final public static String READ_PRIVILEGE_BY_USER_ID = "READ_PRIVILEGE_BY_USER_ID";

  final public static String LIST_SAVED_SEARCH = "LIST_SAVED_SEARCH";
  final public static String READ_SAVED_SEARCH_BY_USER_ID = "READ_SAVED_SEARCH_BY_USER_ID";
  final public static String READ_SAVED_SEARCH_BY_ID = "READ_SAVED_SEARCH_BY_ID";
  final public static String LIST_ALL_SAVED_SEARCHES = "LIST_ALL_SAVED_SEARCHES";
  final public static List<String> ADMIN_PRIVILEGES = Arrays.asList(
      CREATE_USER,
      UPDATE_USER,
      CREATE_ROLE,
      READ_ROLES_BY_USER_ID,
      CREATE_PRIVILEGE,
      READ_PRIVILEGE_BY_USER_ID,
      DELETE_PRIVILEGE,
      LIST_SAVED_SEARCH,
      READ_SAVED_SEARCH_BY_USER_ID,
      READ_SAVED_SEARCH_BY_ID,
      LIST_ALL_SAVED_SEARCHES
  );
}

