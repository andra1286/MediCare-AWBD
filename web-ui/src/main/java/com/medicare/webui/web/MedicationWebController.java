package com.medicare.webui.web;

import com.medicare.webui.client.MedicationApiClient;
import com.medicare.webui.dto.MedicationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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

/** UI for the medication catalog: list, create, edit, delete. */
@Controller
@RequestMapping("/medications")
@RequiredArgsConstructor
public class MedicationWebController {

    private final MedicationApiClient client;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name,asc") String sort,
                       Model model) {
        model.addAttribute("page", client.list(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "medications/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("medicationForm")) {
            model.addAttribute("medicationForm", new MedicationForm());
        }
        model.addAttribute("editId", null);
        return "medications/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("medicationForm")) {
            MedicationForm form = new MedicationForm();
            BeanUtils.copyProperties(client.get(id), form);
            model.addAttribute("medicationForm", form);
        }
        model.addAttribute("editId", id);
        return "medications/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("medicationForm") MedicationForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", null);
            return "medications/form";
        }
        try {
            client.create(form);
            ra.addFlashAttribute("success", "Medication created.");
            return "redirect:/medications";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("medicationForm", form);
            return "redirect:/medications/new";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("medicationForm") MedicationForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            return "medications/form";
        }
        try {
            client.update(id, form);
            ra.addFlashAttribute("success", "Medication updated.");
            return "redirect:/medications";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            return "redirect:/medications/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            client.delete(id);
            ra.addFlashAttribute("success", "Medication deleted.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/medications";
    }
}
