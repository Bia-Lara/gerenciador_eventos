package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class OrganizerPage {

    private static final By PANEL = By.id("organizer-panel");
    private static final By NEW_EVENT = By.id("new-event-button");
    private static final By EVENT_CARDS = By.cssSelector("#events-list .event-card");
    private static final By EVENT_TITLES = By.cssSelector("#events-list .event-card .event-title");
    private static final By FEEDBACK = By.id("dashboard-feedback");
    private static final By TOTAL = By.id("total-count");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Wait<WebDriver> fluentWait;

    public OrganizerPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        this.fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(StaleElementReferenceException.class);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PANEL));
    }

    public EventDialog abrirNovoEvento() {
        driver.findElement(NEW_EVENT).click();
        return new EventDialog(driver, wait);
    }

    public EventDialog abrirEdicao(String nomeDoEvento) {
        WebElement botao = fluentWait.until(d -> cardDoEvento(nomeDoEvento)
                .map(card -> card.findElement(By.xpath(".//button[normalize-space()='Editar']")))
                .orElse(null));
        botao.click();
        return new EventDialog(driver, wait);
    }

    private Optional<WebElement> cardDoEvento(String nomeDoEvento) {
        return driver.findElements(EVENT_CARDS).stream()
                .filter(card -> card.findElement(By.cssSelector(".event-title")).getText().equals(nomeDoEvento))
                .findFirst();
    }

    public List<String> nomesDosEventos() {
        return fluentWait.until(d -> d.findElements(EVENT_TITLES).stream()
                .map(WebElement::getText)
                .toList());
    }

    public String aguardarMensagemDeFeedback() {
        wait.until(d -> !d.findElement(FEEDBACK).getText().isBlank());
        return driver.findElement(FEEDBACK).getText();
    }

    public String totalDeEventos() {
        return driver.findElement(TOTAL).getText();
    }
}
