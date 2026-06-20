package com.medicare.common.client;

import com.medicare.common.dto.AppointmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * HTTP client to appointment-service, used by medical-records-service to confirm an
 * appointment is COMPLETED before a record is created (rule BR-13).
 * <p>
 * {@code url} comes from {@code medicare.appointment.url}; when blank the call is
 * load-balanced by the service name {@code appointment-service} via Eureka.
 */
@FeignClient(name = "appointment-service", url = "${medicare.appointment.url:}")
public interface AppointmentClient {

    @GetMapping("/api/appointments/{id}")
    AppointmentDto getAppointment(@PathVariable("id") Long id);
}
