package br.ifsp.demo.support.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
}