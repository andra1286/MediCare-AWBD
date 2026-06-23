package com.medicare.webui.web;

import com.medicare.common.dto.PatientDto;
import com.medicare.webui.client.AppointmentApiClient;
import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.dto.AppointmentForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** UI for appointments: list, book, cancel, complete. */
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentWebController {

    private final AppointmentApiClient client;
    private final IdentityApiClient identityClient;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "scheduledAt,desc") String sort,
                       Model model) {
        model.addAttribute("page", client.list(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "appointments/list";
    }

    @GetMapping("/new")
    public String newForm(Model model, Authentication auth) {
        if (!model.containsAttribute("appointmentForm")) {
            model.addAttribute("appointmentForm", new AppointmentForm());
        }
        populateBookingOptions(model, auth);
        return "appointments/form";
    }

    @PostMapping
    public String book(@Valid @ModelAttribute("appointmentForm") AppointmentForm form,
                       BindingResult result, Model model, Authentication auth,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateBookingOptions(model, auth);
            return "appointments/form";
        }
        try {
            client.book(form);
            ra.addFlashAttribute("success", "Appointment booked successfully.");
            return "redirect:/appointments";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("appointmentForm", form);
            return "redirect:/appointments/new";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            client.cancel(id);
            ra.addFlashAttribute("success", "Appointment cancelled.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            client.complete(id);
            ra.addFlashAttribute("success", "Appointment marked as completed.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/appointments";
    }

    private void populateBookingOptions(Model model, Authentication auth) {
        model.addAttribute("doctors", identityClient.listDoctorsForSelect());
        if (hasAnyRole(auth, "ADMIN", "DOCTOR")) {
            model.addAttribute("patients", identityClient.listPatientsForSelect());
            model.addAttribute("patientLocked", false);
        } else if (hasAnyRole(auth, "PATIENT")) {
            PatientDto self = identityClient.getMyPatientProfile();
            model.addAttribute("patients", List.of(self));
            model.addAttribute("patientLocked", true);
            AppointmentForm form = (AppointmentForm) model.getAttribute("appointmentForm");
            if (form != null && form.getPatientId() == null) {
                form.setPatientId(self.getId());
            }
        } else {
            model.addAttribute("patients", List.of());
            model.addAttribute("patientLocked", false);
        }
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null) {
            return false;
        }
        var wanted = java.util.Set.of(roles);
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> wanted.contains(a) || wanted.contains(a.replace("ROLE_", "")));
    }
}
