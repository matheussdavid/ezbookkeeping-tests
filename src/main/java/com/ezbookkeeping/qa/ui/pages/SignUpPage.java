package com.ezbookkeeping.qa.ui.pages;

import com.ezbookkeeping.qa.config.AppConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;

public class SignUpPage extends BasePage{


    private static final By INPUT_USERNAME = By.cssSelector("input[autocomplete='username']");
    private static final By INPUT_NICKNAME = By.cssSelector("input[autocomplete='nickname']");
    private static final By INPUT_EMAIL = By.cssSelector("input[autocomplete='email']");
    private static final By INPUT_PASSWORD = By.cssSelector("input[autocomplete='new-password']");

    private static final By INPUT_CONFIRM_PASSWORD = By.xpath("//input[@placeholder='Digite a senha novamente']");
    private static final By DROPDOWN_LANGUAGE = By.xpath("//div[@role='combobox'][.//label[contains(normalize-space(),'Language')]]");
    private static final By DROPDOWN_CURRENCY = By.xpath("//div[@role='combobox'][.//label[contains(normalize-space(),'Moeda Padrão') or contains(normalize-space(),'Default Currency')]]");
    private static final By DROPDOWN_FIRST_DAY_OF_WEEK = By.xpath("//div[@role='combobox'][.//label[contains(normalize-space(),'Primeiro Dia da Semana') or contains(normalize-space(),'First Day of Week')]]");

    private static final By BUTTON_BEFORE = By.xpath("//button[normalize-space()='Anterior']");
    private static final By BUTTON_NEXT = By.xpath("//button[normalize-space()='Próximo']");
    private static final By BUTTON_SEND = By.xpath("//button[normalize-space()='Enviar']");

    private static final By LABEL_USE_PRESET_CATEGORIES = By.xpath("//label[normalize-space()='Usar Categorias de Transação Predefinidas']");
    private static final By CHECKBOX_USE_PRESET_CATEGORIES = By.xpath("//input[@aria-label='Usar Categorias de Transação Predefinidas']");

    private static final By LINK_SIGN_UP = By.cssSelector("a[href$='#/login']");
    private static final By SNACKBAR = By.xpath("//div[@role='status']");

    public SignUpPage(WebDriver driver) {
        super(driver);
    }

    public SignUpPage abrir() {
        driver.get(AppConfig.UI_URL + "/desktop#/signup");
        return this;
    }

    public SignUpPage preencherUsername(String valor) {
        preencher(INPUT_USERNAME, valor);
        return this;
    }

    public SignUpPage preencherNickname(String valor) {
        preencher(INPUT_NICKNAME, valor);
        return this;
    }

    public SignUpPage preencherEmail(String valor) {
        preencher(INPUT_EMAIL, valor);
        return this;
    }

    public SignUpPage preencherSenha(String valor) {
        preencher(INPUT_PASSWORD, valor);
        return this;
    }

    public SignUpPage preencherConfirmarSenha(String valor) {
        preencher(INPUT_CONFIRM_PASSWORD, valor);
        return this;
    }
    
    public SignUpPage clicarAnterior() {
        clicar(BUTTON_BEFORE);
        return this;
    }

    public SignUpPage clicarProximo() {
        clicar(BUTTON_NEXT);
        return this;
    }

    public SignUpPage clicarEnviar() {
        clicar(BUTTON_SEND);
        return this;
    }

    public SignUpPage selecionarIdioma(String nomeIdioma) {
        clicar(DROPDOWN_LANGUAGE);
        clicar(opcaoComTexto(nomeIdioma));
        return this;
    }

    public String getTextoIdiomaSelecionado() {
        return textoSelecao("Language");
    }

    public SignUpPage selecionarMoedaPadrao(String nomeOuCodigo) {
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            WebElement campo = esperarClicavel(DROPDOWN_CURRENCY);

            try {
                campo.click();

                ((JavascriptExecutor) driver).executeScript(
                    "const inp = arguments[0].querySelector('input');" +
                    "const nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
                    "  window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeInputValueSetter.call(inp, arguments[1]);" +
                    "inp.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "inp.dispatchEvent(new Event('change', {bubbles: true}));",
                    campo, nomeOuCodigo);

                clicar(opcaoComTexto(nomeOuCodigo));
                return this;
            } catch (TimeoutException | StaleElementReferenceException | ElementNotInteractableException e) {
                if (tentativa == 3) {
                    throw e;
                }
            }
        }
        return this;
    }

    public String getTextoMoedaSelecionada() {
        WebElement campo = esperarVisivel(DROPDOWN_CURRENCY);
        return (String) ((JavascriptExecutor) driver).executeScript(
                "const t = arguments[0].querySelector('.v-autocomplete__selection-text');" +
                "return t ? t.textContent.trim() : '';", campo);
    }

    public SignUpPage selecionarPrimeiroDiaDaSemana(String nomeDia) {
        clicar(DROPDOWN_FIRST_DAY_OF_WEEK);
        clicar(opcaoComTexto(nomeDia));
        return this;
    }

    public SignUpPage marcarUsarCategoriasPredefinidas() {
        clicar(LABEL_USE_PRESET_CATEGORIES);
        return this;
    }

    public boolean isUsarCategoriasPredefinidasMarcado() {
        return isMarcado(CHECKBOX_USE_PRESET_CATEGORIES);
    }

    public String getTextoPrimeiroDiaSelecionado() {
        return textoSelecao("Primeiro Dia da Semana", "First Day of Week");
    }

    private By opcaoComTexto(String texto) {
        return By.xpath("//div[@role='option'][contains(normalize-space(.),'" + texto + "')]");
    }

    private String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private String textoSelecao(String... labelTextos) {
        String condicao = String.join(" or ",
                java.util.Arrays.stream(labelTextos)
                        .map(l -> ".//label[contains(normalize-space(),'" + l + "')]")
                        .toList());
        By selecao = By.xpath("(//div[contains(@class,'v-select')][" + condicao + "])//div[contains(@class,'v-select__selection')]");
        return esperarVisivel(selecao).getText();
    }

    public boolean isBotaoAnteriorHabilitado() {
        return isHabilitado(BUTTON_BEFORE);
    }

    public boolean temSnackbar() {
        return esperarVisivel(SNACKBAR).isDisplayed();
    }

    public String getMensagemErro() {
        return esperarVisivel(SNACKBAR).getText();
    }

    public SignUpPage esperarSnackbarComMensagem(String trecho) {
        WebElement snackbar = esperarVisivel(SNACKBAR);
        wait.until(d -> snackbar.getText().contains(trecho));
        return this;
    }
}
