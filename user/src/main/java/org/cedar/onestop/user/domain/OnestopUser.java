package org.cedar.onestop.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "onestop_user")
public class OnestopUser {

  @Id //comes from IdP
  @Column(name = "id")
  private String id;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "onestop_users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private List<OnestopRole> roles = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SavedSearch> searches = new ArrayList<>();

  @Column(updatable = false)
  @CreationTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdOn;

  @UpdateTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedOn;

  //constructor will be used by Spring JPA
  public OnestopUser() {
  }

  public OnestopUser(String id) {
    this.id = id;
  }

  public OnestopUser(String id, OnestopRole role) {
    this.id = id;
    this.roles = role != null ? Collections.singletonList(role) : new ArrayList<>();
  }

  public OnestopUser(String id, List<OnestopRole> roles) {
    this.id = id;
    this.roles = roles != null ? roles : new ArrayList<>();
  }

  @PrePersist
  protected void onCreate() {
    createdOn = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdatedOn = Instant.now();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Instant getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  @Nonnull
  public List<OnestopRole> getRoles() {
    return roles;
  }

  public void setRoles(List<OnestopRole> roles) {
    this.roles = roles != null ? roles : new ArrayList<>();
  }

  public void addRole(OnestopRole role) {
    this.roles.add(role);
  }

  @Nonnull
  public List<SavedSearch> getSearches() {
    return searches;
  }

  public void setSearches(List<SavedSearch> searches) {
    this.searches = searches != null ? searches : new ArrayList<>();
  }

  public void addSearch(SavedSearch search) {
    this.searches.add(search);
  }

  public List<OnestopPrivilege> getPrivileges() {
    return roles.stream()
        .flatMap(r -> r.getPrivileges().stream())
        .collect(Collectors.toList());
  }

  public List<GrantedAuthority> getPrivilegesAsAuthorities() {
    return getPrivileges().stream()
        .map(OnestopPrivilege::toString)
        .map(priv -> AuthorizationConfiguration.ROLE_PREFIX + priv)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "OnestopUserModel{" +
        "id=" + id +
        ", roles='" + roles.toString() + '\'' +
        ", createdOn='" + createdOn + '\'' +
        ", lastLogin='" + lastUpdatedOn + '\'' +
        '}';
  }

  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<>();
    result.put("id", id);
    result.put("roles", rolesToMapList());
    result.put("createdOn", createdOn);
    result.put("lastUpdatedOn", lastUpdatedOn);
    return result;
  }

  public List<Map<String, Object>> rolesToMapList() {
    return roles.stream()
        .map(OnestopRole::toMap)
        .collect(Collectors.toList());
  }
}
