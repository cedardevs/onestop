package org.cedar.onestop.user.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model class that maps the data stored into save_search table
 * inside database.
 */

@Entity
@Table(name = "onestop_search")
public class SavedSearch {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  public String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "onestop_user", nullable = false)
  private OnestopUser user;

  @Column(name = "name", nullable = false)
  public String name;

  @Column(name = "value", nullable = false)
  public String value;

  @Column(name = "filter")
  public String filter;

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  public Date createdOn; //TODO do we also accept part of the query

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  public Date lastUpdatedOn; ///TODO do we also accept part of the query

  //constructor will be used by Spring JPA
  protected SavedSearch() {
  }


  protected SavedSearch(OnestopUser user) {
    this.user = user;
  }

  //constructor is for creating instances.
  public SavedSearch(OnestopUser user, String id, String name, String value, String filter,  Date createdOn, Date lastUpdatedOn) {
    this.user = user;
    this.id = id;
    this.name = name;
    this.value = value;
    this.filter = filter;
    this.createdOn = createdOn;
    this.lastUpdatedOn = lastUpdatedOn;
  }

  //constructor is for creating instances.
  public SavedSearch(OnestopUser user, String id, String name, String filter, String value) {
    this.user = user;
    this.id = id;
    this.name = name;
    this.filter = filter;
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

  public void setUser(OnestopUser user){
    this.user = user;
  }

  public OnestopUser getUser() {
    return user;
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

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
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
        ", name='" + name + '\'' +
        ", value='" + value + '\'' +
        ", createdOn='" + createdOn + '\'' +
        ", lastLogin='" + lastUpdatedOn + '\'' +
        '}';
  }

  public Map<String, Object> toMap() {
    //TODO should null fields be included?
    Map result = new HashMap();
    result.put("id", id);
    result.put("name", name);
    result.put("value", value);
    result.put("filter", filter);
    result.put("createdOn", createdOn);
    result.put("lastUpdatedOn", lastUpdatedOn);
    return result;
  }
}
