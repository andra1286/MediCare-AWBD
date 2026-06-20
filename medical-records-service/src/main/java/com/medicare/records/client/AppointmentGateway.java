package com.medicare.records.client;

import com.medicare.common.client.AppointmentClient;
import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.exception.BusinessRuleException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resilient wrapper around {@link AppointmentClient}.
 * <p>
 * Rule BR-13: a medical record may only be created for a COMPLETED appointment.
 * If appointment-service is up and the appointment is not completed (or absent),
 * the record creation is rejected. If appointment-service is unreachable, the
 * fallback fails open (degraded mode) and lets creation proceed with a warning.
 */
@Component
@RequiredArgsConstructor
public class AppointmentGateway {

    private static final Logger log = LoggerFactory.getLogger(AppointmentGateway.class);

    private final AppointmentClient appointmentClient;

    @CircuitBreaker(name = "appointment", fallbackMethod = "verifyFallback")
    @Retry(name = "appointment")
    public void verifyCompleted(Long appointmentId) {
        AppointmentDto appointment;
        try {
            appointment = appointmentClient.getAppointment(appointmentId);
        } catch (FeignException.NotFound nf) {
            throw new BusinessRuleException("Appointment " + appointmentId + " does not exist");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new BusinessRuleException(
                    "A medical record can only be created for a COMPLETED appointment");
        }
    }

    @SuppressWarnings("unused")
    void verifyFallback(Long appointmentId, Throwable t) {
        if (t instanceof BusinessRuleException bre) {
            throw bre; // real rule violation -> reject
        }
        log.warn("appointment-service unavailable; record creation proceeds in degraded mode "
                + "(appointmentId={}): {}", appointmentId, t.toString());
    }
}
