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
    
    public task(task tempTask) {
        this.job = tempTask.job;
        this.action = tempTask.action;
        this.item = tempTask.item;
        this.amount = tempTask.amount;
        this.destination = tempTask.destination;
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
    public boolean compareTo (task tempTask)
    {
        if(this.job.compareTo(tempTask.getJob()) == 0 
                & this.action.compareTo(tempTask.getAction())== 0
                //& this.destination.compareTo(tempTask.getDestination()) == 0
                //& this.amount == tempTask.getAmount()
                //& this.item.compareTo(tempTask.getItem()) == 0
                )
        {
            return true;
        }
        return false;
    }
}
