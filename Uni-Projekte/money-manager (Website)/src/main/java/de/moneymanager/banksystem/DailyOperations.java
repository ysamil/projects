package de.moneymanager.banksystem;

public class DailyOperations implements Runnable {

    private final BankSystemService bankSystemService;

    public DailyOperations(BankSystemService bankSystemService) {
        this.bankSystemService = bankSystemService;
    }

    @Override
    public void run() {
        this.bankSystemService.dailyOperations();
    }

}
