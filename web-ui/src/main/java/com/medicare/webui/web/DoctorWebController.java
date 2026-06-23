package com.medicare.webui.web;

import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.dto.DoctorProfileForm;
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

/** Admin UI for doctor profiles. */
@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorWebController {

    private final IdentityApiClient identityClient;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "specialty,asc") String sort,
                       Model model) {
        model.addAttribute("page", identityClient.listDoctors(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "doctors/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("doctorForm")) {
            model.addAttribute("doctorForm", new DoctorProfileForm());
        }
        return "doctors/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("doctorForm") DoctorProfileForm form,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "doctors/form";
        }
        try {
            identityClient.createDoctor(form);
            ra.addFlashAttribute("success", "Doctor profile created.");
            return "redirect:/doctors";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("doctorForm", form);
            return "redirect:/doctors/new";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            identityClient.deleteDoctor(id);
            ra.addFlashAttribute("success", "Doctor profile deleted.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/doctors";
    }
}
