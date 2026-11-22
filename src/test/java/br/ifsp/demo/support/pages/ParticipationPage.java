package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDate;

public class ParticipationPage {

    private static final By TAB = By.id("participation-tab");
    private static final By PANEL = By.id("participation-panel");
    private static final By CARDS = By.cssSelector("#participation-events-list .event-card");
    private static final By DATE_FILTER = By.id("participation-date");
    private static final By EMPTY_STATE_TITLE = By.cssSelector("#participation-empty h2");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ParticipationPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public ParticipationPage abrir() {
        wait.until(ExpectedConditions.elementToBeClickable(TAB)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PANEL));
        return this;
    }

    public String vagasPreenchidasDoEvento(String nomeDoEvento) {
        return cardDoEvento(nomeDoEvento).findElement(By.cssSelector(".participation-capacity strong")).getText();
    }

    public boolean eventoEstaLotado(String nomeDoEvento) {
        return !cardDoEvento(nomeDoEvento).findElements(By.cssSelector(".participation-capacity .event-status")).isEmpty();
    }

    public WebElement botaoDoEvento(String nomeDoEvento) {
        return cardDoEvento(nomeDoEvento).findElement(By.cssSelector(".participation-event-actions button"));
    }

    public RegistrationDialog inscreverNoEvento(String nomeDoEvento) {
        botaoDoEvento(nomeDoEvento).click();
        return new RegistrationDialog(driver, wait).aguardarAberto();
    }

    public ParticipationPage filtrarPorData(LocalDate data) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                driver.findElement(DATE_FILTER), data.toString());
        return this;
    }

    public boolean eventoEstaListado(String nomeDoEvento) {
        return encontrarCard(nomeDoEvento) != null;
    }

    public String aguardarTituloDoEstadoVazio() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMPTY_STATE_TITLE));
        return driver.findElement(EMPTY_STATE_TITLE).getText();
    }

    private WebElement cardDoEvento(String nomeDoEvento) {
        return wait.until(d -> encontrarCard(nomeDoEvento));
    }

    private WebElement encontrarCard(String nomeDoEvento) {
        try {
            return driver.findElements(CARDS).stream()
                    .filter(card -> card.findElement(By.cssSelector(".event-title")).getText().equals(nomeDoEvento))
                    .findFirst()
                    .orElse(null);
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }
}
