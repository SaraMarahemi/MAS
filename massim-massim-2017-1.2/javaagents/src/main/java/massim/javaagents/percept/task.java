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
public class task {
    private String job;
    private String action;
    private String item;
    private int amount;
    private String destination;

    public task() {
    }

    public task(String job, String action, String item, int amount, String destination) {
        this.job = job;
        this.action = action;
        this.item = item;
        this.amount = amount;
        this.destination = destination;
    }
    public void setTask(String job, String action, String item, int amount, String destination) {
        this.job = job;
        this.action = action;
        this.item = item;
        this.amount = amount;
        this.destination = destination;
    }
    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    
}
