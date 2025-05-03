package com.haw.srs.customerservice.Exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class CourseNotFoundException extends Exception {

    private final long courseNumber;

    public CourseNotFoundException(long courseNumber) {
        super(String.format("Could not find course with number %d.", courseNumber));

        this.courseNumber = courseNumber;
    }
}