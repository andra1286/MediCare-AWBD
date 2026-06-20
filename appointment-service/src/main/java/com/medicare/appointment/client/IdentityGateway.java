package com.medicare.appointment.client;

import com.medicare.common.client.IdentityClient;
import com.medicare.common.dto.DoctorDto;
import com.medicare.common.exception.BusinessRuleException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption wrapper around {@link IdentityClient}, hardened with Resilience4j.
 * <p>
 * Fallback policy (rule BR-10): a genuine validation failure (the identity service is up
 * but the doctor/patient does not exist -> a BusinessRuleException) is propagated and the
 * booking is rejected. A technical outage (identity service unreachable / timing out)
 * triggers fail-open degraded mode: the booking proceeds with a logged warning.
 */
@Component
@RequiredArgsConstructor
public class IdentityGateway {

    private static final Logger log = LoggerFactory.getLogger(IdentityGateway.class);

    private final IdentityClient identityClient;

    /** Verify the doctor and patient exist before booking. */
    @CircuitBreaker(name = "identity", fallbackMethod = "validateFallback")
    @Retry(name = "identity")
    public void validateForBooking(Long doctorId, Long patientId) {
        try {
            identityClient.getDoctor(doctorId);
            identityClient.getPatient(patientId);
        } catch (FeignException.NotFound nf) {
            throw new BusinessRuleException("Doctor or patient does not exist");
        }
    }

    @SuppressWarnings("unused")
    void validateFallback(Long doctorId, Long patientId, Throwable t) {
        if (t instanceof BusinessRuleException bre) {
            throw bre; // real validation failure -> reject the booking
        }
        log.warn("identity-service unavailable; booking proceeds in degraded mode "
                + "(doctorId={}, patientId={}): {}", doctorId, patientId, t.toString());
    }

    /** Cached doctor lookup (frequently read, rarely changed). */
    @Cacheable(value = "doctors", key = "#id")
    @CircuitBreaker(name = "identity", fallbackMethod = "getDoctorFallback")
    public DoctorDto getDoctor(Long id) {
        return identityClient.getDoctor(id);
    }

    @SuppressWarnings("unused")
    DoctorDto getDoctorFallback(Long id, Throwable t) {
        log.warn("identity-service unavailable; returning null for doctor {}: {}", id, t.toString());
        return null;
    }
}
