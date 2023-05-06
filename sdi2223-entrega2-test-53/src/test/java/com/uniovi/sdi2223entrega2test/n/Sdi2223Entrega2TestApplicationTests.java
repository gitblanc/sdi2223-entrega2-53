package com.uniovi.sdi2223entrega2test.n;

import com.uniovi.sdi2223entrega2test.n.pageobjects.*;
import com.uniovi.sdi2223entrega2test.n.util.SeleniumUtils;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Sdi2223Entrega2TestApplicationTests {
    // Windows
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    static String Geckodriver = "C:\\Users\\mines\\Desktop\\nodejs\\geckodriver-v0.30.0-win64.exe";
    //static String Geckodriver = "C:\\Users\\uo277369\\Desktop\\geckodriver-v0.30.0-win64.exe";
    // MACOSX
    //static String PathFirefox = "/Applications/Firefox.app/Contents/MacOS/firefox-bin";
    //static String Geckodriver = "/Users/USUARIO/selenium/geckodriver-v0.30.0-macos";

    //static String Geckodriver = "C:\\Users\\Diego\\Documents\\Universidad\\4º curso\\2º Semestre\\SDI\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";


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

        // Borrar nuevo usuario
        PO_LoginView.loginAsAdmin(driver);
        // Ir a la última página
        for (int i = 2; i < 5; i++) {
            PO_AdminView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }

        // Lo borramos no alterar el número total de usuarios para los siguientes tests
        List<WebElement> usersList = PO_AdminView.getUsersList(driver);
        PO_AdminView.deleteUsers(driver, usersList.size()-1);

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
        PO_SignUpView.fillForm(driver, "user01@email.com", "aaaa", "bbbb",
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
        PO_AdminView.goToLastUsersPage(driver);

        // Borrar último usuario
        List<WebElement> usersList = PO_AdminView.getUsersList(driver);
        String emailBefore = usersList.get(usersList.size()-1).getAttribute("value");
        PO_AdminView.deleteUsers(driver, usersList.size()-1);
        // Ir a la última página
        PO_AdminView.goToLastUsersPage(driver);
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
        PO_AddOfferView.fillFormAddOffer(driver, checkText, "testsBorrar", "100", false);
        // Ir a la última página
        for (int i = 2; i < 4; i++) {
            PO_OwnOffersView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }
        //Comprobamos que aparece la oferta en la página
        List<WebElement> elements = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, elements.get(0).getText());

        // La borramos para no alterar el número total de ofertas para los siguientes tests
        List<WebElement> offers = PO_OwnOffersView.getOffersList(driver);
        WebElement offer = offers.get(offers.size()-1);
        offer.findElement(By.className("offer-delete")).click();

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
        PO_AddOfferView.fillFormAddOffer(driver, "  ", "testsBorrar", "-100", false);
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

    /**
     * [Prueba19] Ir a la lista de ofertas, borrar la primera oferta de la lista, comprobar que la lista se actualiza
     * y que la oferta desaparece.
     */
    @Test
    @Order(19)
    void PR19(){
        PO_LoginView.login(driver, "user12@email.com", "user12", "Lista de ofertas propias");

        WebElement offer = PO_OwnOffersView.getOffersList(driver).get(0);
        String titleOfferToDelete = offer.findElements(By.tagName("td")).get(0).getText();
        offer.findElement(By.className("offer-delete")).click();

        offer = PO_OwnOffersView.getOffersList(driver).get(0);
        String titleFirstOfferAfterDeletion = offer.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertNotEquals(titleOfferToDelete, titleFirstOfferAfterDeletion);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba20] Ir a la lista de ofertas, borrar la última oferta de la lista, comprobar que la lista se actualiza
     * y que la oferta desaparece.
     */
    @Test
    @Order(20)
    void PR20(){
        PO_LoginView.login(driver, "user13@email.com", "user13", "Lista de ofertas propias");

        PO_OwnOffersView.goToLastPage(driver);
        List<WebElement> offers = PO_OwnOffersView.getOffersList(driver);
        WebElement offer = offers.get(offers.size()-1);
        String titleOfferToDelete = offer.findElements(By.tagName("td")).get(0).getText();
        offer.findElement(By.className("offer-delete")).click();

        PO_OwnOffersView.goToLastPage(driver);
        offers = PO_OwnOffersView.getOffersList(driver);
        offer = offers.get(offers.size()-1);
        String titleFirstOfferAfterDeletion = offer.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertNotEquals(titleOfferToDelete, titleFirstOfferAfterDeletion);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba21] Ir a la lista de ofertas, borrar una oferta de otro usuario, comprobar que la oferta no se
     * borra.
     */
    @Test
    @Order(21)
    void PR21(){
        PO_LoginView.login(driver, "user12@email.com", "user12", "Lista de ofertas propias");

        // Ir a la lista de todas las ofertas
        PO_OwnOffersView.clickAllOffersOption(driver);
        // Pillar el id de una oferta de otro usuario
        String idOffer = PO_AllOffersView.getFirstOfferId(driver);
        // Invocar el delete con ese id
        driver.navigate().to(URL+"/offers/delete/"+idOffer);
        // Comprobar el mensaje de error
        String checkText = "Acceso denegado";
        PO_OwnOffersView.checkElementBy(driver, "text", checkText);
        // Comprobar que no se ha borrado
        PO_OwnOffersView.clickAllOffersOption(driver);
        String idOfferAfterDeletion = PO_AllOffersView.getFirstOfferId(driver);
        Assertions.assertEquals(idOffer, idOfferAfterDeletion);
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba22] Ir a la lista de ofertas, borrar una oferta propia que ha sido vendida, comprobar que la
     * oferta no se borra.
     */
    @Test
    @Order(22)
    void PR22(){
        PO_LoginView.login(driver, "user14@email.com", "user14", "Lista de ofertas propias");

        WebElement offer = PO_OwnOffersView.getOffersList(driver).get(0);
        String titleOfferToDelete = offer.findElements(By.tagName("td")).get(0).getText();
        offer.findElement(By.className("offer-delete")).click();

        offer = PO_OwnOffersView.getOffersList(driver).get(0);
        String titleFirstOfferAfterDeletion = offer.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertEquals(titleOfferToDelete, titleFirstOfferAfterDeletion);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba23] Hacer una búsqueda con el campo vacío y comprobar que se muestra la página que
     * corresponde con el listado de las ofertas existentes en el sistema
     */
    @Test
    @Order(23)
    void PR23(){
        PO_LoginView.login(driver, "user14@email.com", "user14", "Lista de ofertas propias");
        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "");

        // Comprobamos que salen todas las ofertas del sistema
        List<WebElement> tableRows = PO_AllOffersView.getOffersList(driver);
        Assertions.assertEquals(5, tableRows.size());

        // Ir a la última página
        for (int i = 2; i < 18; i++) {
            PO_AllOffersView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }

        // Estamos en la última página
        tableRows = PO_AllOffersView.getOffersList(driver);

        Assertions.assertEquals(5, tableRows.size());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba24] Hacer una búsqueda escribiendo en el campo un texto que no exista y comprobar que se
     * muestra la página que corresponde, con la lista de ofertas vacía.
     */
    @Test
    @Order(24)
    void PR24(){
        PO_LoginView.login(driver, "user14@email.com", "user14", "Lista de ofertas propias");

        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "ofertaquenoexiste");
        WebElement tableBody = PO_AllOffersView.checkElementBy(driver, "free", "//tbody").get(1);
        List<WebElement> tableRows = tableBody.findElements(By.tagName("tr"));
        Assertions.assertEquals(0, tableRows.size());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba25] Hacer una búsqueda escribiendo en el campo un texto en minúscula o mayúscula y
     * comprobar que se muestra la página que corresponde, con la lista de ofertas que contengan dicho
     * texto, independientemente que el título esté almacenado en minúsculas o mayúscula.
     */
    @Test
    @Order(25)
    void PR25(){
        PO_LoginView.login(driver, "user14@email.com", "user14", "Lista de ofertas propias");

        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "OFERTA-USER09-N1");
        List<WebElement> tableRows = PO_AllOffersView.getOffersList(driver);
        Assertions.assertEquals(2, tableRows.size());
        WebElement tableRow = tableRows.get(0);
        String title = tableRow.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertEquals("Oferta-user09-n1", title);
        WebElement tableRow2 = tableRows.get(1);
        String title2 = tableRow2.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertEquals("Oferta-user09-n10", title2);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba26] Sobre una búsqueda determinada (a elección de desarrollador), comprar una oferta que
     * deja un saldo positivo en el contador del comprobador. Y comprobar que el contador se actualiza
     * correctamente en la vista del comprador.
     */
    @Test
    @Order(26)
    void PR26(){
        PO_LoginView.login(driver, "user14@email.com", "user14", "Lista de ofertas propias");

        int amountBefore = PO_OwnOffersView.getMyAmount(driver);
        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "OFERTA-USER09-N1");
        int price = PO_AllOffersView.buyFirstOffer(driver);
        int amountAfter = PO_OwnOffersView.getMyAmount(driver);
        Assertions.assertEquals(amountAfter, amountBefore - price);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba27] Sobre una búsqueda determinada (a elección de desarrollador), comprar una oferta que
     * deja un saldo 0 en el contador del comprobador. Y comprobar que el contador se actualiza
     * correctamente en la vista del comprador.
     */
    @Test
    @Order(27)
    void PR27(){
        PO_LoginView.login(driver, "user09@email.com", "user09", "Lista de ofertas propias");

        int amountBefore = PO_OwnOffersView.getMyAmount(driver);
        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "OFERTA-USER14-N10");
        int price = PO_AllOffersView.buyFirstOffer(driver);
        int amountAfter = PO_OwnOffersView.getMyAmount(driver);
        Assertions.assertEquals(amountAfter, amountBefore - price);
        Assertions.assertEquals(amountAfter, 0);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba28] Sobre una búsqueda determinada (a elección de desarrollador), intentar comprar una oferta
     * que esté por encima de saldo disponible del comprador. Y comprobar que se muestra el mensaje
     * de saldo no suficiente.
     */
    @Test
    @Order(28)
    void PR28(){
        PO_LoginView.login(driver, "user08@email.com", "user08", "Lista de ofertas propias");

        PO_OwnOffersView.clickAllOffersOption(driver);
        // Primero hacemos una compra que nos deje por debajo de 60
        PO_AllOffersView.writeIntoSearchBar(driver, "Oferta-USER14-N5");
        PO_AllOffersView.buyFirstOffer(driver);
        int amountBefore = PO_OwnOffersView.getMyAmount(driver);
        // Compramos una oferta de 60
        PO_OwnOffersView.clickAllOffersOption(driver);
        PO_AllOffersView.writeIntoSearchBar(driver, "OFERTA-USER14-N6");
        PO_AllOffersView.buyFirstOffer(driver);
        int amountAfter = PO_OwnOffersView.getMyAmount(driver);
        String checkText = "Error comprar la oferta: dinero insuficiente";
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(1, elements.size());
        Assertions.assertEquals(checkText, elements.get(0).getText());
        Assertions.assertEquals(amountAfter, amountBefore);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba29] Ir a la opción de ofertas compradas del usuario y mostrar la lista. Comprobar que aparecen
     * las ofertas que deben aparecer
     */
    @Test
    @Order(29)
    void PR29(){
        PO_LoginView.login(driver, "user07@email.com", "user07", "Lista de ofertas propias");

        PO_OwnOffersView.clickPurchasesOption(driver);
        List<WebElement> purchases = PO_OwnOffersView.getOffersList(driver);
        Assertions.assertEquals(1, purchases.size());
        WebElement purchase = purchases.get(0);
        String title = purchase.findElements(By.tagName("td")).get(0).getText();
        Assertions.assertEquals("Oferta-user14-n1", title);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba30] Al crear una oferta, marcar dicha oferta como destacada y a continuación comprobar: i)
     * que aparece en el listado de ofertas destacadas para los usuarios y que el saldo del usuario se
     * actualiza adecuadamente en la vista del ofertante (comprobar saldo antes y después, que deberá
     * diferir en 20€).
     */
    @Test
    @Order(30)
    void PR30(){
        PO_LoginView.login(driver, "user11@email.com", "user11", "Lista de ofertas propias");

        PO_OwnOffersView.clickAddOfferOption(driver);
        int amountBefore = PO_OwnOffersView.getMyAmount(driver);
        //Ahora vamos a rellenar la oferta.
        String checkText = "Oferta nueva destacada";
        PO_AddOfferView.fillFormAddOffer(driver, checkText, "testsBorrar", "100", true);
        int amountAfter = PO_OwnOffersView.getMyAmount(driver);
        Assertions.assertEquals(amountAfter, amountBefore - 20);
        PO_LoginView.logout(driver);
        PO_LoginView.login(driver, "user12@email.com", "user12", "Lista de ofertas propias");
        PO_OwnOffersView.clickAllOffersOption(driver);
        String title =PO_AllOffersView.getOffersList(driver).get(0).findElements(By.tagName("td")).get(0).getText();
        Assertions.assertEquals(title, checkText);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba31] Sobre el listado de ofertas de un usuario con más de 20 euros de saldo, pinchar en el enlace
     * Destacada y a continuación comprobar: i) que aparece en el listado de ofertas destacadas para los
     * usuarios y que el saldo del usuario se actualiza adecuadamente en la vista del ofertante (comprobar
     * saldo antes y después, que deberá diferir en 20€ ).
     */
    @Test
    @Order(31)
    void PR31(){
        PO_LoginView.login(driver, "user11@email.com", "user11", "Lista de ofertas propias");

        String checkText = "Oferta-user11-n1";
        int amountBefore = PO_OwnOffersView.getMyAmount(driver);
        //Ahora vamos a destacar la oferta.
        PO_OwnOffersView.checkElementBy(driver, "free", "/html/body/div/table/tbody/tr[1]/td[6]/a").get(0).click();
        // Comprobamos
        int amountAfter = PO_OwnOffersView.getMyAmount(driver);
        Assertions.assertEquals(amountAfter, amountBefore - 20);
        PO_LoginView.logout(driver);
        PO_LoginView.login(driver, "user12@email.com", "user12", "Lista de ofertas propias");
        PO_OwnOffersView.clickAllOffersOption(driver);
        List<WebElement> offers = PO_OwnOffersView.checkElementBy(driver, "class", "title-highlight");
        String title =offers.get(0).getText();
        Assertions.assertEquals(title, checkText);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba32] Sobre el listado de ofertas de un usuario con menos de 20 euros de saldo, pinchar en el
     * enlace Destacada y a continuación comprobar que se muestra el mensaje de saldo no suficiente.
     */
    @Test
    @Order(32)
    void PR32(){
        PO_LoginView.login(driver, "user10@email.com", "user10", "Lista de ofertas propias");

        //Ahora vamos a destacar la oferta.
        PO_OwnOffersView.checkElementBy(driver, "free", "/html/body/div/table/tbody/tr[1]/td[6]/a").get(0).click();
        // Comprobamos
        String checkText= "No tienes al menos 20 euros";
        WebElement error = PO_OwnOffersView.checkElementBy(driver, "text", checkText).get(0);
        Assertions.assertEquals(error.getText(), checkText);

        driver.navigate().to(URL+"/publications");
        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba33] Intentar acceder sin estar autenticado a la opción de listado de usuarios. Se deberá volver
     * al formulario de login.
     */
    @Test
    @Order(33)
    void PR33(){
        driver.navigate().to(URL+"/users/list");

        PO_View.checkElementBy(driver, "text", "Identificación de usuario");
    }

    /**
     * [Prueba34] Intentar acceder sin estar autenticado a la opción de listado de conversaciones
     * [REQUISITO OBLIGATORIO S5]. Se deberá volver al formulario de login.
     */
    @Test
    @Order(34)
    void PR34(){
        /*
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/offers/chats/list";
        Response response = RestAssured.get(RestAssuredURL);
        Assertions.assertEquals(403, response.getStatusCode());

         */
        driver.navigate().to(URL+"/apiclient/client.html?w=conversations");
        PO_View.checkElementBy(driver, "id", "widget-login");
    }

    /**
     * [Prueba35] Estando autenticado como usuario estándar intentar acceder a una opción disponible solo
     * para usuarios administradores (Añadir menú de auditoria (visualizar logs)). Se deberá indicar un
     * mensaje de acción prohibida.
     */
    @Test
    @Order(35)
    void PR35(){
        PO_LoginView.login(driver, "user10@email.com", "user10", "Lista de ofertas propias");

        driver.navigate().to(URL+"/users/logs");

        PO_View.checkElementBy(driver, "text", "Solo el administrador puede acceder a lista de logs");
        driver.navigate().to(URL+"/publications");

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba36] Estando autenticado como usuario administrador visualizar todos los logs generados en
     * una serie de interacciones. Esta prueba deberá generar al menos dos interacciones de cada tipo y
     * comprobar que el listado incluye los logs correspondientes.
     */
    @Test
    @Order(36)
    void PR36(){
        // Generar interacciones
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");

//		String log1ExpectedType = "LOGIN-ERR";
//		String log1ExpectedDescription = "user14@email.com";
        PO_LoginView.fillLoginForm(driver, "user14@email.com", "aaaaa");

//		String log2ExpectedType = "LOGIN-ERR";
//		String log2ExpectedDescription = "user10@email.com";
        PO_LoginView.fillLoginForm(driver, "user10@email.com", "bbbbb");

//		String log3ExpectedType = "LOGIN-EX";
//		String log3ExpectedDescription = "user14@email.com";
        PO_LoginView.fillLoginForm(driver, "user14@email.com", "user14");

//		String log4ExpectedType = "LOGIN-PET";
//		String log4ExpectedMapping = "offer/add";
//		String log4ExpectedHttpMethod = "GET";
        PO_OwnOffersView.clickAddOfferOption(driver);

//		String log5ExpectedType = "ALTA";
//		String log5ExpectedMapping = "offer/add";
//		String log5ExpectedHttpMethod = "POST";
//		String log5ExpectedParam1 = "Oferta-user14-n11";
//		String log5ExpectedParam2 = "testsBorrar";
//		String log5ExpectedParam3 = "110";
        PO_AddOfferView.fillFormAddOffer(driver, "Oferta-user14-n11", "testsBorrar", "110", false);

//		String log6ExpectedType = "LOGOUT";
//		String log6ExpectedDescription = "user14@email.com";
        PO_LoginView.logout(driver);

//		String log7ExpectedType = "LOGIN-PET";
//		String log7ExpectedMapping = "signup";
//		String log7ExpectedHttpMethod = "GET";
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");

//		String log8ExpectedType = "ALTA";
//		String log8ExpectedMapping = "signup";
//		String log8ExpectedHttpMethod = "POST";
//		String log8ExpectedParam1 = "user123@email.com";
//		String log8ExpectedParam2 = "user123";
//		String log8ExpectedParam3 = "testsBorrar";
//		String log8ExpectedParam4 = "77777";
//		String log8ExpectedParam5 = "77777";
        PO_SignUpView.fillForm(driver, "user123@email.com", "testsBorrar", "testsBorrar", "2021-01-01", "77777", "77777");

//		String log9ExpectedType = "LOGOUT";
//		String log9ExpectedDescription = "user123@email.com";
        PO_LoginView.logout(driver);

//		String log10ExpectedType = "LOGIN-EX";
//		String log10ExpectedDescription = "admin@email.com";
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");

        // Ver logs
        PO_AdminView.clickLogsOption(driver);

        List<WebElement> logs = PO_View.checkElementBy(driver, "free", "//tbody/tr");
        Assertions.assertTrue(logs.size() >= 10);

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba34] Estando autenticado como usuario administrador, ir a visualización de logs, pulsar el
     * botón/enlace borrar logs y comprobar que se eliminan los logs de la base de datos.
     */
    @Test
    @Order(37)
    void PR37(){
        PO_LoginView.loginAsAdmin(driver);

        PO_AdminView.clickLogsOption(driver);

        PO_View.checkElementBy(driver, "id", "buttonDeleteLogs").get(0).click();

        List<WebElement> logsBody = PO_View.checkElementBy(driver, "free", "//tbody");
        List<WebElement> logs = logsBody.get(0).findElements(By.tagName("tr"));
        // Log de que se borro todos los logs
        Assertions.assertEquals(1, logs.size());

        PO_LoginView.logout(driver);
    }

    /**
     * [Prueba38] Inicio de sesión con datos válidos.
     */
    @Test
    @Order(38)
    void PR38(){
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/users/login";

        // preparo peticion
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user08@email.com");
        requestParams.put("password", "user08");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago peticion y compruebo que devuelve 200
        Response response = request.post(RestAssuredURL);
        Assertions.assertEquals(200, response.getStatusCode());
    }

    /**
     * [Prueba39] Inicio de sesión con datos inválidos (email existente, pero contraseña incorrecta).
     */
    @Test
    @Order(39)
    void PR39(){
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/users/login";

        // preparo peticion
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user08@email.com");
        requestParams.put("password", "asdf");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago peticion y compruebo que devuelve 401
        Response response = request.post(RestAssuredURL);
        Assertions.assertEquals(401, response.getStatusCode());
    }

    /**
     * [Prueba40] Inicio de sesión con datos inválidos (campo email o contraseña vacíos)..
     */
    @Test
    @Order(40)
    void PR40(){
        final String RestAssuredURL = "http://localhost:8081/api/v1.0/users/login";

        // preaparo peticion
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "    ");
        requestParams.put("password", "   ");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago peticion y compruebo que devuelve 401
        Response response = request.post(RestAssuredURL);
        Assertions.assertEquals(401, response.getStatusCode());
    }

    /**
     * [Prueba41] Mostrar el listado de ofertas para dicho usuario y comprobar que se muestran todas las que
     * existen para este usuario. Esta prueba implica invocar a dos servicios: S1 y S2.
     */
    @Test
    @Order(41)
    void PR41(){
        final String loginURL = "http://localhost:8081/api/v1.0/users/login";
        final String offersURL = "http://localhost:8081/api/v1.0/offers";

        // preparo login
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user08@email.com");
        requestParams.put("password", "user08");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago login y guardo el token
        Response response = request.post(loginURL);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String token = jsonPathEvaluator.get("token");
        String sessionCookie = response.getCookie("connect.sid");

        // preparo consulta a ofertas
        request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("token", token);
        request.cookie("connect.sid", sessionCookie);

        // consulta a ofertas
        response = request.get(offersURL);
        jsonPathEvaluator = response.jsonPath();
        ArrayList offers = jsonPathEvaluator.get("offers");

        // compruebo resultado y codigo respuesta
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(90, offers.size());
    }

    /**
     * [Prueba42] Enviar un mensaje a una oferta. Esta prueba consistirá en comprobar que el servicio
     * almacena correctamente el mensaje para dicha oferta. Por lo tanto, el usuario tendrá que
     * identificarse (S1), enviar un mensaje para una oferta de id conocido (S3) y comprobar que el
     * mensaje ha quedado bien registrado (S4).
     */
    @Test
    @Order(42)
    void PR42(){
        final String loginURL = "http://localhost:8081/api/v1.0/users/login";
        final String offersURL = "http://localhost:8081/api/v1.0/offers";
        final String chatURL = "http://localhost:8081/api/v1.0/offers/chats/byoffer/";
        final String newMessageURL = "http://localhost:8081/api/v1.0/chat/";

        // preparo login
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user08@email.com");
        requestParams.put("password", "user08");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago login y guardo el token
        Response response = request.post(loginURL);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String token = jsonPathEvaluator.get("token");
        String sessionCookie = response.getCookie("connect.sid");

        // preparo consulta a ofertas
        request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("token", token);
        request.cookie("connect.sid", sessionCookie);

        // consulta a ofertas y cojo el id de la primera
        response = request.get(offersURL);
        Assertions.assertEquals(200, response.getStatusCode());
        jsonPathEvaluator = response.jsonPath();
        ArrayList<LinkedHashMap<String, String>> offers = jsonPathEvaluator.get("offers");
        String offerID = offers.get(0).get("_id");

        // consulta a nuevo chat
        response = request.get(chatURL+offerID);
        Assertions.assertEquals(200, response.getStatusCode());
        jsonPathEvaluator = response.jsonPath();
        String chatID = jsonPathEvaluator.get("chat");

        // consulta a enviar mensaje
        requestParams = new JSONObject();
        requestParams.put("messageText", "PRUEBA");
        request.body(requestParams.toJSONString());
        response = request.post(newMessageURL+offerID+"/"+chatID);
        Assertions.assertEquals(201, response.getStatusCode());

        // consulta a recuperar mensajes
        response = request.get(chatURL+offerID);
        jsonPathEvaluator = response.jsonPath();
        ArrayList<LinkedHashMap<String, String>> messages = jsonPathEvaluator.get("messages");

        // compruebo resultado y codigo respuesta
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, messages.size());
    }

    /**
     * [Prueba43] Enviar un primer mensaje una oferta propia y comprobar que no se inicia la conversación.
     * En este caso de prueba, el propietario de la oferta tendrá que identificarse (S1), enviar un mensaje
     * para una oferta propia (S3) y comprobar que el mensaje no se almacena (S4).
     */
    @Test
    @Order(43)
    void PR43(){
        final String loginURL = "http://localhost:8081/api/v1.0/users/login";
        final String offersURL = "http://localhost:8081/api/v1.0/offers";
        final String chatURL = "http://localhost:8081/api/v1.0/offers/chats/byoffer/";
        final String newMessageURL = "http://localhost:8081/api/v1.0/chat/";

        // preparo login
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user08@email.com");
        requestParams.put("password", "user08");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago login y guardo el token
        Response response = request.post(loginURL);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String token = jsonPathEvaluator.get("token");
        String sessionCookie = response.getCookie("connect.sid");

        // preparo consulta
        request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("token", token);
        request.cookie("connect.sid", sessionCookie);

        // id conocido de mi oferta
        String offerID = "6456bba94dec7434ef3c3a8f";

        // consulta a nuevo chat
        response = request.get(chatURL+offerID+"?otherUser=user08@email.com");
        Assertions.assertEquals(200, response.getStatusCode());
        jsonPathEvaluator = response.jsonPath();
        String chatID = jsonPathEvaluator.get("chat");

        // consulta a enviar mensaje
        requestParams = new JSONObject();
        requestParams.put("messageText", "PRUEBA");
        request.body(requestParams.toJSONString());
        response = request.post(newMessageURL+offerID+"/"+chatID);

        // compruebo codigo respuesta
        Assertions.assertEquals(404, response.getStatusCode());
    }

    /**
     * [Prueba44] Obtener los mensajes de una conversación. Esta prueba consistirá en comprobar que el
     * servicio retorna el número correcto de mensajes para una conversación. El ID de la conversación
     * deberá conocerse a priori. Por lo tanto, se tendrá primero que invocar al servicio de identificación
     * (S1), y solicitar el listado de mensajes de una conversación de id conocido a continuación (S4),
     * comprobando que se retornan los mensajes adecuados.
     */
    @Test
    @Order(44)
    void PR44(){
        final String loginURL = "http://localhost:8081/api/v1.0/users/login";
        final String messagesURL = "http://localhost:8081/api/v1.0/offers/chats/";

        // preparo login
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user07@email.com");
        requestParams.put("password", "user07");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago login y guardo el token
        Response response = request.post(loginURL);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String token = jsonPathEvaluator.get("token");
        String sessionCookie = response.getCookie("connect.sid");

        // preparo consulta
        request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("token", token);
        request.cookie("connect.sid", sessionCookie);

        // id conocido del chat
        String chatID = "1435bba94dec7434ef3c3a7a";

        // consulta a nuevo chat
        response = request.get(messagesURL+chatID);
        // compruebo codigo respuesta
        Assertions.assertEquals(200, response.getStatusCode());
        jsonPathEvaluator = response.jsonPath();
        ArrayList<LinkedHashMap<String, String>> chats = jsonPathEvaluator.get("messages");
        Assertions.assertEquals(1, chats.size());
    }

    /**
     * [Prueba45] Obtener la lista de conversaciones de un usuario. Esta prueba consistirá en comprobar que
     * el servicio retorna el número correcto de conversaciones para dicho usuario. Por lo tanto, se tendrá
     * primero que invocar al servicio de identificación (S1), y solicitar el listado de conversaciones a
     * continuación (S5) comprobando que se retornan las conversaciones adecuadas.
     */
    @Test
    @Order(45)
    void PR45(){
        final String loginURL = "http://localhost:8081/api/v1.0/users/login";
        final String chatsURL = "http://localhost:8081/api/v1.0/offers/chats/list";

        // preparo login
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user07@email.com");
        requestParams.put("password", "user07");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        // hago login y guardo el token
        Response response = request.post(loginURL);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String token = jsonPathEvaluator.get("token");
        String sessionCookie = response.getCookie("connect.sid");

        // preparo consulta a chats
        request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("token", token);
        request.cookie("connect.sid", sessionCookie);

        // consulta a chats
        response = request.get(chatsURL);
        // compruebo resultado y codigo respuesta
        Assertions.assertEquals(200, response.getStatusCode());
        jsonPathEvaluator = response.jsonPath();

        ArrayList<LinkedHashMap<String, String>> chats = jsonPathEvaluator.get("chats");
        Assertions.assertEquals(1, chats.size());
    }

    /**
     * [Prueba48] Inicio de sesión con datos válidos.
     */
    @Test
    @Order(48)
    void PR48(){
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "user08");
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "text", "Listado de ofertas");
        Assertions.assertEquals(1, elements.size());
    }

    /**
     * [Prueba49] Inicio de sesión con datos inválidos (email existente, pero contraseña incorrecta).
     */
    @Test
    @Order(49)
    void PR49(){
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "asdfasdf");
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "id", "div-errors");
        Assertions.assertEquals(1, elements.size());
    }

    /**
     * [Prueba50] Inicio de sesión con datos inválidos (campo email o contraseña vacíos).
     */
    @Test
    @Order(50)
    void PR50(){
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "        ");
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "id", "div-errors");
        Assertions.assertEquals(1, elements.size());
    }

    /**
     * [Prueba51] Mostrar el listado de ofertas disponibles y comprobar que se muestran todas las que existen,
     * menos las del usuario identificado.
     */
    @Test
    @Order(51)
    void PR51(){
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "user08");
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "text", "Listado de ofertas");
        Assertions.assertEquals(1, elements.size());
        elements = PO_OwnOffersView.checkElementBy(driver, "free", "//tbody/tr");
        Assertions.assertEquals(90, elements.size());
    }

    /**
     * [Prueba52] Sobre listado de ofertas disponibles (a elección de desarrollador), enviar un mensaje a una
     * oferta concreta. Se abriría dicha conversación por primera vez. Comprobar que el mensaje aparece
     * en el listado de mensajes.
     */
    @Test
    @Order(52)
    void PR52(){
        // hago login
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "user08");

        // entro a una conversación
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//tbody/tr/td/button[@id='Oferta-user02-n1-user02@email.com']");
        Assertions.assertEquals(1, elements.size());
        elements.get(0).click();

        // mando un mensaje
        WebElement email = driver.findElement(By.name("message"));
        email.click();
        email.clear();
        email.sendKeys("PRUEBA");
        By boton = By.className("btn");
        driver.findElement(boton).click();

        // compruebo que aparece
        elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//tbody/tr/td[contains(text(), 'PRUEBA')]");
        Assertions.assertEquals(1, elements.size());
    }

    /**
     * [Prueba53] Sobre el listado de conversaciones enviar un mensaje a una conversación ya abierta.
     * Comprobar que el mensaje aparece en el listado de mensajes.
     */
    @Test
    @Order(53)
    void PR53(){
        // hago login
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "user08");

        // entro a listado de conversaciones
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//a[@id='link-conversations']");
        Assertions.assertEquals(1, elements.size());
        elements.get(0).click();

        // entro a la conversación
        elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//tbody/tr/td/button[@id='chat-Oferta-user02-n1']");
        Assertions.assertEquals(1, elements.size());
        elements.get(0).click();

        // mando un mensaje
        WebElement email = driver.findElement(By.name("message"));
        email.click();
        email.clear();
        email.sendKeys("NUEVO");
        By boton = By.className("btn");
        driver.findElement(boton).click();

        // compruebo que aparece
        elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//tbody/tr/td[contains(text(), 'NUEVO')]");
        Assertions.assertEquals(1, elements.size());
    }

    /**
     * [Prueba54] Mostrar el listado de conversaciones ya abiertas. Comprobar que el listado contiene la
     * cantidad correcta de conversaciones.
     */
    @Test
    @Order(54)
    void PR54(){
        // hago login
        driver.navigate().to(URL+"/apiclient/client.html");
        PO_LoginView.fillLoginForm(driver, "user14@email.com", "user14");

        // entro a listado de conversaciones
        List<WebElement> elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//a[@id='link-conversations']");
        Assertions.assertEquals(1, elements.size());
        elements.get(0).click();

        // cuento las conversaciones
        elements = PO_OwnOffersView.checkElementBy(driver, "free",
                "//tbody/tr");
        Assertions.assertEquals(1, elements.size());
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
