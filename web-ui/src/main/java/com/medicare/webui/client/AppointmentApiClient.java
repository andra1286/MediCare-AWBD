package com.medicare.webui.client;

import com.medicare.common.dto.AppointmentDto;
import com.medicare.common.dto.PageResponse;
import com.medicare.webui.dto.AppointmentForm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls appointment-service on behalf of the UI. */
@Component
public class AppointmentApiClient {

    private final RestClient client;

    public AppointmentApiClient(@Qualifier("appointmentRestClient") RestClient client) {
        this.client = client;
    }

    public PageResponse<AppointmentDto> list(int page, int size, String sort) {
        return client.get()
                .uri(b -> b.path("/api/appointments")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<AppointmentDto>>() {});
    }

    public AppointmentDto get(Long id) {
        return client.get().uri("/api/appointments/{id}", id)
                .retrieve().body(AppointmentDto.class);
    }

    public AppointmentDto book(AppointmentForm form) {
        return client.post().uri("/api/appointments")
                .body(form)
                .retrieve()
                .body(AppointmentDto.class);
    }

    public void cancel(Long id) {
        client.post().uri("/api/appointments/{id}/cancel", id).retrieve().toBodilessEntity();
    }

    public void complete(Long id) {
        client.post().uri("/api/appointments/{id}/complete", id).retrieve().toBodilessEntity();
    }
}
