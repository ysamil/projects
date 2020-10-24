package de.moneymanager.transaction;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.banksystem.BankSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
class TransactionMangr implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TransactionMangr.class);

    private final AccountService        accountService;
    private final TransactionRepository transactionRepository;
    private final BankSystemService     bankSystemService;

    private Thread thread;

    /** Waiting queue for transactions. */
    private final BlockingQueue<Transaction> queue = new LinkedBlockingQueue<>();

    public TransactionMangr(AccountService accountService, TransactionRepository transactionRepository,
                            @Lazy BankSystemService bankSystemService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.bankSystemService = bankSystemService;

        this.thread = new Thread(this);
        this.thread.start();
    }

    /**
     * Converts the specified data into a transaction and queues it to be executed.
     *
     * @param usage           usage of transaction
     * @param source          bank account from which the amount is to be debited
     * @param destinationIban bank accounts iban to which the amount should be credited
     * @param amount          amount to be transferred
     * @param user            user account that wants to perform the transaction
     */
    public void addTransaction(String usage, BankAccount source, String destinationIban, long amount,
                               UserAccount user) throws IllegalArgumentException {

        if (!TransactionUtilities.checkIban(destinationIban)) {
            throw new IllegalArgumentException();
        }

        Optional<BankAccount> optionalBankAccount = this.accountService.getBankAccountByIban(destinationIban);
        BankAccount           destination         = optionalBankAccount.orElse(null);

        addTransaction(usage, source, destination, amount, user);
    }

    /**
     * Converts the specified data into a transaction and queues it to be executed.
     *
     * @param usage       usage of transaction
     * @param source      bank account from which the amount is to be debited
     * @param destination bank accounts iban to which the amount should be credited
     * @param amount      amount to be transferred
     * @param user        user account that wants to perform the transaction
     */
    public void addTransaction(String usage, BankAccount source, BankAccount destination, long amount,
                               UserAccount user) throws IllegalArgumentException {

        BankAccount unlimitedBankAccount = this.accountService.getUnlimitedBankAccount();

        boolean transactionIsInvalid =
                usage.isEmpty() || source == null || destination == null || amount <= 0 ||
                (user == null && !source.equals(unlimitedBankAccount) && !destination.equals(unlimitedBankAccount)) ||
                source.equals(destination);

        if (transactionIsInvalid) {
            throw new IllegalArgumentException("Transaction data not valid");
        }

        Transaction transaction = new Transaction(usage, source, destination, amount, user);
        try {
            this.logger.info("Transaction added to queue: " + transaction);
            this.queue.put(transaction);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isValid(Transaction transaction) {

        long currentSourceBalance = transaction.getSource().getBalance();
        long allowedDisposition   = this.bankSystemService.getDispo();
        long transferAmount       = transaction.getAmount();

        BankAccount unlimitedBankAccount = this.accountService.getUnlimitedBankAccount();

        boolean hasUnlimitedMoney = unlimitedBankAccount.equals(transaction.getSource()) ||
                                    unlimitedBankAccount.equals(transaction.getDestination());

        return currentSourceBalance - transferAmount >= allowedDisposition || hasUnlimitedMoney;
    }

    private void transfer(Transaction transaction, boolean isReturnDebit) {

        this.logger.info("Transferring: " + transaction);

        transaction.setDate(LocalDateTime.now());

        BankAccount source        = this.accountService.getBankAccountById(transaction.getSource().getId())
                                                       .orElseThrow();
        long        sourceBalance = source.getBalance() - transaction.getAmount();

        BankAccount destination = this.accountService.getBankAccountById(transaction.getDestination().getId())
                                                     .orElseThrow();
        long destinationBalance = destination.getBalance() + transaction.getAmount();

        this.accountService.setBankAccountBalance(source, sourceBalance);
        this.accountService.setBankAccountBalance(destination, destinationBalance);

        if (!isValid(transaction) && !isReturnDebit) {
            this.logger.warn("Transaction invalid: " + transaction);
            Transaction backTransfer = new Transaction("return debit: " + transaction.getUsage(),
                                                       transaction.getDestination(), transaction.getSource(),
                                                       transaction.getAmount(), transaction.getUser()
            );
            transaction.setCanceled(true);
            this.transactionRepository.save(transaction);
            transfer(backTransfer, true);
        } else {
            this.logger.info("Transaction successful: " + transaction);
            this.transactionRepository.save(transaction);
        }

    }

    /** Stops transfer thread and clears transaction queue and repository. */
    public void reset() {
        this.thread.interrupt();
        this.queue.clear();
        this.transactionRepository.deleteAll();
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {

        while (true) {
            try {

                Transaction currentTransaction = this.queue.take();
                transfer(currentTransaction, false);

            } catch (InterruptedException e) {
                break;
            }

        }

    }

}
