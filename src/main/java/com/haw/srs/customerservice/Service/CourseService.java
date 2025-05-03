package com.haw.srs.customerservice.Service;

import com.haw.srs.customerservice.Course;
import com.haw.srs.customerservice.Customer;
import com.haw.srs.customerservice.Exception.CourseNotFoundException;
import com.haw.srs.customerservice.Exception.CustomerNotFoundException;
import com.haw.srs.customerservice.Exception.MembershipMailNotSent;
import com.haw.srs.customerservice.MailGateway;
import com.haw.srs.customerservice.Repo.CourseRepository;
import com.haw.srs.customerservice.Repo.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MailGateway mailGateway;

    @Transactional
    public void enrollInCourse(String lastName, Course course) throws CustomerNotFoundException {
        Customer customer = customerRepository
                .findByLastName(lastName)
                .orElseThrow(() -> new CustomerNotFoundException(lastName));
        course.setAnzahlTeilnehmer(course.getAnzahlTeilnehmer() + 1);
        courseRepository.save(course);
        customer.addCourse(course);
        customerRepository.save(customer);
    }

    @Transactional
    public void transferCourses(String fromCustomerLastName, String toCustomerLastName) throws CustomerNotFoundException {
        Customer from = customerRepository
                .findByLastName(fromCustomerLastName)
                .orElseThrow(() -> new CustomerNotFoundException(fromCustomerLastName));
        Customer to = customerRepository
                .findByLastName(toCustomerLastName)
                .orElseThrow(() -> new CustomerNotFoundException(toCustomerLastName));

        to.getCourses().addAll(from.getCourses());
        from.getCourses().clear();
        //wenn übereinstimmung der kurse anzahlteilnehmer updaten
        customerRepository.save(from);
        customerRepository.save(to);
    }

    /**
     * Cancels a course membership. An Email is sent to all possible participants on the waiting list for this course.
     * If customer is not member of the provided course, the operation is ignored.
     *
     * @throws IllegalArgumentException if customerNumber==null or courseNumber==null
     */
    @Transactional
    public void cancelMembership(Long customerNumber, Long courseNumber) throws CustomerNotFoundException, CourseNotFoundException, MembershipMailNotSent {

        // some implementation goes here
        // find customer, find course, look for membership, remove membership, etc.
        if(customerNumber==null || courseNumber==null){
            throw new IllegalArgumentException();
        }
        Customer customer= customerRepository.findById(customerNumber).orElseThrow(()->new CustomerNotFoundException(customerNumber));
        Course course = courseRepository.findById(courseNumber).orElseThrow(()->new CourseNotFoundException(courseNumber));
        if(!customer.getCourses().contains(course)){
           return;
        }

        String customerMail = customer.getEmail();

        boolean mailWasSent = mailGateway.sendMail(customerMail, "Oh, we're sorry that you canceled your membership!", "Some text to make her/him come back again...");
        if (!mailWasSent) {
            // do some error handling here (including e.g. transaction rollback, etc.)
            // ...
            
            throw new MembershipMailNotSent(customerMail);
        }
        course.setAnzahlTeilnehmer(course.getAnzahlTeilnehmer()-1);
        customer.removeCourse(course);
        customerRepository.save(customer);
        courseRepository.save(course);
    }

//    public Course findCourseByName(String lastName) throws CourseNotFoundException {
//        return courseRepository
//                .findCourseByName(lastName)
//                .orElseThrow(() -> new CourseNotFoundException());
//    }
}
