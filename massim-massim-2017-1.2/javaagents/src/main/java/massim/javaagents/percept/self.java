/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.percept;

/**
 *
 * @author Sarah
 */
public class self {
    private int charge;
    private double lat;
    private int load;
    private double lon;
    private String name;
    private String role;
    private String team;
    private String LastAction;
    private String LastActionResult;
    private int teamMoney;

    public int getTeamMoney() {
        return teamMoney;
    }

    public void setTeamMoney(int teamMoney) {
        this.teamMoney = teamMoney;
    }

    public self(int charge, double lat, int load, double lon, String name, String role, String team, String LastAction, String LastActionResult) {
        this.charge = charge;
        this.lat = lat;
        this.load = load;
        this.lon = lon;
        this.name = name;
        this.role = role;
        this.team = team;
        this.LastAction = LastAction;
        this.LastActionResult = LastActionResult;
    }

    public self() {
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setLastAction(String LastAction) {
        this.LastAction = LastAction;
    }

    public void setLastActionResult(String LastActionResult) {
        this.LastActionResult = LastActionResult;
    }

    public int getCharge() {
        return charge;
    }

    public double getLat() {
        return lat;
    }

    public int getLoad() {
        return load;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getTeam() {
        return team;
    }

    public String getLastAction() {
        return LastAction;
    }

    public String getLastActionResult() {
        return LastActionResult;
    }
    
}