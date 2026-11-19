package br.ifsp.demo.support;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AuthPageObject {

    private final WebDriver driver;
    private final WebDriverWait espera;

    public AuthPageObject(WebDriver driver) {
        this.driver = driver;
        this.espera = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void logar(String email, String senha) {
        driver.findElement(By.id("login-email")).sendKeys(email);
        driver.findElement(By.id("login-password")).sendKeys(senha);
        driver.findElement(By.cssSelector("#login-form button[type=submit]")).click();
    }

    public boolean esperarDashboardFicarVisivel() {
        try {
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-view")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean telaDeAutenticacaoEstaEscondida() {
        return driver.findElement(By.id("auth-view")).getAttribute("class").contains("hidden");
    }

    public boolean botaoDeLogoutEstaVisivel() {
        return driver.findElement(By.id("logout-button")).isDisplayed();
    }
}
