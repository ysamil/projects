package de.moneymanager.controller.tranferobjects;

import lombok.Data;

@Data
public class TransactionTO {

    private String  bankAccount;
    private String  date;
    private String  usage;
    private double  amount;
    private boolean canceled;

}
