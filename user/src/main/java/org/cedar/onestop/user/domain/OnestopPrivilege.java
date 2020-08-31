package org.cedar.onestop.user.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name="onestop_privileges")
public class OnestopPrivilege {
    @Id
    @Column(name= "privilege_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    public String id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<OnestopRole> roles;

    protected OnestopPrivilege(){}

    public OnestopPrivilege(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
