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
public class role {
    private int battery;
    private int load;
    private String name;
    private int speed;
    private List<String> tools = new Vector<>();

    public int getBattery() {
        return battery;
    }

    public int getLoad() {
        return load;
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public role(int battery, int load, String name, int speed, List<String> tools) {
        this.battery = battery;
        this.load = load;
        this.name = name;
        this.speed = speed;
        this.tools = tools;
    }

    public role() {
    }
    
    
}
