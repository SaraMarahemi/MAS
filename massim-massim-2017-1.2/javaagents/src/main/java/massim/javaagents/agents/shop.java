/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.agents;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author Sarah
 */
public class shop {
    //shop lat="48.85888" lon="2.40388" name="shop2" restock="2"
    private double shopLat;
    private double shopLon;
    private String shopName;
    private int shopRestock;
    private List<shopItem> shopItems = new Vector<>();

    public shop(double shopLat, double shopLon, String shopName, int shopRestock, List<shopItem> shopItems) {
        this.shopLat = shopLat;
        this.shopLon = shopLon;
        this.shopName = shopName;
        this.shopRestock = shopRestock;
        this.shopItems = shopItems;
    }

    public double getShopLat() {
        return shopLat;
    }

    public void setShopLat(double shopLat) {
        this.shopLat = shopLat;
    }

    public double getShopLon() {
        return shopLon;
    }

    public void setShopLon(double shopLon) {
        this.shopLon = shopLon;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getShopRestock() {
        return shopRestock;
    }

    public void setShopRestock(int shopRestock) {
        this.shopRestock = shopRestock;
    }

    public List<shopItem> getShopItems() {
        return shopItems;
    }

    public void setShopItems(List<shopItem> shopItems) {
        this.shopItems = shopItems;
    }
    
    
}
