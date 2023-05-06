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

    public static WebElement getFirstOfferBuyLink(WebDriver driver) {
        String firstOfferBuyLinkfullXPath = "/html/body/div/table[2]/tbody/tr[1]/td[5]/a";
        return PO_AllOffersView.checkElementBy(driver, "free", firstOfferBuyLinkfullXPath).get(0);
    }

    public static String getFirstOfferId(WebDriver driver) {
        WebElement firstOfferBuyLink = getFirstOfferBuyLink(driver);
        // Sacar la id del href partiendo por /
        String buyLink = firstOfferBuyLink.getAttribute("href");
        String[] buyLinkParts = buyLink.split("/");
        return buyLinkParts[buyLinkParts.length-1];
    }

    public static int buyFirstOffer(WebDriver driver) {
        String firstOfferPriceFullXPath = "/html/body/div/table/tbody/tr[1]/td[4]";
        WebElement firstOfferPrice = checkElementBy(driver, "free", firstOfferPriceFullXPath).get(0);
        String firstOfferPriceText = firstOfferPrice.getText();
        firstOfferPriceText = firstOfferPriceText.substring(0, firstOfferPriceText.length()-2);
        int res = Integer.parseInt(firstOfferPriceText);
        WebElement firstOfferBuyLink = getFirstOfferBuyLink(driver);

        firstOfferBuyLink.click();
        return res;
    }

    public static void openFirstOfferChat(WebDriver driver) {
        List<WebElement> firstOfferRow = PO_View.checkElementBy(driver, "free", "//tbody/tr[1]");
        WebElement chatLink= firstOfferRow.get(0).findElement(By.className("chat"));

        chatLink.click();
    }
}
