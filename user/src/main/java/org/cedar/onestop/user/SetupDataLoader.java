package org.cedar.onestop.user;

import org.cedar.onestop.user.service.OnestopUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class SetupDataLoader {
  private static final Logger logger = LoggerFactory.getLogger(SetupDataLoader.class);

  private boolean alreadySetup = false;

  private final OnestopUserService userService;
  private final List<String> adminIds;

  @Autowired
  public SetupDataLoader(OnestopUserService userService, @Value("${admins:}") List<String> adminIds) {
    this.userService = userService;
    this.adminIds = adminIds;
  }

  @PostConstruct
  @Transactional
  public void createAdmins() {
    if (!alreadySetup && adminIds != null && !adminIds.isEmpty()) {
      logger.info("initializing " + adminIds.size() + " configured admin users");
      adminIds.forEach(id -> userService.createDefaultUser(id, true));
      alreadySetup = true;
    }
  }

}