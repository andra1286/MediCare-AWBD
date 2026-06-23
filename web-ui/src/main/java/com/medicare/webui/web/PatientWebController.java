package com.medicare.webui.web;

import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.dto.PatientProfileForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/** UI for patient profiles (list for admin/doctor, create for admin). */
@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientWebController {

    private final IdentityApiClient identityClient;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "personalId,asc") String sort,
                       Model model) {
        model.addAttribute("page", identityClient.listPatients(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "patients/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("patientForm")) {
            model.addAttribute("patientForm", new PatientProfileForm());
        }
        return "patients/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("patientForm") PatientProfileForm form,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "patients/form";
        }
        try {
            identityClient.createPatient(form);
            ra.addFlashAttribute("success", "Patient profile created.");
            return "redirect:/patients";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("patientForm", form);
            return "redirect:/patients/new";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            identityClient.deletePatient(id);
            ra.addFlashAttribute("success", "Patient profile deleted.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/patients";
    }
}
