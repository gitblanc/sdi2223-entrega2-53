package com.uniovi.sdi2223entrega2test.n;

import com.uniovi.sdi2223entrega2test.n.pageobjects.*;
import com.uniovi.sdi2223entrega2test.n.util.SeleniumUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
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
        PO_SignUpView.fillForm(driver, "emailvalido@pruebas.com", "aaaa" ,"bbbb",
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
        PO_SignUpView.fillForm(driver, "", "", "", "", "77777",
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
        PO_LoginView.login(driver, "user01@email.com", "", "Identificación de usuario");
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
        List<WebElement> usersList = PO_AdminView.getUsersList(driver);

        Assertions.assertEquals(3, usersList.size());
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
     * y dicho usuario desaparece.
     */
    @Test
    @Order(12)
    void PR12(){
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");

        WebElement firstUserBeforeDeletion = PO_AdminView.getUsersList(driver).get(0);
        // La primera celda de la fila de un usuario es el correo
        String firstUserBeforeDeletionEmail = firstUserBeforeDeletion.findElement(By.tagName("td")).getText();
        PO_AdminView.deleteUsers(driver, 0);
        Assertions.assertNotEquals(firstUserBeforeDeletionEmail, PO_AdminView.getUsersList(driver).get(0).findElement(By.tagName("td")).getText());

        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
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
