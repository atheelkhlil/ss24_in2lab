package com.haw.srs.customerservice.Exception;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Value
@EqualsAndHashCode(callSuper=false)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomerAlreadyExistingException extends Exception {

    private final String lastName;

    public CustomerAlreadyExistingException(String lastName) {
        super(String.format("Customer with name %s does already exist.", lastName));

        this.lastName = lastName;
    }
}