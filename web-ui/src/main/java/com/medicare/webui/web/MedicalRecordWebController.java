package com.medicare.webui.web;

import com.medicare.webui.client.MedicalRecordApiClient;
import com.medicare.webui.client.MedicationApiClient;
import com.medicare.webui.dto.MedicalRecordForm;
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

/** UI for medical records: list, create (with a prescription), view. */
@Controller
@RequestMapping("/records")
@RequiredArgsConstructor
public class MedicalRecordWebController {

    private final MedicalRecordApiClient recordClient;
    private final MedicationApiClient medicationClient;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "createdAt,desc") String sort,
                       Model model) {
        model.addAttribute("page", recordClient.list(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "records/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("medicalRecordForm")) {
            model.addAttribute("medicalRecordForm", new MedicalRecordForm());
        }
        model.addAttribute("medications", medicationClient.listAll());
        return "records/form";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("record", recordClient.get(id));
        return "records/view";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("medicalRecordForm") MedicalRecordForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("medications", medicationClient.listAll());
            return "records/form";
        }
        try {
            recordClient.create(form);
            ra.addFlashAttribute("success", "Medical record created.");
            return "redirect:/records";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("medicalRecordForm", form);
            return "redirect:/records/new";
        }
    }
}
