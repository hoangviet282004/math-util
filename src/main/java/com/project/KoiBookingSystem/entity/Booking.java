package com.project.KoiBookingSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.KoiBookingSystem.enums.BookingStatus;
import com.project.KoiBookingSystem.enums.BookingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Booking ID cannot be null")
    private String bookingID;

    @NotNull(message = "Create date cannot be null")
    @PastOrPresent(message = "Create date cannot be in the future")
    private LocalDate createDate;

    @Min(value = 1, message = "Number of persons must be at least 1")
    private int numberOfPerson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Booking status cannot be null")
    private BookingStatus bookingStatus;

    @Positive(message = "Total price must be positive")
    private float totalPrice;

    @NotNull(message = "seatBooked can not be blank")
    private int seatBooked;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    private Date lastUpdate;
    private  boolean refundable = false;

    @ManyToOne
    @JoinColumn(name = "customer_Id" , nullable = false, referencedColumnName = "userID")
    private Account customer;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false, referencedColumnName = "tourID")
    private Tour tourId;

    @OneToOne(mappedBy = "booking")
    @JsonIgnore // Ngăn vòng lặp
    private Payment payment;


    @ManyToOne
    @JoinColumn(name = "consulting_Id", referencedColumnName = "userID")
    private Account consulting;

}
