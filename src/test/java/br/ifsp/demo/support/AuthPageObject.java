package br.ifsp.demo.support;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    public void alternarParaAbaDeCadastro() {
        driver.findElement(By.id("register-tab")).click();
    }

    public void cadastrar(String nome, String sobrenome, String email, String senha) {
        driver.findElement(By.id("register-name")).sendKeys(nome);
        driver.findElement(By.id("register-lastname")).sendKeys(sobrenome);
        driver.findElement(By.id("register-email")).sendKeys(email);
        driver.findElement(By.id("register-password")).sendKeys(senha);
        driver.findElement(By.cssSelector("#register-form button[type=submit]")).click();
    }

    public String esperarMensagemDeFeedback() {
        WebElement feedback = driver.findElement(By.id("auth-feedback"));
        espera.until(d -> !feedback.getText().isBlank());
        return feedback.getText();
    }

    public boolean telaDeAutenticacaoEstaEscondida() {
        return driver.findElement(By.id("auth-view")).getAttribute("class").contains("hidden");
    }

    public boolean botaoDeLogoutEstaVisivel() {
        return driver.findElement(By.id("logout-button")).isDisplayed();
    }

    public boolean formularioDeLoginEstaVisivel() {
        return !driver.findElement(By.id("login-form")).getAttribute("class").contains("hidden");
    }

    public boolean formularioDeCadastroEstaVisivel() {
        return !driver.findElement(By.id("register-form")).getAttribute("class").contains("hidden");
    }

    public void sair() {
        driver.findElement(By.id("logout-button")).click();
    }

    public boolean campoSenhaDeCadastroEstaInvalido() {
        WebElement senha = driver.findElement(By.id("register-password"));
        Boolean valido = (Boolean) ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", senha);
        return !valido;
    }
}
