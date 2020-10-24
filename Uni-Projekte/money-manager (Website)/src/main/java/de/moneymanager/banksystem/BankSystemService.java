package de.moneymanager.banksystem;

import de.moneymanager.accounts.AccountServiceImpl;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing games.
 */
@Service
public class BankSystemService {
    //TODO: Get EvaluationName

    private final Logger logger = LoggerFactory.getLogger(BankSystemService.class);

    private static final String FEE_USAGE             = "daily fee";
    private static final String CREDIT_INTEREST_USAGE = "credit interest";
    private static final String DEBIT_INTEREST_USAGE  = "debit interest";

    private final GameRepository gameRepository;

    private final PdfCreator pdfCreator;

    private final AccountServiceImpl accountService;

    private final TransactionService transactionService;

    private final InboxEntryRepository inboxEntryRepository;

    private final ThreadPoolTaskScheduler taskScheduler;

    public BankSystemService(GameRepository gameRepository, PdfCreator pdfCreator, AccountServiceImpl accountService,
                             TransactionService transactionService, InboxEntryRepository inboxEntryRepository,
                             ThreadPoolTaskScheduler taskScheduler) {
        this.gameRepository = gameRepository;
        this.pdfCreator = pdfCreator;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.inboxEntryRepository = inboxEntryRepository;
        this.taskScheduler = taskScheduler;

        startScheduler();

    }

    public void startScheduler() {
        Game game = getGame();
        if (game.getRunning()) {
            LocalDateTime firstInterval = game.getFirstInterval();
            long          timeInterval  = game.getTimeInterval();

            setTimeInterval(timeInterval, firstInterval);
        }
    }

    /**
     * Sets the time interval for dailyOperations
     *
     * @param intervalLength the length of the interval in milliseconds
     * @param intervalStart  the fist time to start
     */
    public void setTimeInterval(long intervalLength, LocalDateTime intervalStart) {

        // Make sure that the first trigger is in the future.
        while (intervalStart.isBefore(LocalDateTime.now())) {
            intervalStart = intervalStart.plusSeconds(intervalLength / 1000);
        }

        Game game = getGame();
        game.setTimeInterval(intervalLength);
        game.setFirstInterval(intervalStart);
        this.gameRepository.save(game);

        Date start = Date.from(intervalStart.atZone(ZoneId.systemDefault()).toInstant());

        this.logger.info(
                "The daily operations get scheduled for: " + start + " and repeats every " + intervalLength + "ms");

        this.taskScheduler.shutdown();
        this.taskScheduler.initialize();
        this.taskScheduler.scheduleAtFixedRate(new DailyOperations(this), start, intervalLength);

        this.logger.info("Set up daily operations!");

    }

    /**
     * Executes daily operations, like booking interest and sending bills/bank statements.
     */
    public void dailyOperations() {
        this.logger.info("Executing daily operations");

        //book interest
        bookInterest();

        //book fees
        bookFees();

        //create bank statements
        List<BankAccount> bankAccounts = this.accountService.getBankAccounts();
        for (BankAccount b : bankAccounts) {
            this.pdfCreator.createBankStatement(b);
        }
    }

    /***
     * Returns the inbox for a specific bank account.
     */
    public List<InboxEntry> getInbox(BankAccount bankAccount) {

        Iterable<InboxEntry> inboxIterable = this.inboxEntryRepository.findAllByBankAccount(bankAccount);

        // Create an empty list
        List<InboxEntry> inboxList = new ArrayList<>();

        // Add each element of iterator to the List
        inboxIterable.forEach(inboxList::add);

        return inboxList;
    }

    /**
     * Sets the games state to running.
     */
    public void startGame() {
        Game game = getGame();

        if (!game.getRunning()) {

            game.setRunning(true);
            this.gameRepository.save(game);
            startScheduler();

        }
    }

    /**
     * Ends the game, creates evaluation and deletes all data.
     */
    public void endGame() {
        Game game = getGame();
        game.setRunning(false);

        //delete everything
        this.transactionService.reset();
        this.inboxEntryRepository.deleteAll();
        this.accountService.reset();
        this.gameRepository.deleteAll();
    }

    /**
     * Gets the Game from the database and secures that there is exactly one.
     */
    private Game getGame() {

        Optional<Game> game = this.gameRepository.findById(1L);
        // Create a new Game if there is none in the Database.
        return game.isEmpty() ? this.gameRepository.save(new Game()) : game.get();

    }

    /**
     * Books fees for all bank accounts.
     */
    private void bookFees() {
        Game game = getGame();

        //get fee
        Long fee = game.getAccountFees();

        if (fee <= 0) {
            return;
        }

        //get all bank accounts.
        List<BankAccount> bankAccounts = this.accountService.getBankAccounts();

        //get "Bank" bank account
        BankAccount unlimitedBankAccount = this.accountService.getUnlimitedBankAccount();

        for (BankAccount bankAccount : bankAccounts) {

            if (bankAccount.equals(unlimitedBankAccount)) { continue; }

            //book interest
            this.transactionService.addTransaction(FEE_USAGE, bankAccount, unlimitedBankAccount, fee, null);

            //create bill
            this.pdfCreator.createBill(bankAccount, fee);
        }

    }

    /**
     * Books interest for all bank accounts.
     */
    private void bookInterest() {
        Game game = getGame();
        //get Interests
        double debitInterest  = game.getDebitInterest();
        double creditInterest = game.getCreditInterest();

        //get all bank accounts.
        List<BankAccount> bankAccounts = this.accountService.getBankAccounts();

        //get "Bank" bank account
        BankAccount unlimitedBankAccount = this.accountService.getUnlimitedBankAccount();

        //calculate and book interest
        for (BankAccount bankAccount : bankAccounts) {

            if (bankAccount.equals(unlimitedBankAccount)) { continue; }

            long   balance = bankAccount.getBalance();
            long   interest;
            String usage;

            //calculate interest
            if (balance > 0) {
                interest = Math.round(Math.ceil(balance * (creditInterest / 100)));
                usage = CREDIT_INTEREST_USAGE;
                //book interest
                if (interest > 0) {
                    this.transactionService.addTransaction(usage, unlimitedBankAccount, bankAccount, interest, null);
                }
            } else {
                interest = Math.round(Math.ceil(balance * (debitInterest / 100)));

                interest = -interest;
                usage = DEBIT_INTEREST_USAGE;
                //book interest

                if (interest > 0) {
                    this.transactionService.addTransaction(usage, bankAccount, unlimitedBankAccount, interest, null);
                }
            }
        }

    }

    //Getter and Setter for all attributes that are customizable by the user.

    public Boolean isRunning() {
        Game game = getGame();
        return game.getRunning();
    }

    public String getBankName() {
        Game game = getGame();
        return game.getBankName();
    }

    public void setBankName(String bankName) {
        Game game = getGame();
        game.setBankName(bankName);
        this.gameRepository.save(game);
    }

    public double getDebitInterest() {
        Game game = getGame();
        return game.getDebitInterest();
    }

    public void setDebitInterest(Double debitInterest) {
        Game game = getGame();
        game.setDebitInterest(debitInterest);
        this.gameRepository.save(game);
    }

    public double getCreditInterest() {
        Game game = getGame();
        return game.getCreditInterest();
    }

    public void setCreditInterest(Double creditInterest) {
        Game game = getGame();
        game.setCreditInterest(creditInterest);
        this.gameRepository.save(game);
    }

    public boolean getBankStatement() {
        Game game = getGame();
        return game.getBankStatement();
    }

    public void setBankStatement(Boolean bankStatement) {
        Game game = getGame();
        game.setBankStatement(bankStatement);
        this.gameRepository.save(game);
    }

    public boolean getBill() {
        Game game = getGame();
        return game.getBill();
    }

    public void setBill(Boolean bill) {
        Game game = getGame();
        game.setBill(bill);
        this.gameRepository.save(game);
    }

    public Long getStartBalance() {
        Game game = getGame();
        return game.getStartBalance();
    }

    public void setStartBalance(Long startBalance) {
        Game game = getGame();
        game.setStartBalance(startBalance);
        this.gameRepository.save(game);
    }

    public void setDispo(Long dispo) {
        Game game = getGame();
        game.setDispo(dispo);
        this.gameRepository.save(game);
    }

    public Long getDispo() {
        Game game = getGame();
        return game.getDispo();
    }

    public void setAccountFees(Long fees) {
        Game game = getGame();
        game.setAccountFees(fees);
        this.gameRepository.save(game);
    }

    public Long getAccountFees() {
        Game game = getGame();
        return game.getAccountFees();
    }

    public Long getTimeInterval() {
        Game game = getGame();
        return game.getTimeInterval();
    }

    public LocalDateTime getFirstInterval() {
        Game game = getGame();
        return game.getFirstInterval();
    }

    public String getFormattedFirstInterval() {
        LocalDateTime firstInterval = getFirstInterval();
        int           hour          = firstInterval.getHour();
        int           minute        = firstInterval.getMinute();
        return String.format("%02d:%02d", hour, minute);
    }

    public String getBlz() {
        Game   game = getGame();
        String blz  = game.getBlz();
        if (blz == null) {
            StringBuilder builder = new StringBuilder();
            SecureRandom  random  = new SecureRandom();
            builder.append("2002");
            for (int i = 0; i < 4; i++) {
                builder.append(random.nextInt(10));
            }
            blz = builder.toString();
            game.setBlz(blz);
            this.gameRepository.save(game);
        }
        return blz;

    }

}
