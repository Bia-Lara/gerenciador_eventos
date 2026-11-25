package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RegistrationDialog {

    private static final By DIALOG = By.id("registration-dialog");
    private static final By CATEGORY_SELECT = By.id("registration-category-id");
    private static final By CONFIRM_BUTTON = By.cssSelector("#registration-form button[type=submit]");
    private static final By BACK_BUTTON = By.cssSelector("#registration-form .dialog-actions [data-close-registration-dialog]");
    private static final By FEEDBACK = By.id("registration-feedback");
    private static final By REGISTRATIONS_PANEL = By.id("registrations-panel");
    private static final By REGISTRATIONS_FEEDBACK = By.id("registrations-feedback");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public RegistrationDialog(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public RegistrationDialog aguardarAberto() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        return this;
    }

    public boolean estaAberto() {
        return driver.findElement(DIALOG).getAttribute("open") != null;
    }

    public List<String> opcoesDeCategoria() {
        return new Select(driver.findElement(CATEGORY_SELECT)).getOptions().stream()
                .map(WebElement::getText)
                .toList();
    }

    public RegistrationDialog selecionarCategoria(String nomeDaCategoria) {
        Select select = new Select(driver.findElement(CATEGORY_SELECT));
        WebElement opcao = select.getOptions().stream()
                .filter(o -> o.getText().startsWith(nomeDaCategoria + " · "))
                .findFirst()
                .orElseThrow();
        select.selectByValue(opcao.getAttribute("value"));
        return this;
    }

    public RegistrationDialog confirmar() {
        driver.findElement(CONFIRM_BUTTON).click();
        return this;
    }

    public RegistrationDialog voltar() {
        driver.findElement(BACK_BUTTON).click();
        wait.until(d -> !estaAberto());
        return this;
    }

    public boolean categoriaHabilitada() {
        return driver.findElement(CATEGORY_SELECT).isEnabled();
    }

    public WebElement botaoConfirmar() {
        return driver.findElement(CONFIRM_BUTTON);
    }

    public boolean botaoConfirmarEstaClicavel() {
        WebElement botao = botaoConfirmar();
        return botao.isDisplayed() && botao.isEnabled();
    }

    public String aguardarMensagemDeErro() {
        wait.until(d -> !d.findElement(FEEDBACK).getText().isBlank());
        return driver.findElement(FEEDBACK).getText();
    }

    public String aguardarConfirmacaoEmMinhasInscricoes() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(REGISTRATIONS_PANEL));
        wait.until(d -> !d.findElement(REGISTRATIONS_FEEDBACK).getText().isBlank());
        return driver.findElement(REGISTRATIONS_FEEDBACK).getText();
    }
}
