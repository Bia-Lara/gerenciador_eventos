package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventDialog {

    private static final By DIALOG = By.id("event-dialog");
    private static final By NAME = By.id("event-name");
    private static final By START = By.id("event-start");
    private static final By END = By.id("event-end");
    private static final By FEEDBACK = By.id("event-feedback");
    private static final By TITLE = By.id("event-dialog-title");
    private static final By SAVE = By.cssSelector("#event-dialog button[type=submit]");
    private static final By BACK = By.cssSelector("#event-dialog .dialog-actions [data-close-dialog]");
    private static final DateTimeFormatter DATETIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public EventDialog(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
    }

    public EventDialog preencher(String nome, LocalDateTime inicio, LocalDateTime fim) {
        WebElement campoNome = driver.findElement(NAME);
        campoNome.clear();
        campoNome.sendKeys(nome);
        definirDataHora(START, inicio);
        definirDataHora(END, fim);
        return this;
    }

    public EventDialog salvar() {
        driver.findElement(SAVE).click();
        return this;
    }

    public EventDialog voltar() {
        driver.findElement(BACK).click();
        return this;
    }

    public boolean estaAberto() {
        return driver.findElement(DIALOG).isDisplayed();
    }

    public void aguardarFechamento() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
    }

    public String titulo() {
        return driver.findElement(TITLE).getText();
    }

    public String nome() {
        return driver.findElement(NAME).getAttribute("value");
    }

    public boolean nomeAceitoPeloNavegador() {
        return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();", driver.findElement(NAME));
    }

    public String aguardarMensagemDeErro() {
        wait.until(d -> !d.findElement(FEEDBACK).getText().isBlank());
        return driver.findElement(FEEDBACK).getText();
    }

    private void definirDataHora(By campo, LocalDateTime valor) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];", driver.findElement(campo), valor.format(DATETIME_LOCAL));
    }
}
