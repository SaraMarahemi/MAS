/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
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
import massim.javaagents.percept.auction;
import massim.javaagents.percept.job;
import massim.javaagents.percept.shop;

/**
 *
 * @author Sarah
 */
public class SaraAgent extends Agent{
    private AgentPercepts AP = new AgentPercepts(); 
    private Queue<Action> actionQueue = new LinkedList<>();
    private String myJob;
    private String shop;
    private Set<String> jobsTaken = new HashSet<>();
    private boolean simpleJobFlag;
    private String jobType;
    private job currentJob;
    private auction currentMission;
    private auction currentAuction;
    
    public SaraAgent(String name, MailService mailbox) {
        super(name, mailbox);
        simpleJobFlag = false;
        System.out.println("Sara Agent");
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
        AP.getSelfInfo().setName(this.getName());
        ///***
    }

    @Override
    public Action step() {
        //Percept
        makePerceptObjects();
        System.out.println("Step : "+AP.getStep()+ this.getName());
        if(actionQueue.size() > 0) 
        {
            //System.out.println("action queue : "+ actionQueue.size());
            
            return actionQueue.poll();
        }
        
        if (myJob == null){
            
            Set<String> availableJobs = new HashSet<>(AP.Jobs.keySet());
            Set<String> availableAuctions = new HashSet<>(AP.Auctions.keySet());
            Set<String> availableMissions = new HashSet<>(AP.Missions.keySet());
            availableJobs.removeAll(jobsTaken);
            availableAuctions.removeAll(jobsTaken);
            availableMissions.removeAll(jobsTaken);
            
            //start ->Peyman
            //choose a job !!!
            if(availableJobs.size() > 0)
            {
                //System.out.println("ABCDE 1");
                myJob = availableJobs.iterator().next(); // set job to agent !!!
                currentJob = AP.Jobs.get(myJob);
                System.out.println(myJob+""+currentJob.getJobID());
                        
                //assign job type :
                if(currentJob.getJobRequireds().size() > 1)
                {
                    jobType = "multiItemJob";
                    System.out.println("multiItemJob!!!");
                }
                else
                {
                    jobType = "simpleJob";
                    System.out.println("simpleJob!!!");
                }
                
                jobsTaken.add(myJob);
                broadcast(new Percept("taken", new Identifier(myJob)), getName());
            }
            else if(availableMissions.size() > 0)
            {
                myJob = availableMissions.iterator().next(); // set job to agent !!!
                currentMission = AP.Missions.get(myJob);
                //assign job type :
                jobType = "mission";
                System.out.println("mission!!!");
                jobsTaken.add(myJob);
                broadcast(new Percept("taken", new Identifier(myJob)), getName());
            }
            else if(availableAuctions.size() > 0)
            {
             myJob = availableAuctions.iterator().next();
             currentAuction = AP.Auctions.get(myJob);
             //assign job type :
                jobType = "auction";
                System.out.println("auction!!!");
                
                jobsTaken.add(myJob);
                broadcast(new Percept("taken", new Identifier(myJob)), getName());
            }
            //end ->Peyman
        }
        
        if(myJob != null){
            // plan the job
            if( (AP.getSelfInfo().getLastAction().compareTo("buy") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
            {
                simpleJobFlag = true;
                actionQueue.clear();
                
            }
            if(AP.getSelfInfo().getLastAction().compareTo("deliver_job")==0 && AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0)
            {
                simpleJobFlag = false;
                myJob = null;
                actionQueue.clear();
            }
            // 1. acquire items
            //job currentJob = AP.Jobs.get(myJob);
            if(currentJob == null && currentMission == null && currentAuction == null){
                say("I lost my job :(");
                currentJob = null;
                return new Action("recharge");
            }
            //***
            //Doing Job
            
            switch(jobType)
            {
                case "simpleJob":
                    System.out.println("case simpleJob");
                    DoSimpleJob();
                    break;
                case "multiItemJob":
                    System.out.println("case multiItemJob");
                    DoMultiItemJob();
                    break;
                case "mission":
                    System.out.println("case mission");
                    DoMission();
                    break;
                case "auction":
                    System.out.println("case auction");
                    DoAuction();
                    break;
                        
            }
          
        }
        if(actionQueue.peek() != null)
        {
           // System.out.println("actionQueue.peek() != null");
            return actionQueue.poll();
        }
        else
        {
           // System.out.println("actionQueue.peek() == null");
            return new Action("recharge");
        }
        
        
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch (message.getName()){
            case "taken":
                jobsTaken.add(stringParam(message.getParameters(), 0));
                break;
        }
    }
    
    private void DoSimpleJob()
    {
            ///***
        
            if(simpleJobFlag == false)
            {
                
                for(int i = 0; i < currentJob.getJobRequireds().size(); i++)
                {
                    // 1.1 get enough items of that type
                    String itemName = currentJob.getJobRequireds().get(i).getLeft();
                    int amount = currentJob.getJobRequireds().get(i).getRight();
                    // find a shop selling the item
                    List<shop> shops = AP.shopsByItem.get(itemName);
                    if (shop == null && shops != null)
                    {
                            shop = shops.get(0).getShopName();
                            
                    }
                    actionQueue.add(new Action("goto", new Identifier(shop)));
                    // buy the items
                    actionQueue.add(new Action("buy", new Identifier(itemName), new Numeral(amount)));
                }
            }
            if (simpleJobFlag == true)
            {
                //System.out.println("simpleJobFlag true");
                // 2. get items to storage
                actionQueue.add(new Action("goto", new Identifier(currentJob.getJobStorage())));
                // 2.1 deliver items
                actionQueue.add(new Action("deliver_job", new Identifier(myJob)));
            }
    }
    private void DoMultiItemJob()
    {
        
        
        
    }
    private void DoMission()
    {
        System.out.println("Do Mission :)");
        if(AP.getStep() <= currentMission.getAuctionEnd())
        {
            //Do mission
        }
        else
        {
            //the mission is died
            myJob = null;
            actionQueue.clear();
        }
        
    }
    private void DoAuction()
    {
        System.out.println("Do Auction :)");
        if(AP.getStep() <= currentAuction.getAuctionStart()+currentAuction.getAuctionTime())
        {
            //bid for auction
            LinkedList <Parameter>parameter = new LinkedList<Parameter>();
            parameter.add(new Identifier(currentAuction.getAuctionID()));
            parameter.add(new Identifier("25"));
            actionQueue.add(new Action("bid_for_job",parameter));
        }
        else
        {
            //cheking the result of auction
            if(AP.Auctions.get(currentAuction.getAuctionID()) != null )
            {
                // We win the auction
                if(AP.getStep() <= currentAuction.getAuctionEnd())
                {
                //Do auction
                }
                else
                {
                    // the auction is died
                    myJob = null;
                    actionQueue.clear();
                }
            }
            else
            {
                //We loose the auction
                myJob = null;
                actionQueue.clear();
            }
        }
    }
}
