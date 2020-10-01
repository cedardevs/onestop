package org.cedar.onestop.user;

import org.cedar.onestop.user.service.OnestopUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
  private static final Logger logger = LoggerFactory.getLogger(SetupDataLoader.class);

  boolean alreadySetup = false;

  @Autowired
  private OnestopUserService userService;

  @Value("${admin:null}")
  private String adminId;

  @Override
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent event) {
    logger.info("onApplicationEvent");
    if (alreadySetup)
      return;

    userService.createAdminPrivilegesIfNotFound();
    userService.createNewUserPrivilegesIfNotFound();

//    if (adminId != null) {
//      userService.findOrCreateAdminUser(adminId);
//    }

    alreadySetup = true;
  }

}