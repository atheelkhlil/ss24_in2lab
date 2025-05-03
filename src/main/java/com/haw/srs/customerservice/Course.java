package com.haw.srs.customerservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor // (access = AccessLevel.PRIVATE)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    @JsonIgnore
    @ManyToMany( mappedBy = "courses",cascade = CascadeType.ALL,
    fetch = FetchType.EAGER)
    private List<Customer> customers = new ArrayList<>();

    // @Formula("select count (*) from customer_courses where course_id=id")
    private Integer anzahlTeilnehmer;

    public Course(String name) {
        this.name = name;
        this.anzahlTeilnehmer=0;

    }


}
