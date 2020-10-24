package de.moneymanager.controller;

import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.banksystem.BankSystemService;
import de.moneymanager.banksystem.InboxEntry;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.*;

@Controller
public class InboxController extends AbstractController {

    BankSystemService bankSystemService;

    public InboxController(BankSystemService bankSystemService) {
        this.bankSystemService = bankSystemService;
    }

    @GetMapping("/inbox")
    public String getInbox(Model model) {
        List<InboxEntry> inbox = this.bankSystemService.getInbox(getUserAccountFromModel(model).getBankAccount());
        Collections.sort(inbox, (o2, o1) -> o1.getTimeStamp().compareTo(o2.getTimeStamp()));
        model.addAttribute("formatter", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        model.addAttribute("inbox", inbox);
        return "/user/inbox";
    }

    @GetMapping("/download/{name}")
    public String inboxFile(@PathVariable("name") String pdfName, Model model) {
        FileSystemResource file = new FileSystemResource("src/main/resources/Pdfs/" + pdfName);
        if(file.exists()) {
            return "redirect:/inbox/" + pdfName;
        } else {
            model.addAttribute("message", "Sorry, file: "+ "src/main/resources/Pdfs/" + pdfName + " does not exist.");
            return "error";
        }
    }

    @GetMapping("/inbox/{name}")
    @ResponseBody
    public FileSystemResource getInboxFile(@PathVariable("name") String pdfName, Model model) {
        return new FileSystemResource("src/main/resources/Pdfs/" + pdfName + ".pdf");
    }



}
