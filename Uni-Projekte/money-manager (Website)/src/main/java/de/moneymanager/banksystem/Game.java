package de.moneymanager.banksystem;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Entity that describes a game.
 */
@Entity
@Getter
@Setter
public class Game {

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long id = 1L;

    private String bankName = "Money Manager";

    private Double debitInterest  = 0.0;
    private Double creditInterest = 0.0;
    private Long   accountFees    = 0L;

    private Boolean bankStatement = true;
    private Boolean bill          = true;

    private Long startBalance = 50_000_00L;
    private Long dispo        = 0L;

    private String blz = null;

    private Boolean running = false;

    private Long          timeInterval  = 24 * 60 * 60 * 1000L; // Every 24h
    private LocalDateTime firstInterval = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);

}
