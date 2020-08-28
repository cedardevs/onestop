package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.domain.OnestopUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnestopUserRepository extends JpaRepository<OnestopUser, String> {
}
