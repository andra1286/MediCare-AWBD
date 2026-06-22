package com.medicare.webui.client;

import com.medicare.common.dto.DoctorDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.common.dto.PatientDto;
import com.medicare.common.dto.UserDto;
import com.medicare.webui.dto.DoctorProfileForm;
import com.medicare.webui.dto.LoginResponse;
import com.medicare.webui.dto.PatientProfileForm;
import com.medicare.webui.dto.UserForm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Calls identity-service for auth and user/doctor/patient management. */
@Component
public class IdentityApiClient {

    private final RestClient authClient;
    private final RestClient apiClient;

    public IdentityApiClient(
            @Qualifier("identityAuthRestClient") RestClient authClient,
            @Qualifier("identityRestClient") RestClient apiClient) {
        this.authClient = authClient;
        this.apiClient = apiClient;
    }

    public LoginResponse login(String username, String password) {
        return authClient.post()
                .uri("/auth/login")
                .body(Map.of("username", username, "password", password))
                .retrieve()
                .body(LoginResponse.class);
    }

    public PageResponse<UserDto> listUsers(int page, int size, String sort) {
        return apiClient.get()
                .uri(b -> b.path("/api/users")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<UserDto>>() {});
    }

    public UserDto getUser(Long id) {
        return apiClient.get().uri("/api/users/{id}", id).retrieve().body(UserDto.class);
    }

    public UserDto createUser(UserForm form) {
        return apiClient.post()
                .uri("/api/users")
                .body(Map.of(
                        "username", form.getUsername(),
                        "email", form.getEmail(),
                        "fullName", form.getFullName(),
                        "password", form.getPassword(),
                        "roles", form.getRoles()))
                .retrieve()
                .body(UserDto.class);
    }

    public UserDto updateUser(Long id, UserForm form) {
        var body = new java.util.HashMap<String, Object>();
        body.put("email", form.getEmail());
        body.put("fullName", form.getFullName());
        body.put("enabled", form.isEnabled());
        body.put("roles", form.getRoles());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            body.put("password", form.getPassword());
        }
        return apiClient.put()
                .uri("/api/users/{id}", id)
                .body(body)
                .retrieve()
                .body(UserDto.class);
    }

    public void deleteUser(Long id) {
        apiClient.delete().uri("/api/users/{id}", id).retrieve().toBodilessEntity();
    }

    public PageResponse<DoctorDto> listDoctors(int page, int size, String sort) {
        return apiClient.get()
                .uri(b -> b.path("/api/doctors")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<DoctorDto>>() {});
    }

    public DoctorDto createDoctor(DoctorProfileForm form) {
        return apiClient.post()
                .uri("/api/doctors")
                .body(Map.of(
                        "userId", form.getUserId(),
                        "specialty", form.getSpecialty(),
                        "licenseNo", form.getLicenseNo()))
                .retrieve()
                .body(DoctorDto.class);
    }

    public void deleteDoctor(Long id) {
        apiClient.delete().uri("/api/doctors/{id}", id).retrieve().toBodilessEntity();
    }

    public PageResponse<PatientDto> listPatients(int page, int size, String sort) {
        return apiClient.get()
                .uri(b -> b.path("/api/patients")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<PatientDto>>() {});
    }

    public PatientDto getMyPatientProfile() {
        return apiClient.get().uri("/api/patients/me").retrieve().body(PatientDto.class);
    }

    public PatientDto createPatient(PatientProfileForm form) {
        return apiClient.post()
                .uri("/api/patients")
                .body(Map.of(
                        "userId", form.getUserId(),
                        "personalId", form.getPersonalId(),
                        "phone", form.getPhone() != null ? form.getPhone() : ""))
                .retrieve()
                .body(PatientDto.class);
    }

    public void deletePatient(Long id) {
        apiClient.delete().uri("/api/patients/{id}", id).retrieve().toBodilessEntity();
    }

    /** Loads up to 200 doctors for appointment dropdowns. */
    public List<DoctorDto> listDoctorsForSelect() {
        return listDoctors(0, 200, "specialty,asc").getContent();
    }

    /** Loads up to 200 patients for appointment dropdowns. */
    public List<PatientDto> listPatientsForSelect() {
        return listPatients(0, 200, "personalId,asc").getContent();
    }
}
