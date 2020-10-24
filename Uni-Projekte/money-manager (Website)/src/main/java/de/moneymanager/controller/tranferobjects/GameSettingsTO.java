package de.moneymanager.controller.tranferobjects;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class GameSettingsTO {

    @NotNull
    private String  bankName;
    @NotNull
    private double  debitInterest;
    @NotNull
    private double  creditInterest;
    @NotNull
    private double  startBalance;
    @NotNull
    private double  accountFees;
    @NotNull
    private boolean createBills;
    @NotNull
    private boolean createStatements;
    @NotNull
    private double  overdraft;
    @NotNull
    private String  startTime;
    @NotNull
    private int     intervalHour;
    @NotNull
    private int     intervalMin;

}
