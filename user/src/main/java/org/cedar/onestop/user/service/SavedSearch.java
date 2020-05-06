package org.cedar.onestop.user.service;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Domain model class that maps the data stored into save_search table
 * inside database.
 */

@Entity
@Table(name = "savesearch")
public class SavedSearch {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  public String id;

  @NotNull
  @Column(name = "userId", updatable = false )
  public String userId;

  @Column(name = "name", nullable = false)
  public String name;

  @Column(name = "value", nullable = false)
  public String value;

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  public Date createdOn; //TODO do we also accept part of the query

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  public Date lastUpdatedOn; ///TODO do we also accept part of the query

  //constructor will be used by Spring JPA
  protected SavedSearch() {
  }

  //constructor is for creating instances.
  public SavedSearch(String id, String userId, String name, String value, Date createdOn, Date lastUpdatedOn) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.value = value;
    this.createdOn = createdOn;
    this.lastUpdatedOn = lastUpdatedOn;
  }

  //constructor is for creating instances.
  public SavedSearch(String id, String userId, String name, String value) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.value = value;
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

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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

  @Override
  public String toString() {
    return "SaveSearchModel{" +
        "id=" + id +
        ", userId='" + userId + '\'' +
        ", name='" + name + '\'' +
        ", value='" + value + '\'' +
        ", createdOn='" + createdOn + '\'' +
        ", lastLogin='" + lastUpdatedOn + '\'' +
        '}';
  }
}
