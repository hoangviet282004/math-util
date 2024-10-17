package com.project.KoiBookingSystem.model.response;

import lombok.Data;

@Data
public class TicketResponse {
    private BookingResponse bookingResponse;
    private String subject;
    private String link;
}
