package de.moneymanager.accounts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@MappedSuperclass
@Getter
@Setter(AccessLevel.PACKAGE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long    id;
    @Column(nullable = false)
    private String  name;
    @Column(nullable = false)
    private Boolean enable = true;

}