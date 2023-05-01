package com.uniovi.sdi2223entrega2test.n;

import com.uniovi.sdi2223entrega2test.n.pageobjects.*;
import com.uniovi.sdi2223entrega2test.n.util.SeleniumUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Sdi2223Entrega2TestApplicationTests {
    // Windows
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    static String Geckodriver = "C:\\Users\\mines\\Desktop\\nodejs\\geckodriver-v0.30.0-win64.exe";
    // MACOSX
    //static String PathFirefox = "/Applications/Firefox.app/Contents/MacOS/firefox-bin";
    //static String Geckodriver = "/Users/USUARIO/selenium/geckodriver-v0.30.0-macos";
//Común a Windows y a MACOSX
    static WebDriver driver = getDriver(PathFirefox, Geckodriver);
    static String URL = "http://localhost:8081";

    public static WebDriver getDriver(String PathFirefox, String Geckodriver) {
        System.setProperty("webdriver.firefox.bin", PathFirefox);
        System.setProperty("webdriver.gecko.driver", Geckodriver);
        driver = new FirefoxDriver();
        return driver;
    }

    @BeforeEach
    public void setUp() {
        driver.navigate().to(URL);
    }

    //Después de cada prueba se borran las cookies del navegador
    @AfterEach
    public void tearDown() {
        driver.manage().deleteAllCookies();
    }

    //Antes de la primera prueba
    @BeforeAll
    static public void begin() {
        driver.navigate().to(URL+"/tests/insert");
        // Esperar a que se inserten
        PO_View.checkElementBy(driver, "text", "datos de los tests insertados");
    }

    //Al finalizar la última prueba
    @AfterAll
    static public void end() {
        driver.navigate().to(URL+"/tests/delete");
        // Esperar a que se borren
        PO_View.checkElementBy(driver, "text", "datos de los tests quitados");
//Cerramos el navegador al finalizar las pruebas
        driver.quit();
    }

    /**
     * [Prueba1] Registro de Usuario con datos válidos.
     */
    @Test
    @Order(1)
    void PR01() {
        PO_NavView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "emailvalido@pruebas.com", "testsBorrar" ,"testsBorrar",
                "2001-01-01", "77777", "77777");
        String checkText = "Lista de ofertas propias";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba2] Registro de Usuario con datos inválidos (email, nombre, apellidos y fecha de nacimiento vacíos).
     */
    @Test
    @Order(2)
    public void PR02() {
        PO_NavView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "  ", "  ", "  ", "   ", "77777",
                "77777");
        SeleniumUtils.textIsPresentOnPage(driver, "Registrar usuario");
    }

    /**
     * [Prueba3] Registro de Usuario con datos inválidos (repetición de contraseña inválida).
     */
    @Test
    @Order(3)
    public void PR03() {
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "emailvalido2@pruebas.comm", "aaaa", "bbbb",
                "2001-01-01", "77777", "66666");
        String checkText = "Se ha producido un error al registrar el usuario, contraseñas distintas.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    /**
     * [Prueba4] Registro de Usuario con datos inválidos (email existente).
     */
    @Test
    @Order(4)
    public void PR04() {
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "emailvalido@pruebas.com", "aaaa", "bbbb",
                "2001-01-01", "77777", "77777");
        String checkText = "Se ha producido un error al registrar el usuario, email ya existe.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    /**
     * [Prueba5] Inicio de sesión con datos válidos (administrador).
     */
    @Test
    @Order(5)
    void PR05(){
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.loginAsAdmin(driver);
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba6] Inicio de sesión con datos válidos (usuario estándar).
     */
    @Test
    @Order(6)
    void PR06(){
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.login(driver, "user01@email.com", "user01", "Lista de ofertas propias");
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba7] Inicio de sesión con datos inválidos (usuario estándar, email existente, pero contraseña incorrecta).
     */
    @Test
    @Order(7)
    void PR07(){
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.login(driver, "user01@email.com", "error", "Email o password incorrecto");
    }

    /**
     * [Prueba8] Inicio de sesión con datos inválidos (campo email o contraseña vacíos).
     */
    @Test
    @Order(8)
    public void PR08() {
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.login(driver, "user01@email.com", "  ", "Identificación de usuario");
    }

    /**
     * [Prueba9] Hacer clic en la opción de salir de sesión y comprobar que se redirige a la página de inicio de
     * sesión (Login).
     */
    @Test
    @Order(9)
    void PR09(){
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.login(driver, "user01@email.com", "user01", "Lista de ofertas propias");
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba10] Comprobar que el botón cerrar sesión no está visible si el usuario no está autenticado.
     */
    @Test
    @Order(10)
    void PR10(){
        SeleniumUtils.textIsNotPresentOnPage(driver, "Desconectarse");
    }

    /**
     * [Prueba11] Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema, contabilizando al menos el número de usuarios.
     */
    @Test
    @Order(11)
    void PR11(){
        PO_LoginView.loginAsAdmin(driver);

        for (int i = 2; i <= 4; i++) {
            List<WebElement> usersList = PO_AdminView.getUsersList(driver);
            Assertions.assertEquals(5, usersList.size());
            if (i < 4) {
                PO_AdminView.checkElementBy(driver, "id", "pl-"+i).get(0).click();
            }
        }

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
     * y dicho usuario desaparece.
     */
    @Test
    @Order(12)
    void PR12(){
        PO_LoginView.loginAsAdmin(driver);

        String emailBefore = PO_AdminView.getUsersList(driver).get(0).getAttribute("value");
        PO_AdminView.deleteUsers(driver, 0);
        String emailAfter = PO_AdminView.getUsersList(driver).get(0).getAttribute("value");
        Assertions.assertNotEquals(emailBefore, emailAfter);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba13] Ir a la lista de usuarios, borrar el último usuario de la lista, comprobar que la lista se actualiza
     * y dicho usuario desaparece.
     */
    @Test
    @Order(13)
    void PR13(){
        PO_LoginView.loginAsAdmin(driver);

        // Ir a la última página
        for (int i = 2; i < 4; i++) {
            PO_AdminView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }

        // Borrar último usuario
        List<WebElement> usersList = PO_AdminView.getUsersList(driver);
        String emailBefore = usersList.get(usersList.size()-1).getAttribute("value");
        PO_AdminView.deleteUsers(driver, usersList.size()-1);
        usersList = PO_AdminView.getUsersList(driver);
        String emailAfter = usersList.get(usersList.size()-1).getAttribute("value");
        Assertions.assertNotEquals(emailBefore, emailAfter);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la lista se actualiza y dichos
     * usuarios desaparecen.
     */
    @Test
    @Order(14)
    void PR14(){
        PO_LoginView.loginAsAdmin(driver);

        List<WebElement> users = PO_AdminView.getUsersList(driver);

        String email2 = users.get(2).getAttribute("value");
        String email3 = users.get(3).getAttribute("value");
        String email4 = users.get(4).getAttribute("value");

        PO_AdminView.deleteUsers(driver, 2, 3, 4);
        users = PO_AdminView.getUsersList(driver);

        for (WebElement user: users) {
            String emailUser = user.getAttribute("value");
            Assertions.assertNotEquals(emailUser, email2);
            Assertions.assertNotEquals(emailUser, email3);
            Assertions.assertNotEquals(emailUser, email4);
        }

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba15] Intentar borrar el usuario que se encuentra en sesión y comprobar que no ha sido borrado
     * (porque no es un usuario administrador o bien, porque, no se puede borrar a sí mismo, si está
     * autenticado).
     */
    @Test
    @Order(15)
    void PR15(){
        PO_LoginView.loginAsAdmin(driver);

        PO_AdminView.tryToDeleteAdmin(driver);
        driver.navigate().to(URL+"/users/list");

        List<WebElement> users = PO_AdminView.getUsersList(driver);

        for (WebElement user: users) {
            String emailUser = user.getAttribute("value");
            Assertions.assertNotEquals(emailUser, "admin@email.com");
        }

        PO_LoginView.logout(driver);
        // Comprobar que no hemos sido borrados
        PO_LoginView.loginAsAdmin(driver);
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba16] Ir al formulario de alta de oferta, rellenarla con datos válidos y pulsar el botón Submit.
     * Comprobar que la oferta sale en el listado de ofertas de dicho usuario.
     */
    @Test
    @Order(16)
    void PR16(){
        PO_LoginView.login(driver, "user11@email.com", "user11", "Lista de ofertas propias");

        PO_OwnOffersView.clickAddOfferOption(driver);
        //Ahora vamos a rellenar la oferta.
        String checkText = "Oferta nueva 1";
        PO_AddOfferView.fillFormAddOffer(driver, checkText, "testsBorrar", "100");
        // Ir a la última página
        for (int i = 2; i < 4; i++) {
            PO_AdminView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }
        //Comprobamos que aparece la oferta en la página
        List<WebElement> elements = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, elements.get(0).getText());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba17] Ir al formulario de alta de oferta, rellenarla con datos inválidos (campo título vacío y precio
     * en negativo) y pulsar el botón Submit. Comprobar que se muestra el mensaje de campo inválido.
     */
    @Test
    @Order(17)
    void PR17(){
        PO_LoginView.login(driver, "user11@email.com", "user11", "Lista de ofertas propias");

        PO_OwnOffersView.clickAddOfferOption(driver);
        PO_AddOfferView.fillFormAddOffer(driver, "  ", "testsBorrar", "-100");
        String checkText = "Se ha producido un error al añadir la oferta, título vacío";
        List<WebElement> result = PO_AddOfferView.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText , result.get(0).getText());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba18] Mostrar el listado de ofertas para dicho usuario y comprobar que se muestran todas las que
     * existen para este usuario.
     */
    @Test
    @Order(18)
    void PR18(){
        PO_LoginView.login(driver, "user12@email.com", "user12", "Lista de ofertas propias");

        Assertions.assertEquals(5, PO_OwnOffersView.getOffersList(driver).size());
        PO_OwnOffersView.checkElementBy(driver, "id", "pl-2").get(0).click();
        Assertions.assertEquals(5, PO_OwnOffersView.getOffersList(driver).size());

        PO_LoginView.logout(driver);
    }

    /* Ejemplos de pruebas de llamada a una API-REST */
    /* ---- Probamos a obtener lista de canciones sin token ---- */
    /*
    @Test
    @Order(11)
    public void PR11() {
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/songs";
        Response response = RestAssured.get(RestAssuredURL);
        Assertions.assertEquals(403, response.getStatusCode());
    }

    @Test
    @Order(38)
    public void PR38() {
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/users/login";
        //2. Preparamos el parámetro en formato JSON
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "prueba1@prueba1.com");
        requestParams.put("password", "prueba1");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());
        //3. Hacemos la petición
        Response response = request.post(RestAssuredURL);
        //4. Comprobamos que el servicio ha tenido exito
        Assertions.assertEquals(200, response.getStatusCode());
    }
    */
}
