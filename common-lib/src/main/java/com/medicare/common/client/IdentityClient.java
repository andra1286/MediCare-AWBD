package com.medicare.common.client;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PatientDto;
import com.medicare.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * HTTP client to identity-service. Spring Cloud generates the implementation.
 * <p>
 * {@code url} is taken from the property {@code medicare.identity.url}; when blank,
 * the call is load-balanced by the service name {@code identity-service} via Eureka.
 * identity-service (Dev A) implements these endpoints under {@code /internal}.
 */
@FeignClient(name = "identity-service", url = "${medicare.identity.url:}", path = "/internal")
public interface IdentityClient {

    @GetMapping("/doctors/{id}")
    DoctorDto getDoctor(@PathVariable("id") Long id);

    @GetMapping("/patients/{id}")
    PatientDto getPatient(@PathVariable("id") Long id);

    @GetMapping("/users/{id}")
    UserDto getUser(@PathVariable("id") Long id);
}
