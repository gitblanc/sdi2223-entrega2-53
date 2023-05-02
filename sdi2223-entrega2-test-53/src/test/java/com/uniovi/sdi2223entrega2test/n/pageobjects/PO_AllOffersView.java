package com.uniovi.sdi2223entrega2test.n.pageobjects;


import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_AllOffersView extends PO_NavView{
    public static void writeIntoSearchBar(WebDriver driver, String searchText) {
        String searchBarType = "class";
        String searchBarText = "search-query form-control";
        List<WebElement> searchBarList = checkElementBy(driver, searchBarType, searchBarText);
        // Tiene que haber una barra de búsqueda
        Assertions.assertEquals(1, searchBarList.size());
        WebElement searchBar = searchBarList.get(0);
        // Escribimos en ella
        searchBar.click();
        searchBar.clear();
        searchBar.sendKeys(searchText);
        // Pinchamos el botón de buscar
        By boton = By.className("btn");
        driver.findElement(boton).click();
        // Comprobamos que se recarga la página
        checkElementBy(driver, searchBarType, searchBarText);
    }

    static public List<WebElement> getOffersList(WebDriver driver) {
        return checkElementBy(driver, "free", "//tbody/tr");
    }

    public static void buyFirstOffer(WebDriver driver) {
        List<WebElement> offersToBuyRow = PO_View.checkElementBy(driver, "free", "//tbody/tr[1]");
        List<WebElement> offerToBuyLink = offersToBuyRow.get(0).findElements(By.className("buy"));

        WebElement buyLink = offerToBuyLink.get(0);

        buyLink.click();
    }

    public static void openFirstOfferChat(WebDriver driver) {
        List<WebElement> firstOfferRow = PO_View.checkElementBy(driver, "free", "//tbody/tr[1]");
        WebElement chatLink= firstOfferRow.get(0).findElement(By.className("chat"));

        chatLink.click();
    }
}
