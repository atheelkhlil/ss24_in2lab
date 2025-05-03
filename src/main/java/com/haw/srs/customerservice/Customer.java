package com.haw.srs.customerservice;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.FetchType;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor//(access = AccessLevel.PRIVATE)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String email;

    private PhoneNumber phoneNumber;

    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinTable(
            name = "customer_courses",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Setter(AccessLevel.NONE)
    private List<Course> courses = new ArrayList<>();

    public Customer(String firstName, String lastName, Gender gender, String email, PhoneNumber phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Customer(String firstName, String lastName, Gender gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = null;
        this.phoneNumber = null;
    }

    public void addCourse(Course course) {
        this.courses.add(course);
        course.getCustomers().add(this);

       // course.setAnzahlTeilnehmer(course.getAnzahlTeilnehmer()+1);
    }

    public void removeCourse(Course course) {
        this.courses.remove(course);
        course.getCustomers().remove(this);
       // course.setAnzahlTeilnehmer(course.getAnzahlTeilnehmer()-1);
    }
}
