package com.emt.dms1.user;

import jakarta.persistence.*;

@Entity
@Table(name = "role_privileges")
public class RolePrivilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "privileges")
    private Permission permission;
}
