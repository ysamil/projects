package de.moneymanager.controller;

import de.moneymanager.banksystem.BankSystemService;
import de.moneymanager.controller.tranferobjects.GameSettingsTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@Secured("ROLE_ADMIN")
public class GameController {

    private final BankSystemService bankSystemService;

    public GameController(BankSystemService bankSystemService) {
        this.bankSystemService = bankSystemService;
    }

    @GetMapping("/game")
    public String gameSettingsPage(Model model, @RequestParam(required = false) boolean start,
                                   @RequestParam(required = false) boolean end) {
        if (start) {
            this.bankSystemService.startGame();
            return "redirect:/game";
        }
        if (end) {
            this.bankSystemService.endGame();
            return "redirect:/game";
        }

        if (this.bankSystemService.isRunning()) {
            model.addAttribute("bank", this.bankSystemService);
            return "admin/gameSettings";
        } else {
            return "admin/newGame";
        }

    }

    @PostMapping("/game")
    public String gameSettings(@Validated GameSettingsTO gameSettingsTO) {
        this.bankSystemService.setBankName(gameSettingsTO.getBankName());
        this.bankSystemService.setBankStatement(gameSettingsTO.isCreateStatements());
        this.bankSystemService.setBill(gameSettingsTO.isCreateBills());
        this.bankSystemService.setCreditInterest(gameSettingsTO.getCreditInterest());
        this.bankSystemService.setDebitInterest(gameSettingsTO.getDebitInterest());
        this.bankSystemService.setStartBalance((long) (gameSettingsTO.getStartBalance() * 100));
        this.bankSystemService.setDispo((long) (gameSettingsTO.getOverdraft() * 100));
        this.bankSystemService.setAccountFees((long) (gameSettingsTO.getAccountFees() * 100));

        String[] startTime = gameSettingsTO.getStartTime().split(":");

        LocalDateTime localDateTime =
                LocalDateTime.now()
                             .withHour(Integer.parseInt(startTime[0]))
                             .withMinute(Integer.parseInt(startTime[1]))
                             .withSecond(0);

        long hour = gameSettingsTO.getIntervalHour() * 60 * 60 * 1000L;
        long min  = gameSettingsTO.getIntervalMin() * 60 * 1000L;

        this.bankSystemService.setTimeInterval(hour + min, localDateTime);

        return "redirect:/game";
    }

    @GetMapping("/evaluation")
    public String evaluation(Model model) {
        model.addAttribute("message", "Sorry, not yet implemented.");
        return "error";
    }

    @RequestMapping(value = "/evaluationFile", method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource getEvaluation(Model model) {
        return new FileSystemResource("src/main/resources/Pdfs/Evaluation.pdf");
    }
}
