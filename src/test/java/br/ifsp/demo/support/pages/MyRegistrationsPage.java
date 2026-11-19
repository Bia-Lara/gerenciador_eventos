package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class MyRegistrationsPage {

    private static final By TAB = By.id("registrations-tab");
    private static final By PANEL = By.id("registrations-panel");
    private static final By EMPTY_STATE = By.id("registrations-empty");
    private static final By CARDS = By.cssSelector("#registrations-list .registration-card");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public MyRegistrationsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public MyRegistrationsPage abrir() {
        wait.until(ExpectedConditions.elementToBeClickable(TAB)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PANEL));
        return this;
    }

    public boolean aguardarEstadoVazio() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(EMPTY_STATE));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String textoDoEstadoVazio() {
        return driver.findElement(EMPTY_STATE).getText();
    }

    public int quantidadeDeCards() {
        return driver.findElements(CARDS).size();
    }

    public RegistrationCard cardDoEvento(String nomeDoEvento) {
        wait.until(d -> encontrarCard(nomeDoEvento) != null);
        return new RegistrationCard(encontrarCard(nomeDoEvento));
    }

    private WebElement encontrarCard(String nomeDoEvento) {
        List<WebElement> cards = driver.findElements(CARDS);
        for (WebElement card : cards) {
            if (card.findElement(By.tagName("h2")).getText().equals(nomeDoEvento)) {
                return card;
            }
        }
        return null;
    }

    public static class RegistrationCard {

        private final WebElement card;

        RegistrationCard(WebElement card) {
            this.card = card;
        }

        public String detalhes() {
            return card.findElement(By.tagName("p")).getText();
        }

        public String status() {
            return card.findElement(By.className("event-status")).getText();
        }

        public String categoria() {
            return card.findElement(By.className("registration-category")).getText();
        }

        public boolean possuiBotaoCancelar() {
            return !card.findElements(By.cssSelector("button.text-button")).isEmpty();
        }
    }
}