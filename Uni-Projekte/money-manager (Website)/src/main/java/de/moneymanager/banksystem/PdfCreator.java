package de.moneymanager.banksystem;

import de.moneymanager.accounts.AccountServiceImpl;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.transaction.Transaction;
import de.moneymanager.transaction.TransactionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Service for creating Pdfs.
 */
@Service
public class PdfCreator {
    private final BankSystemService bankSystemService;

    private final PDFont FONT = PDType1Font.HELVETICA_BOLD;

    private final InboxEntryRepository inboxEntryRepository;

    private final AccountServiceImpl accountService;

    private final TransactionService transactionService;

    private static final String EVALUATION_NAME = "Evaluation.pdf";

    public PdfCreator(@Lazy BankSystemService bankSystemService, InboxEntryRepository inboxEntryRepository, AccountServiceImpl accountService, TransactionService transactionService) {
        this.bankSystemService = bankSystemService;
        this.inboxEntryRepository = inboxEntryRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }


    public String getEvaluationName() {
        return EVALUATION_NAME;
    }


    /**
     * Creates a bill for a specific bank account.
     */
    public void createBill(BankAccount bankAccount, Long fee) {
        String pdfName;
        Iterable<InboxEntry> inboxIter = inboxEntryRepository.findAllByBankAccount(bankAccount);

        Double realFee = fee / 100.0;

        // Create an empty list
        List<InboxEntry> inbox = new ArrayList<>();

        // Add each element of iterator to the List
        inboxIter.forEach(inbox::add);

        pdfName = "Bill-" + bankAccount.getIban() + "-" + inbox.size() + ".pdf";

        //create pdf
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();

            doc.addPage(page);

            PDPageContentStream contents = null;

            contents = new PDPageContentStream(doc, page);

            //text 1
            contents.beginText();

            contents.setFont(FONT, 30);

            contents.newLineAtOffset(0, 700);

            contents.showText("Bill");

            contents.endText();

            //text 2
            contents.beginText();

            contents.setFont(FONT, 20);

            contents.newLineAtOffset(0, 600);

            contents.showText("fees: " + realFee + " Euro");

            contents.endText();

            contents.close();

            doc.save("src/main/resources/Pdfs/" + pdfName);

            doc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //add Inbox entry
        InboxEntry inboxEntry = new InboxEntry();

        inboxEntry.setBankAccount(bankAccount);
        inboxEntry.setTimeStamp(LocalDateTime.now());
        inboxEntry.setPdfName(pdfName);

        //save it
        inboxEntryRepository.save(inboxEntry);
    }

    /**
     * Creates a bank statement for a specific bank account.
     */
    public void createBankStatement(BankAccount bankAccount) {
        String pdfName;
        Iterable<InboxEntry> inboxIter = inboxEntryRepository.findAllByBankAccount(bankAccount);

        // Create an empty list
        List<InboxEntry> inbox = new ArrayList<>();

        // Add each element of iterator to the List
        inboxIter.forEach(inbox::add);
        pdfName = "Statement-" + bankAccount.getIban() + "-" + inbox.size() + ".pdf";

        //create pdf
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();

            doc.addPage(page);

            PDPageContentStream contents = null;

            contents = new PDPageContentStream(doc, page);

            //Accountstatement
            contents.beginText();

            contents.setFont(FONT, 30);

            contents.newLineAtOffset(0, 700);

            contents.showText("Account statement");

            contents.endText();

            //Iban
            contents.beginText();

            contents.setFont(FONT, 20);

            contents.newLineAtOffset(0, 600);

            contents.showText(bankAccount.getIban());

            contents.endText();

            //table of transactions
            LocalDateTime lastCall = LocalDateTime.now().minusSeconds(bankSystemService.getTimeInterval() / 1000);
            List<Transaction> transactions = transactionService.getAccountStatement(bankAccount, lastCall, LocalDateTime.now());

            //TODO

            String[][] table = new String[transactions.size() + 1][4];
            table[0][0] = "Source";

            table[0][1] = "Destination";

            table[0][2] = "Ammount";

            table[0][3] = "User";

            for (int row = 1; row < transactions.size(); row++) {

                table[row][0] = transactions.get(row).getSource().getName();
                table[row][1] = transactions.get(row).getDestination().getName();
                table[row][2] = "" + transactions.get(row).getAmount() / 100.0;
                table[0][3] = transactions.get(row).getUser().getName();

            }

            createTable(table, doc, 10, 100, 10);

            contents.close();

            doc.save("src/main/resources/Pdfs/" + pdfName);

            doc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //add Inbox entry
        InboxEntry inboxEntry = new InboxEntry();

        inboxEntry.setBankAccount(bankAccount);
        inboxEntry.setTimeStamp(LocalDateTime.now());
        inboxEntry.setPdfName(pdfName);

        //save it
        inboxEntryRepository.save(inboxEntry);
    }

    /**
     * Creates a evaluation for the whole game.
     */
    public void createEvaluation() {

        //create pdf
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();

            doc.addPage(page);

            PDPageContentStream contents = null;

            contents = new PDPageContentStream(doc, page);

            //Evaluation
            contents.beginText();

            contents.setFont(FONT, 30);

            contents.newLineAtOffset(0, 700);

            contents.showText("Evaluation");

            contents.endText();

            contents.close();

            //table of balances
            List<BankAccount> bankAccounts = accountService.getBankAccounts();

            String[][] table = new String[bankAccounts.size() + 1][3];
            table[0][0] = "Name";

            table[0][1] = "IBan";

            table[0][2] = "Balance";

            for (int row = 1; row < bankAccounts.size(); row++) {

                table[row][0] = bankAccounts.get(row).getName();
                table[row][1] = bankAccounts.get(row).getIban();
                table[row][2] = "" + bankAccounts.get(row).getBalance() / 100.0;

            }

            createTable(table, doc, 10, 100, 10);

            doc.save("src/main/resources/Pdfs/" + EVALUATION_NAME);

            doc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a table.
     */
    private void createTable(String[][] table, PDDocument doc, int rowDist, int colDist, int fontsize) {
        PDPage page = new PDPage();
        doc.addPage(page);

        try {
            PDPageContentStream contents = new PDPageContentStream(doc, page);

            int ty = 700;
            for (String[] row : table) {
                int tx = 30;
                for (String element : row) {
                    contents.beginText();

                    contents.setFont(FONT, fontsize);

                    contents.newLineAtOffset(tx, ty);

                    if (element != null) {
                        contents.showText(element);

                    }
                    contents.endText();

                    tx += colDist;
                }
                ty -= rowDist;
            }
            contents.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
