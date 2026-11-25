package br.ifsp.demo.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8091")
public abstract class BaseRegistrationsUiTest extends BaseApiTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        if (!Boolean.getBoolean("ui.headed")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:" + port);
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), "SenhaForte123");
    }

    protected void entrarComToken(String token) {
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('encontro.jwt', arguments[0]);", token);
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registrations-tab")));
    }

    protected boolean scriptInjetadoFoiExecutado() {
        return (Boolean) ((JavascriptExecutor) driver).executeScript("return window.__xss === true;");
    }
}