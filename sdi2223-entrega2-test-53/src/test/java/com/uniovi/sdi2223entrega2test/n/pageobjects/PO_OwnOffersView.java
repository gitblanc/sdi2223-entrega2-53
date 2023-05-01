package com.uniovi.sdi2223entrega2test.n.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_OwnOffersView extends PO_NavView {

    public static void clickAddOfferOption(WebDriver driver) {
        WebElement button = checkElementBy(driver, "id", "add-offer").get(0);
        button.click();
    }

    static public List<WebElement> getOffersList(WebDriver driver) {
        return checkElementBy(driver, "free", "//tbody/tr");
    }
}
