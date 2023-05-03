package com.uniovi.sdi2223entrega2test.n;

import com.uniovi.sdi2223entrega2test.n.pageobjects.*;
import com.uniovi.sdi2223entrega2test.n.util.SeleniumUtils;
import io.restassured.RestAssured;
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

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Sdi2223Entrega2TestApplicationTests {
    // Windows
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    //static String Geckodriver = "C:\\Users\\mines\\Desktop\\nodejs\\geckodriver-v0.30.0-win64.exe";
    //static String Geckodriver = "C:\\Users\\uo277369\\Desktop\\geckodriver-v0.30.0-win64.exe";
    // MACOSX
    //static String PathFirefox = "/Applications/Firefox.app/Contents/MacOS/firefox-bin";
    //static String Geckodriver = "/Users/USUARIO/selenium/geckodriver-v0.30.0-macos";

    static String Geckodriver = "C:\\Users\\Diego\\Documents\\Universidad\\4º curso\\2º Semestre\\SDI\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";


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
        for (int i = 2; i < 23; i++) {
            PO_AllOffersView.checkElementBy(driver, "id", "pl-" + i).get(0).click();
        }

        // Estamos en la última página
        tableRows = PO_AllOffersView.getOffersList(driver);

        Assertions.assertEquals(4, tableRows.size());

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
        Assertions.assertEquals(99, elements.size());
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
