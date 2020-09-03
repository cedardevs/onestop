package org.cedar.onestop.user.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="onestop_user")
public class OnestopUser {

    @Id //comes from IdP
    @Column(name= "user_id")
    public String id;

    private boolean enabled = false;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "onestop_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<OnestopRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SavedSearch> searches;

    @Column(name = "createdOn", updatable = false)
    @CreationTimestamp
    public Date createdOn; //TODO do we also accept part of the query

    @Column(name = "lastUpdatedOn")
    @UpdateTimestamp
    public Date lastUpdatedOn; ///TODO do we also accept part of the query

    //constructor will be used by Spring JPA
    public OnestopUser() {
    }

    public OnestopUser(String id) {
        this.id = id;
    }

    public OnestopUser(OnestopRole role) {
        this.roles = Arrays.asList(role);
    }

    public OnestopUser(String id, OnestopRole role) {
        this.id = id;
        this.roles = Arrays.asList(role);
    }

    public OnestopUser(String id, Collection<OnestopRole> roles) {
        this.id = id;
        this.roles = roles;
    }

    public OnestopUser(Collection<OnestopRole> roles) {
        this.roles = roles;
    }

    public OnestopUser(String id, HashSet<OnestopRole> roles, boolean enabled) {
        this.id = id;
        this.roles = roles;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public Collection<OnestopRole> getRoles() { return roles; }

    public void setRoles(Collection<OnestopRole> roles) { this.roles = roles; }

    public void addRole(OnestopRole role) { this.roles.add(role); }

    public Set<SavedSearch> getSearches() { return searches; }

    public void setSearches(Set<SavedSearch> searches) { this.searches = searches; }

    public void addSearch(SavedSearch search) { this.searches.add(search); }

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
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", id);
        result.put("roles", roleToStringList());
        result.put("createdOn", createdOn);
        result.put("lastUpdatedOn", lastUpdatedOn);
        return result;
    }

    public List<Map> roleToStringList(){
        List<Map> roleList = new ArrayList<>();
        for(int i = 0; i < roles.size(); i++) {
            roleList.add(((List<OnestopRole>)roles).get(i).toMap());
        }
        return roleList;
    }
}
