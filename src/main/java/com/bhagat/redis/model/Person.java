package com.bhagat.redis.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

//@formatter:off
@Data
@EqualsAndHashCode(exclude = { "children" })
@RedisHash("persons")
@NoArgsConstructor
public class Person {
    private @Id String id;
    private @Indexed String firstname;
    private @Indexed String lastname;

    private Gender gender;
    private Address address;
    private @Reference List<Person> children;

    public Person(String firstname, String lastname, Gender gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;
    }
}
