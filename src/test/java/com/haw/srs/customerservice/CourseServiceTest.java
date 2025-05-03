package com.haw.srs.customerservice;

import com.haw.srs.customerservice.Exception.CourseNotFoundException;
import com.haw.srs.customerservice.Exception.CustomerNotFoundException;
import com.haw.srs.customerservice.Exception.MembershipMailNotSent;
import com.haw.srs.customerservice.Repo.CourseRepository;
import com.haw.srs.customerservice.Repo.CustomerRepository;
import com.haw.srs.customerservice.Service.CourseService;
import com.haw.srs.customerservice.Service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CourseServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CourseRepository courseRepository;

    @MockBean
    private MailGateway mailGateway;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
    }

    @Test
    void enrollCustomerInCourseSuccess() throws CustomerNotFoundException {
        Customer customer = new Customer("Jane", "Doe", Gender.FEMALE, "jane.doe@mail.com", null);
        customerRepository.save(customer);

        assertThat(customer.getCourses()).size().isEqualTo(0);
        Course course = new Course("Software Engineering 1");
        courseService.enrollInCourse(customer.getLastName(), course);
        assertEquals(1,
                course.getAnzahlTeilnehmer());
        assertThat(customerService.findCustomerByLastname(customer.getLastName()).getCourses())
                .size().isEqualTo(1);


    }

    @Test
    void enrollCustomerInCourseFailBecauseOfCustomerNotFound() {
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> courseService.enrollInCourse("notExisting", new Course("Software Engineering 1")))
                .withMessageContaining("Could not find customer with lastname notExisting.");
    }

    @Test
    void transferCoursesSuccess() throws CustomerNotFoundException {
        Customer from = new Customer("John", "Smith", Gender.MALE);
        Course course1 = new Course("Software Engineering 1");
        Course course2 = new Course("Software Engineering 2");

        courseService.enrollInCourse("Smith",course1);
        courseService.enrollInCourse("Smith",course2);


        Customer to = new Customer("Eva", "Miller", Gender.FEMALE);
        customerRepository.save(to);
        from = customerService.findCustomerByLastname(from.getLastName());
        assertThat(from.getCourses()).size().isEqualTo(2);
        assertThat(to.getCourses()).size().isEqualTo(0);

        courseService.transferCourses(from.getLastName(), to.getLastName());

        assertThat(customerService.findCustomerByLastname(from.getLastName()).getCourses())
                .size().isEqualTo(0);
        assertThat(customerService.findCustomerByLastname(to.getLastName()).getCourses())
                .size().isEqualTo(2);
    }

    @Test
    void cancelMembershipSuccess() throws CustomerNotFoundException, CourseNotFoundException, MembershipMailNotSent {

        Customer customer = new Customer("Max", "Mustermann", Gender.MALE,"max.must@mail.com", null);
        Course course =new Course("Informationssysteme 2");
        courseService.enrollInCourse(customer.getLastName(), course);

        when(mailGateway.sendMail(anyString(), anyString(), anyString())).thenReturn(true);

        courseService.cancelMembership(customer.getId(), course.getId());
        assertEquals(0, course.getAnzahlTeilnehmer());
    }

    @Test
    void cancelMembershipFailBecauseOfUnableToSendMail() throws CustomerNotFoundException {
        // set up customer and course here
        // ...
        Customer customer = new Customer("Max", "Mustermann", Gender.MALE,"max.must@mail.com", null);
        Course course =new Course("Informationssysteme 2");
        courseService.enrollInCourse(customer.getLastName(), course);
//        customer.addCourse(course);
//        customerRepository.save(customer);
//        courseRepository.save(course);
        // configure MailGateway-mock
        when(mailGateway.sendMail(anyString(), anyString(), anyString())).thenReturn(false);

        assertThatExceptionOfType(MembershipMailNotSent.class)
                .isThrownBy(() -> courseService.cancelMembership(customer.getId(), course.getId()))
                .withMessageContaining("Could not send membership mail to");
        assertEquals(0, course.getAnzahlTeilnehmer());

    }
    
    @Test
    void cancelMembershipSuccessBDDStyle() throws CustomerNotFoundException, CourseNotFoundException, MembershipMailNotSent {
        // set up customer and course here
        // ...
        Customer customer = new Customer("Max", "Mustermann", Gender.MALE,"max.must@mail.com", null);
        Course course =new Course("Informationssysteme 2");
        courseService.enrollInCourse(customer.getLastName(), course);
//        customer.addCourse(course);
//        customerRepository.save(customer);
//        courseRepository.save(course);
        // configure MailGateway-mock with BDD-style
        given(mailGateway.sendMail(anyString(), anyString(), anyString())).willReturn(true);

        courseService.cancelMembership(customer.getId(), course.getId());
        assertEquals(0, course.getAnzahlTeilnehmer());
    }

    @Test
    @Transactional
    void enrollCustomerInCourseSuccess_hibernateCacheTest() throws CustomerNotFoundException {
        Customer customer = new Customer("Jane", "Doe", Gender.FEMALE, "jane.doe@mail.com", null);
        customerRepository.save(customer);

        assertThat(customer.getCourses()).size().isEqualTo(0);

        courseService.enrollInCourse(customer.getLastName(), new Course("Software Engineering 1"));

        // works anyway because updated customer object is read from database
        //assertThat(customerService.findCustomerByLastname(customer.getLastName()).getCourses())
        // .size().isEqualTo(1);

        // the following assert fails because of separate transaction (incl. separate persistent object cache) in method "enrollInCourse"
        // put @Transactional before this method to fix this -> only a single transaction and therefore cache is used in this method
        assertThat(customer.getCourses()).size().isEqualTo(1);

    }

//    @Test
//    void testAnzahlTeilnehmer() throws Exception {
//        // Arrange
//        Customer customer = new Customer("Max", "Müller", Gender.MALE,"max.müll@mail.com", null);
//        customerRepository.save(customer);
//
//        Course course = new Course("Datenbanken");
//
//
//        // Act
//        courseService.enrollInCourse(customer.getLastName(), course);
//
//        // Assert
//
//        assertEquals(1,
//                        course.getAnzahlTeilnehmer());
//
//        Customer customer2 = new Customer("Max", "Mahmud", Gender.MALE,"max.mahm@mail.com", null);
//        customerRepository.save(customer2);
//
//        courseService.transferCourses(customer.getLastName(),customer2.getLastName());
//        //courseRepository.save(course);
//        assertEquals(customerService.findCustomerByLastname(customer2.getLastName()).getCourses().size(),
//                course.getAnzahlTeilnehmer());
//        //assertEquals(0, customer.getCourses().size());
//        //assertEquals(1, customer2.getCourses().size())
//        customer2=customerService.findCustomerByLastname(customer2.getLastName());
//        when(mailGateway.sendMail(anyString(), anyString(), anyString())).thenReturn(true);
//
//        courseService.cancelMembership(customer2.getId(), course.getId());
//        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
//        assertEquals(0, updatedCourse.getAnzahlTeilnehmer());
//    }


}
