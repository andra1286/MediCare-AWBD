package com.medicare.webui.web;

import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.dto.UserForm;
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

/** Admin UI for identity users. */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserWebController {

    private final IdentityApiClient identityClient;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "username,asc") String sort,
                       Model model) {
        model.addAttribute("page", identityClient.listUsers(page, size, sort));
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            UserForm form = new UserForm();
            form.getRoles().add("ROLE_PATIENT");
            model.addAttribute("userForm", form);
        }
        model.addAttribute("editId", null);
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("userForm")) {
            UserForm form = new UserForm();
            var user = identityClient.getUser(id);
            form.setUsername(user.getUsername());
            form.setEmail(user.getEmail());
            form.setEnabled(user.isEnabled());
            form.setRoles(user.getRoles() != null ? user.getRoles() : java.util.List.of());
            model.addAttribute("userForm", form);
        }
        model.addAttribute("editId", id);
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            result.rejectValue("password", "required", "Password is required");
        }
        if (result.hasErrors()) {
            model.addAttribute("editId", null);
            return "users/form";
        }
        try {
            identityClient.createUser(form);
            ra.addFlashAttribute("success", "User created.");
            return "redirect:/users";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            ra.addFlashAttribute("userForm", form);
            return "redirect:/users/new";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            return "users/form";
        }
        try {
            identityClient.updateUser(id, form);
            ra.addFlashAttribute("success", "User updated.");
            return "redirect:/users";
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
            return "redirect:/users/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            identityClient.deleteUser(id);
            ra.addFlashAttribute("success", "User deleted.");
        } catch (RestClientResponseException ex) {
            ra.addFlashAttribute("error", ApiErrors.message(ex));
        }
        return "redirect:/users";
    }
}
