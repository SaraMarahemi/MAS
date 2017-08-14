/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import massim.javaagents.MailService;
import static massim.javaagents.agents.WarpAgent.stringParam;
import massim.javaagents.percept.AgentPercepts;
import massim.javaagents.percept.job;
import massim.javaagents.percept.shop;

/**
 *
 * @author Sarah
 */
public class MiladAgent extends Agent{
    private AgentPercepts AP = new AgentPercepts(); 
    private Queue<Action> actionQueue = new LinkedList<>();
    private String myJob;
    private String shop;
    private Set<String> jobsTaken = new HashSet<>();
    private boolean flag;
    
    public MiladAgent(String name, MailService mailbox) {
        super(name, mailbox);
        flag = false;
        System.out.println("Milad Agent");
    }

    @Override
    public void handlePercept(Percept percept) {
        throw new UnsupportedOperationException("ABCDE* Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    void makePerceptObjects()
    {
        List<Percept> percepts = new Vector<>();
        percepts = this.getPercepts();
        
        ///*** Changing percepts handle
        AP.setPercepts(percepts);
        if(getStepNumber() != 0) //Step Percept
        {
            AP.stepPercept();
        }
        else //Initial Percept
        {
            AP.initialize();
        }
        ///***
    }

    @Override
    public Action step() {
        //Percept
        makePerceptObjects();
        if(actionQueue.size() > 0) 
        {
            return actionQueue.poll();
        }
        if (myJob == null){
            Set<String> availableJobs = new HashSet<>(AP.Jobs.keySet());
            availableJobs.removeAll(jobsTaken);
            if(availableJobs.size() > 0){
                myJob = availableJobs.iterator().next(); // set job to agent
                jobsTaken.add(myJob);
                broadcast(new Percept("taken", new Identifier(myJob)), getName());
            }
        }
        if(myJob != null){
            // plan the job
            if( (AP.getSelfInfo().getLastAction().compareTo("buy") == 0) &&  (AP.getSelfInfo().getLastAction().compareTo("successful") == 0) )
            {
                flag = true;
                actionQueue.clear();
            }
            if(AP.getSelfInfo().getLastAction().compareTo("deliver_job")==0 && AP.getSelfInfo().getLastAction().compareTo("successful") == 0)
            {
                flag = false;
            }
            // 1. acquire items
            job currentJob = AP.Jobs.get(myJob);
            if(currentJob == null){
                say("I lost my job :(");
                currentJob = null;
                return new Action("skip");
            }
            if(flag == false)
            {
                for(int i = 0; i < currentJob.getJobRequireds().size(); i++)
                {
                    // 1.1 get enough items of that type
                    String itemName = currentJob.getJobRequireds().get(i).getLeft();
                    int amount = currentJob.getJobRequireds().get(i).getRight();
                    // find a shop selling the item
                    List<shop> shops = AP.shopsByItem.get(currentJob.getJobRequireds().get(i).getLeft());
                    if (shop == null)
                            shop = shops.get(0).getShopName();
                    actionQueue.add(new Action("goto", new Identifier(shop)));
                    // buy the items
                    actionQueue.add(new Action("buy", new Identifier(itemName), new Numeral(amount)));
                }
            }
            if (flag == true)
            {
                // 2. get items to storage
                actionQueue.add(new Action("goto", new Identifier(currentJob.getJobStorage())));
                // 2.1 deliver items
                actionQueue.add(new Action("deliver_job", new Identifier(myJob)));
            }
        }
        return actionQueue.peek() != null? actionQueue.poll() : new Action("skip");
        
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch (message.getName()){
            case "taken":
                jobsTaken.add(stringParam(message.getParameters(), 0));
                break;
        }
    }
    
}
