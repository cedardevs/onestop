package org.cedar.onestop.user.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "onestop_user")
public class OnestopUser {

  @Id //comes from IdP
  @Column(name = "id")
  public String id;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "onestop_users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private List<OnestopRole> roles = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
      cascade = CascadeType.ALL)
  private List<SavedSearch> searches = new ArrayList<>();

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  public Date createdOn;

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  public Date lastUpdatedOn;

  //constructor will be used by Spring JPA
  public OnestopUser() {
  }

  public OnestopUser(String id) {
    this.id = id;
  }

  public OnestopUser(String id, OnestopRole role) {
    this.id = id;
    this.roles = role != null ? Arrays.asList(role) : new ArrayList<>();
  }

  public OnestopUser(String id, List<OnestopRole> roles) {
    this.id = id;
    this.roles = roles != null ? roles : new ArrayList<>();
  }

  @PrePersist
  protected void onCreate() {
    createdOn = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdatedOn = new Date();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(Date lastUpdatedOn) {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
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
