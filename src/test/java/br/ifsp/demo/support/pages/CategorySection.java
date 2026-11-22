package br.ifsp.demo.support.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class CategorySection {

    private static final By EVENT_CARDS = By.cssSelector("#events-list .event-card");
    private static final By FEEDBACK = By.id("dashboard-feedback");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String nomeDoEvento;

    public CategorySection(WebDriver driver, WebDriverWait wait, String nomeDoEvento) {
        this.driver = driver;
        this.wait = wait;
        this.nomeDoEvento = nomeDoEvento;
    }

    public CategorySection adicionar(String nome, String capacidade, String preco) {
        WebElement secao = secaoAberta();
        secao.findElement(By.cssSelector("[data-category-name]")).sendKeys(nome);
        secao.findElement(By.cssSelector("[data-category-capacity]")).sendKeys(capacidade);
        secao.findElement(By.cssSelector("[data-category-price]")).sendKeys(preco);
        secao.findElement(By.cssSelector("button[type=submit]")).click();
        return this;
    }

    public CategorySection removerLimitesDoFormulario() {
        WebElement secao = secaoAberta();
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].removeAttribute('maxlength'); arguments[1].removeAttribute('min');",
                secao.findElement(By.cssSelector("[data-category-name]")),
                secao.findElement(By.cssSelector("[data-category-capacity]")));
        return this;
    }

    public boolean capacidadeAceitaPeloNavegador() {
        WebElement capacidade = secaoAberta().findElement(By.cssSelector("[data-category-capacity]"));
        return (Boolean) ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", capacidade);
    }

    public CategorySection remover(String nomeDaCategoria, boolean confirmar) {
        secaoAberta()
                .findElement(By.cssSelector("[aria-label='Remover categoria " + nomeDaCategoria + "']"))
                .click();
        Alert confirmacao = wait.until(ExpectedConditions.alertIsPresent());
        if (confirmar) {
            confirmacao.accept();
        } else {
            confirmacao.dismiss();
        }
        return this;
    }

    public String aceitarAlerta() {
        Alert alerta = wait.until(ExpectedConditions.alertIsPresent());
        String texto = alerta.getText();
        alerta.accept();
        return texto;
    }

    public String aguardarMensagemDeFeedback() {
        wait.until(d -> !d.findElement(FEEDBACK).getText().isBlank());
        return driver.findElement(FEEDBACK).getText();
    }

    public List<String> categoriasListadas() {
        return wait.until(d -> {
            try {
                return secaoAberta().findElements(By.cssSelector(".category-item span")).stream()
                        .map(WebElement::getText)
                        .toList();
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
    }

    public String contador() {
        return wait.until(d -> {
            try {
                return cardDoEvento().findElement(By.cssSelector(".category-count")).getText();
            } catch (StaleElementReferenceException | NullPointerException e) {
                return null;
            }
        });
    }

    private WebElement secaoAberta() {
        WebElement secao = wait.until(d -> {
            WebElement card = cardDoEvento();
            return card == null ? null : card.findElement(By.cssSelector(".category-manager"));
        });
        if (secao.getAttribute("open") == null) {
            secao.findElement(By.tagName("summary")).click();
        }
        return secao;
    }

    private WebElement cardDoEvento() {
        try {
            return driver.findElements(EVENT_CARDS).stream()
                    .filter(card -> card.findElement(By.cssSelector(".event-title")).getText().equals(nomeDoEvento))
                    .findFirst()
                    .orElse(null);
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }
}
