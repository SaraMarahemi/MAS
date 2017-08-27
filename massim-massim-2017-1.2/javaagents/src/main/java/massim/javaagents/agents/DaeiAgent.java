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
import static java.nio.file.Files.list;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import jdk.nashorn.internal.ir.RuntimeNode;
import massim.javaagents.MailService;
import static massim.javaagents.agents.WarpAgent.stringParam;
import massim.javaagents.percept.AgentPercepts;
import massim.javaagents.percept.Pair;
import massim.javaagents.percept.auction;
import massim.javaagents.percept.item;
import massim.javaagents.percept.job;
import massim.javaagents.percept.shop;
import massim.javaagents.percept.storage;
import massim.javaagents.percept.task;

/**
 *
 * @author Daei
 */
public class DaeiAgent extends Agent{
    
    private AgentPercepts AP = new AgentPercepts(); 
    private Queue<Action> actionQueue = new LinkedList<>();
    private String myJob;
    private String shop;
    private Set<String> jobsTaken = new HashSet<>();
    private boolean flag;
    private List<job> DefinedJobs = new Vector<>();
    private List<task> DefinedTasks = new Vector<>();
    private List<task> takenTasks = new Vector<>();
    private List<auction> DefinedMissions = new Vector<>();
    //List<Pair<item,Pair<Integer,storage>>> Requirements = new Vector<>();
    
    public DaeiAgent(String name, MailService mailbox) {
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
            //Set<String> availableJobs = new HashSet<>(AP.Jobs.keySet());
            //availableJobs.removeAll(jobsTaken);
            //if(availableJobs.size() > 0){
                ///***
                //1
                DefineRequirement();
                //2
                //SplitRequiremnet(req);
                //3
                //chooseTask();
                //4
              //  DoTask();
                
                ///***
               // myJob = availableJobs.iterator().next(); // set job to agent
                //jobsTaken.add(myJob);
                //broadcast(new Percept("taken", new Identifier(myJob)), getName());
                //broadcast(new Percept("taken", new Identifier(jobId),new Identifier(action),new Identifier(itemName),new Identifier(itemAmount),new Identifier(destination)), getName());
            //}
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
            case "taskTaken":
                task tempTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),intParam(message.getParameters(), 3),stringParam(message.getParameters(), 4));
                takenTasks.add(tempTask);
                break;
        }
    }
    
    private void DefineRequirement()
    {
        List<job> availableJobs = new Vector<>();
        availableJobs = AP.getJobs();
        availableJobs.removeAll(DefinedJobs);
        for(int i=0; i<availableJobs.size();i++)
        {
            job tempJob = availableJobs.get(i);
            DefinedJobs.add(tempJob);
            for(int j=0; j<tempJob.getJobRequireds().size();j++)
            {
                String itemName = tempJob.getJobRequireds().get(j).getLeft();
                Integer itemAmount = tempJob.getJobRequireds().get(j).getRight();
                storage tempStorage = AP.Storages.get(tempJob.getJobStorage());
                item tempItem = AP.ItemsInEnv.get(itemName);
                /*Pair<item,Pair<Integer,storage>> requirement = new Pair(tempItem,new Pair(itemAmount,tempStorage));
                Requirements.add(requirement);*/
                // add tasks to list
                if (tempItem.getSubItems().size() == 0)
                {
                    task tempTask = new task(tempJob.getJobID(),"buy",itemName,itemAmount,"shop");
                    DefinedTasks.add(tempTask);
                    tempTask.setTask(tempJob.getJobID(), "carryToStorage", itemName, itemAmount, tempStorage.getName());
                    DefinedTasks.add(tempTask);
                }
                else
                {
                    for(int h = 0; h < tempItem.getSubItems().size(); h++)
                    {
                        task tempTask = new task(tempJob.getJobID(),"buy",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),"shop");
                        DefinedTasks.add(tempTask);
                        tempTask.setTask(tempJob.getJobID(),"carryToWorkshop",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),"workshop");
                        DefinedTasks.add(tempTask);
                    }
                    task tempTask = new task(tempJob.getJobID(),"assemble",itemName,itemAmount,"workshop");
                    DefinedTasks.add(tempTask);
                    tempTask.setTask(tempJob.getJobID(),"carryToStorage",itemName,itemAmount,"storage");
                    DefinedTasks.add(tempTask);
                }
            }
        }
        
        List<auction> availableMissions = new Vector<>();
        availableMissions = AP.getMissions();
        availableMissions.removeAll(DefinedMissions);
        for(int i=0; i<availableMissions.size();i++)
        {
            auction tempJob = availableMissions.get(i);
            DefinedMissions.add(tempJob);
            for(int j=0; j<tempJob.getAuctionRequireds().size();j++)
            {
                String itemName = tempJob.getAuctionRequireds().get(j).getLeft();
                Integer itemAmount = tempJob.getAuctionRequireds().get(j).getRight();
                storage tempStorage = AP.Storages.get(tempJob.getAuctionStorage());
                item tempItem = AP.ItemsInEnv.get(itemName);
                /*Pair<item,Pair<Integer,storage>> requirement = new Pair(tempItem,new Pair(itemAmount,tempStorage));
                Requirements.add(requirement);*/
                if (tempItem.getSubItems().size() == 0)
                {
                    task tempTask = new task(tempJob.getAuctionID(),"buy",itemName,itemAmount,"shop");
                    DefinedTasks.add(tempTask);
                    tempTask.setTask(tempJob.getAuctionID(), "carryToStorage", itemName, itemAmount, tempStorage.getName());
                    DefinedTasks.add(tempTask);
                }
                else
                {
                    for(int h = 0; h < tempItem.getSubItems().size(); h++)
                    {
                        task tempTask = new task(tempJob.getAuctionID(),"buy",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),"shop");
                        DefinedTasks.add(tempTask);
                        tempTask.setTask(tempJob.getAuctionID(),"carryToWorkshop",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),"workshop");
                        DefinedTasks.add(tempTask);
                    }
                    task tempTask = new task(tempJob.getAuctionID(),"assemble",itemName,itemAmount,"workshop");
                    DefinedTasks.add(tempTask);
                    tempTask.setTask(tempJob.getAuctionID(),"carryToStorage",itemName,itemAmount,"storage");
                    DefinedTasks.add(tempTask);
                }
            }
        }
        /*if(AP.getStep() > 0)
        {
            Pair<item,Pair<Integer,storage>> temprequirement = new Pair(Requirements.get(0).getLeft(),new Pair(Requirements.get(0).getRight().getLeft(),Requirements.get(0).getRight().getRight()));
            System.out.println("item : "+temprequirement.getLeft().getName()+" Amount : "+temprequirement.getRight().getLeft()+" Sotrage : "+temprequirement.getRight().getRight().getName());
        }*/
    }
    private static int intParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return -1;
        Parameter param = params.get(index);
        if(param instanceof Numeral) return ((Numeral) param).getValue().intValue();
        return -1;
    }
}