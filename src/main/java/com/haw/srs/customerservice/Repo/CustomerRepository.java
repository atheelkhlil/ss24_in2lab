package com.haw.srs.customerservice.Repo;

import com.haw.srs.customerservice.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByLastName(String lastName);

    Optional<Customer> findCustomerByFirstName(String lastName);

   // Optional<Customer> findById(Long id);

}
