package com.medicare.webui.client;

import com.medicare.common.dto.PageResponse;
import com.medicare.webui.dto.MedicationForm;
import com.medicare.webui.dto.MedicationView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Calls the medication catalog endpoints of medical-records-service. */
@Component
public class MedicationApiClient {

    private final RestClient client;

    public MedicationApiClient(@Qualifier("recordsRestClient") RestClient client) {
        this.client = client;
    }

    public PageResponse<MedicationView> list(int page, int size, String sort) {
        return client.get()
                .uri(b -> b.path("/api/medications")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<MedicationView>>() {});
    }

    public List<MedicationView> listAll() {
        return client.get().uri("/api/medications/all")
                .retrieve()
                .body(new ParameterizedTypeReference<List<MedicationView>>() {});
    }

    public MedicationView get(Long id) {
        return client.get().uri("/api/medications/{id}", id)
                .retrieve().body(MedicationView.class);
    }

    public MedicationView create(MedicationForm form) {
        return client.post().uri("/api/medications")
                .body(form).retrieve().body(MedicationView.class);
    }

    public MedicationView update(Long id, MedicationForm form) {
        return client.put().uri("/api/medications/{id}", id)
                .body(form).retrieve().body(MedicationView.class);
    }

    public void delete(Long id) {
        client.delete().uri("/api/medications/{id}", id).retrieve().toBodilessEntity();
    }
}
