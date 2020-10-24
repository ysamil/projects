package de.moneymanager.banksystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class GameRepositoryTest {

    @Autowired
    GameRepository gameRepository;

    @AfterEach
    void tearDown() {
        this.gameRepository.deleteAll();
    }

    @Test
    void gameRepositoryTest() {
        //Create a new Game.
        Game game = new Game();

        //Get all attributes of the game.
        Long          fee           = game.getAccountFees();
        Double        credit        = game.getCreditInterest();
        Double        debit         = game.getDebitInterest();
        String        bankName      = game.getBankName();
        Boolean       bankStatement = game.getBankStatement();
        Boolean       bill          = game.getBill();
        Long          dispo         = game.getDispo();
        Long          startBalance  = game.getStartBalance();
        LocalDateTime firstInterval = game.getFirstInterval();
        Long          timeInterval  = game.getTimeInterval();

        //Check that the repository is empty before a game is saved.
        assertFalse(this.gameRepository.findAll().iterator().hasNext());

        //Save the Game.
        this.gameRepository.save(game);

        //After saving the repository should not be empty.
        assertTrue(this.gameRepository.findAll().iterator().hasNext());

        //Get the saved game.
        Game savedGame = this.gameRepository.findAll().iterator().next();

        //Compare all attributes of the saved game to the on created in the beginning.
        assertEquals(savedGame.getAccountFees(), fee);
        assertEquals(savedGame.getCreditInterest(), credit);
        assertEquals(savedGame.getDebitInterest(), debit);
        assertEquals(savedGame.getBankName(), bankName);
        assertEquals(savedGame.getBankStatement(), bankStatement);
        assertEquals(savedGame.getBill(), bill);
        assertEquals(savedGame.getDispo(), dispo);
        assertEquals(savedGame.getStartBalance(), startBalance);
        assertEquals(savedGame.getFirstInterval(), firstInterval);
        assertEquals(savedGame.getTimeInterval(), timeInterval);
    }

}
