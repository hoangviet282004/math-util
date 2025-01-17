package com.project.KoiBookingSystem.model.response;


import com.project.KoiBookingSystem.enums.BookingStatus;
import com.project.KoiBookingSystem.enums.BookingType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponse {
    @NotBlank(message = "Booking ID cannot be null")
    private String bookingId;



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

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    private UserResponse customer;


    private TourResponse tourID;
}
