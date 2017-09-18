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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import massim.javaagents.MailService;
import static massim.javaagents.agents.WarpAgent.stringParam;
import massim.javaagents.percept.AgentPercepts;
import massim.javaagents.percept.Pair;
import massim.javaagents.percept.auction;
import massim.javaagents.percept.chargingStation;
import massim.javaagents.percept.item;
import massim.javaagents.percept.job;
import massim.javaagents.percept.shop;
import massim.javaagents.percept.storage;
import massim.javaagents.percept.subItem;
import massim.javaagents.percept.task;
import massim.javaagents.percept.workshop;

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
    private List<job> DefinedJobs = new Vector<>();
    private List<task> DefinedTasks = new Vector<>();
    private List<task> takenTasks = new Vector<>();
    private List<auction> DefinedMissions = new Vector<>();
    private task myTask;
    private boolean pauseMyTask;
    private boolean hasTask;
    public Map<String, List<String>> Tools = new HashMap<>();
    //List<Pair<item,Pair<Integer,storage>>> Requirements = new Vector<>();
    
    public MiladAgent(String name, MailService mailbox) {
        super(name, mailbox);
        flag = false;
        pauseMyTask = false;
        myTask = new task();
        hasTask = false;
       
//        
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
    private void sayMyTool()
    {
        for(int i=0 ; i<AP.getSelfRole().getTools().size() ; i++)
        {
            broadcast(new Percept("MyTool", new Identifier(AP.getSelfRole().getTools().get(i))), getName());
        }
    }
    
    @Override
    public Action step() {
        System.out.println("Start me : "+getName()+" myTask : "+myTask.getAction()+myTask.getJob());
        //Percept
        makePerceptObjects();
        sayMyTool();
        
            for (String name: Tools.keySet()){
                    
                List<String> value = Tools.get(name);  
                System.out.println("Tools -> "+name);
                for(int j=0; j<value.size() ; j++)  
                {
                    System.out.println("*** "+value.get(j));
                }
            }
        
        /*for(int i=0; i<AP.getItemsInEnv().size();i++)
        {
            System.out.println("**** "+AP.getItemsInEnv().get(i).getName());
            if(AP.getItemsInEnv().get(i).getSubItems().size() > 0 )
            {
                for(int j=0; j< AP.getItemsInEnv().get(i).getSubItems().size() ; j++)
                    System.out.println("--- "+AP.getItemsInEnv().get(i).getSubItems().get(j).getSubItemName());
            }
        }*/
        if(actionQueue.size() > 0) 
        {
            return actionQueue.poll();
        }
        if(hasTask == true)
        {
            if(pauseMyTask == true)
            {
                charge();
            }
            if(pauseMyTask == false)
            {
                doTask();
            }   
        }
        
        if (hasTask == false && AP.getStep()>0)
        {    
                DefineRequirement();
                chooseTask();
        }
        
        System.out.println("End me : "+getName()+" myTask : "+myTask.getAction()+myTask.getJob());
        return actionQueue.peek() != null? actionQueue.poll() : new Action("recharge");
        
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch (message.getName()){
            case "taken":
                jobsTaken.add(stringParam(message.getParameters(), 0));
                break;
            case "taskTaken":
                task tempTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),intParam(message.getParameters(), 3),stringParam(message.getParameters(), 4));
                //System.out.println("TakenTasks Handle Message :  agentName"+getName()+tempTask.getAction()+tempTask.getJob() );
                takenTasks.add(tempTask);
                break;
            case "MyTool":
                if((Tools.containsKey(stringParam(message.getParameters(), 0))) == false)
                {
                    Tools.putIfAbsent(stringParam(message.getParameters(), 0), new ArrayList<String>());
                }
                
                if(Tools.get(stringParam(message.getParameters(), 0)).contains(sender))
                    ;
                else
                    Tools.get(stringParam(message.getParameters(), 0)).add(sender);
                break;
        }
    }
    
    private void DefineRequirement()
    {
        List<job> availableJobs = new Vector<>();
        availableJobs = AP.getJobs();
        
        //*** //availableJobs.removeAll(DefinedJobs);
        for(int i=0; i<DefinedJobs.size();i++)
        {
            job DJob = new job(DefinedJobs.get(i));
            for(int j=0; j<availableJobs.size();j++)
            {   
                job AJob = new job(availableJobs.get(j));
                if(DJob.compareTo(AJob) == true)
                {
                    availableJobs.remove(j);
                    break;
                }
            }
           
        }
        //***
        
        for(int i=0; i<availableJobs.size();i++)
        {
            job tempJob = new job();
            tempJob = availableJobs.get(i);
            DefinedJobs.add(tempJob);
            for(int j=0; j<tempJob.getJobRequireds().size();j++)
            {
                String itemName = tempJob.getJobRequireds().get(j).getLeft();
                Integer itemAmount = tempJob.getJobRequireds().get(j).getRight();
                storage tempStorage = AP.Storages.get(tempJob.getJobStorage());
                item tempItem = AP.ItemsInEnv.get(itemName);
                ///tempItem , itemAmount
                
                // add tasks to list
                if (tempItem.getSubItems().size() == 0)
                {
                    
                    DefinedTasks.add(new task(tempJob.getJobID(),"buy",itemName,itemAmount,findNearestshop(tempStorage.getName(),itemName,itemAmount, false)));
                    DefinedTasks.add(new task(tempJob.getJobID(), "carryToStorage", itemName, itemAmount, tempStorage.getName()));
                     
                }
               /* else //tempItem is a multiItem
                {
                    for(int g=0; g<itemAmount ; g++)
                    {
                        for(int h = 0; h < tempItem.getSubItems().size(); h++)
                        {
                            //System.out.println("^^ TempSubItem"+itemName+" "+tempItem.getSubItems().get(h).getSubItemName()+" "+ tempItem.getSubItems().get(h).getSubItemAmount());
                            subItem tempSubItem = tempItem.getSubItems().get(h);
                            String tempSubItemName = tempSubItem.getSubItemName();
                            int tempSubItemAmount = tempSubItem.getSubItemAmount();
                            item subItem = AP.ItemsInEnv.get(tempSubItemName);
                            if(subItem.getSubItems().size() == 0)
                            {
                                //System.out.println("|| tempSubItem is simple :)");
                                //System.out.println("");
                                DefinedTasks.add(new task(tempJob.getJobID(),"buy",tempSubItemName,tempSubItemAmount,findNearestshop(tempStorage.getName(),tempSubItemName,tempSubItemAmount, true)));
                                DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempSubItemName,tempSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                            }
                            else//tempSubItem is a multiItem
                            {
                                //System.out.println("## tempSubItem is Multi :)");
                                for(int v=0; v<subItem.getSubItems().size() ; v++)
                                {
                                    subItem subSubItem = subItem.getSubItems().get(v);
                                    String subSubItemName = subSubItem.getSubItemName();
                                    int subSubItemAmount = subSubItem.getSubItemAmount();
                                    item tempSubSubItem = AP.ItemsInEnv.get(subSubItemName);
                                    //System.out.println("##^^^ "+subSubItemName+" "+subSubItemAmount);
                                    //maybe subsubitem has subitems 
                                    //simple
                                    if(tempSubSubItem.getSubItems().size() == 0)
                                    {
                                        //System.out.println("##^^^ || single subsubitem");
                                        DefinedTasks.add(new task(tempJob.getJobID(),"buy",subSubItemName,subSubItemAmount,findNearestshop(tempStorage.getName(),subSubItemName,subSubItemAmount, true)));
                                        DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subSubItemName,subSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                                    }
                                    //multi
                                    else
                                    {
                                        //System.out.println("##^^^ ## Multi subsubitem");
                                        for(int k=0 ; k<tempSubSubItem.getSubItems().size();k++)
                                        {
                                            subItem subSubSubItem = tempSubSubItem.getSubItems().get(k);
                                            String subSubSubItemName = subSubSubItem.getSubItemName();
                                            int subSubSubItemAmount = subSubSubItem.getSubItemAmount();
                                            //System.out.println("##^^^ ##^^^ "+subSubSubItemName+" "+subSubSubItemAmount);
                                            DefinedTasks.add(new task(tempJob.getJobID(),"buy",subSubSubItemName,subSubSubItemAmount,findNearestshop(tempStorage.getName(),subSubSubItemName,subSubSubItemAmount, true)));
                                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subSubSubItemName,subSubSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                                        }//tools
                                        for(int k=0 ; k<tempSubSubItem.getTools().size();k++)
                                        {
                                            //System.out.println("##^^^ ##^^^ tempItem subtools :"+tempSubSubItem.getTools().get(k));
                                            //DefinedTasks.add(new task(tempJob.getJobID(),"buy",tempSubSubItem.getTools().get(k),1,findNearestshop(tempStorage.getName(),tempSubSubItem.getTools().get(k),1, true)));
                                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempSubSubItem.getTools().get(k),1,findNearestWorkshop(tempStorage.getName())));
                                        }
                                        DefinedTasks.add(new task(tempJob.getJobID(),"assemble",subSubItem.getSubItemName(),subSubItem.getSubItemAmount(),findNearestWorkshop(tempStorage.getName())));
                                    }
                                    
                            
                                }
                                //tools
                                for(int v=0; v<subItem.getTools().size() ; v++)
                                {
                                    //System.out.println("##^^^ tempItem subtools :"+subItem.getTools().get(v));
                                    //DefinedTasks.add(new task(tempJob.getJobID(),"buy",subItem.getTools().get(v),1,findNearestshop(tempStorage.getName(),subItem.getTools().get(v),1, true)));
                                    DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subItem.getTools().get(v),1,findNearestWorkshop(tempStorage.getName())));
                                }
                                DefinedTasks.add(new task(tempJob.getJobID(),"assemble",tempSubItemName,tempSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                            }
                            
                        }
                        //tools
                        for(int h = 0; h < tempItem.getTools().size(); h++)
                        {
                            //System.out.println("^^ tempItem subtools :"+tempItem.getTools().get(h));
                            //DefinedTasks.add(new task(tempJob.getJobID(),"buyForAssemble",tempItem.getTools().get(h),1,findNearestshop(tempStorage.getName(),tempItem.getTools().get(h),1, true)));
                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempItem.getTools().get(h),1,findNearestWorkshop(tempStorage.getName())));
                        }
                        //???
                        DefinedTasks.add(new task(tempJob.getJobID(),"assemble",tempItem.getName(),itemAmount,findNearestWorkshop(tempStorage.getName())));
                        DefinedTasks.add(new task(tempJob.getJobID(),"carryToStorage",tempItem.getName(),itemAmount,tempStorage.getName()));
                    }
                }*/
            }
        }
        
       List<auction> availableMissions = new Vector<>();
        availableMissions = AP.getMissions();
        
        //*** //availableMissions.removeAll(DefinedMissions);
        for(int i=0; i<DefinedMissions.size();i++)
        {
            auction DJob = new auction(DefinedMissions.get(i));
            for(int j=0; j<availableMissions.size();j++)
            {   
                auction AJob = new auction(availableMissions.get(j));
                if(DJob.compareTo(AJob) == true)
                {
                    availableMissions.remove(j);
                    break;
                }
            }
           
        }
        //***
        /*for(int i=0; i<availableMissions.size();i++)
        {
            auction tempJob = new auction();
            tempJob = availableMissions.get(i);
            
            DefinedMissions.add(tempJob);
            for(int j=0; j<tempJob.getAuctionRequireds().size();j++)
            {
                String itemName = tempJob.getAuctionRequireds().get(j).getLeft();
                Integer itemAmount = tempJob.getAuctionRequireds().get(j).getRight();
                storage tempStorage = AP.Storages.get(tempJob.getAuctionStorage());
                item tempItem = AP.ItemsInEnv.get(itemName);
                ///tempItem , itemAmount
                
                // add tasks to list
                if (tempItem.getSubItems().size() == 0)
                {
                    DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",itemName,itemAmount,findNearestshop(tempStorage.getName(),itemName,itemAmount, false)));
                    DefinedTasks.add(new task(tempJob.getAuctionID(), "carryToStorage", itemName, itemAmount, tempStorage.getName()));
                     
                }
                else //tempItem is a multiItem
                {
                    for(int g=0; g<itemAmount ; g++)
                    {
                        for(int h = 0; h < tempItem.getSubItems().size(); h++)
                        {
                            //System.out.println("^^ TempSubItem"+itemName+" "+tempItem.getSubItems().get(h).getSubItemName()+" "+ tempItem.getSubItems().get(h).getSubItemAmount());
                            subItem tempSubItem = tempItem.getSubItems().get(h);
                            String tempSubItemName = tempSubItem.getSubItemName();
                            int tempSubItemAmount = tempSubItem.getSubItemAmount();
                            item subItem = AP.ItemsInEnv.get(tempSubItemName);
                            if(subItem.getSubItems().size() == 0)
                            {
                               // System.out.println("|| tempSubItem is simple :)");
                               // System.out.println("");
                                DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",tempSubItemName,tempSubItemAmount,findNearestshop(tempStorage.getName(),tempSubItemName,tempSubItemAmount, true)));
                                DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",tempSubItemName,tempSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                            }
                            else
                            {
                                //System.out.println("## tempSubItem is Multi :)");
                                for(int v=0; v<subItem.getSubItems().size() ; v++)
                                {
                                    subItem subSubItem = subItem.getSubItems().get(v);
                                    String subSubItemName = subSubItem.getSubItemName();
                                    int subSubItemAmount = subSubItem.getSubItemAmount();
                                    item tempSubSubItem = AP.ItemsInEnv.get(subSubItemName);
                                    //System.out.println("##^^^ "+subSubItemName+" "+subSubItemAmount);
                                    //maybe subsubitem has subitems 
                                    //simple
                                    if(tempSubSubItem.getSubItems().size() == 0)
                                    {
                                        //System.out.println("##^^^ || single subsubitem");
                                        DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",subSubItemName,subSubItemAmount,findNearestshop(tempStorage.getName(),subSubItemName,subSubItemAmount, true)));
                                        DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",subSubItemName,subSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                                    }
                                    //multi
                                    else
                                    {
                                        //System.out.println("##^^^ ## Multi subsubitem");
                                        for(int k=0 ; k<tempSubSubItem.getSubItems().size();k++)
                                        {
                                            subItem subSubSubItem = tempSubSubItem.getSubItems().get(k);
                                            String subSubSubItemName = subSubSubItem.getSubItemName();
                                            int subSubSubItemAmount = subSubSubItem.getSubItemAmount();
                                           // System.out.println("##^^^ ##^^^ "+subSubSubItemName+" "+subSubSubItemAmount);
                                            DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",subSubSubItemName,subSubSubItemAmount,findNearestshop(tempStorage.getName(),subSubSubItemName,subSubSubItemAmount, true)));
                                            DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",subSubSubItemName,subSubSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                                        }
                                        //tools
                                        for(int k=0 ; k<tempSubSubItem.getTools().size();k++)
                                        {
                                            //System.out.println("##^^^ ##^^^ tempItem subtools :"+tempSubSubItem.getTools().get(k));
                                            //DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",tempSubSubItem.getTools().get(k),1,findNearestshop(tempStorage.getName(),tempSubSubItem.getTools().get(k),1, true)));
                                            DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",tempSubSubItem.getTools().get(k),1,findNearestWorkshop(tempStorage.getName())));
                                        }
                                        DefinedTasks.add(new task(tempJob.getAuctionID(),"assemble",subSubItem.getSubItemName(),subSubItem.getSubItemAmount(),findNearestWorkshop(tempStorage.getName())));
                                    }
                                    
                            
                                }
                                //tools
                                for(int v=0; v<subItem.getTools().size() ; v++)
                                {
                                    //System.out.println("##^^^ tempItem subtools :"+subItem.getTools().get(v));
                                    //DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",subItem.getTools().get(v),1,findNearestshop(tempStorage.getName(),subItem.getTools().get(v),1, true)));
                                    DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",subItem.getTools().get(v),1,findNearestWorkshop(tempStorage.getName())));
                                }
                                DefinedTasks.add(new task(tempJob.getAuctionID(),"assemble",tempSubItemName,tempSubItemAmount,findNearestWorkshop(tempStorage.getName())));
                                
                            }
                            
                        }
                        //tools
                        for(int h = 0; h < tempItem.getTools().size(); h++)
                        {
                            //System.out.println("^^ tempItem subtools :"+tempItem.getTools().get(h));
                            //DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",tempItem.getTools().get(h),1,findNearestshop(tempStorage.getName(),tempItem.getTools().get(h),1, true)));
                            DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",tempItem.getTools().get(h),1,findNearestWorkshop(tempStorage.getName())));
                        }
                        //???
                        DefinedTasks.add(new task(tempJob.getAuctionID(),"assemble",tempItem.getName(),itemAmount,findNearestWorkshop(tempStorage.getName())));
                        DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToStorage",tempItem.getName(),itemAmount,tempStorage.getName()));
                    }
                }
            }
        }
        */
        //for(int i=0; i<DefinedTasks.size();i++)
        //{
            //System.out.println("#1#ABCDE  DefinedRequirementAndTasks.get(i) : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getDestination()+DefinedTasks.get(i).getItem()+DefinedTasks.get(i).getJob()+DefinedTasks.get(i).getAmount());
        //}
    }
    
    private String findNearestWorkshop (String storage)
    {
        double minDistance = Double.MAX_VALUE;
        String workshop = "";
        storage tempStorage = AP.Storages.get(storage);
        for (int i = 0; i < AP.getWorkshops().size(); i++)
        {
            workshop next = AP.getWorkshops().get(i);
            double workshopLat = next.getLat();
            double workshopLon = next.getLon();
            double storageLat = tempStorage.getLat();
            double storageLon = tempStorage.getLon();
            double dworkshop = Math.sqrt((workshopLat-storageLat)*(workshopLat-storageLat) + (workshopLon-storageLon)*(workshopLon-storageLon));
            if (dworkshop < minDistance)
            {
                minDistance = dworkshop;
                workshop = next.getName();
            }
        }
        return workshop;
    }
    
    private String findNearestshop (String storage ,String itemName, int itemAmount, boolean isMultiItem)
    {
        if (isMultiItem)
        {
            String workshop = findNearestWorkshop(storage);
            double minDistance = Double.MAX_VALUE;
            String shop = AP.getShops().get(0).getShopName();
            workshop tempWorkshop = AP.Workshops.get(workshop);
            List<shop> shops = AP.shopsByItem.get(itemName);
            if(shops != null)
            {
                for (int i = 0; i < shops.size(); i++)
                {
                    shop next = shops.get(i);
                    double workshopLat = tempWorkshop.getLat();
                    double workshopLon = tempWorkshop.getLon();
                    double shopLat = next.getShopLat();
                    double shopLon = next.getShopLon();
                    double dshop = Math.sqrt((workshopLat-shopLat)*(workshopLat-shopLat) + (workshopLon-shopLon)*(workshopLon-shopLon));
                    if (dshop < minDistance && next.ShopItemsMap.get(itemName).getAmount() >= itemAmount)
                    {
                        minDistance = dshop;
                        shop = next.getShopName();
                    }
                }
            }
            else
            {
                //if(getName() == "agentA4")
                
                System.out.println("Error !! no shop has this item!" + itemName);
                //System.out.println(AP.shopsByItem.size()+" "+AP.getShops().size());
            }
            return shop;
        }
        else
        {
            double minDistance = Double.MAX_VALUE;
            String shop = "";
            storage tempStorage = AP.Storages.get(storage);
            List<shop> shops = AP.shopsByItem.get(itemName);
            if(shops != null)
            {
                for (int i = 0; i < shops.size(); i++)
                {
                    shop next = shops.get(i);
                    double shopLat = next.getShopLat();
                    double shopLon = next.getShopLon();
                    double storageLat = tempStorage.getLat();
                    double storageLon = tempStorage.getLon();
                    double dshop = Math.sqrt((shopLat-storageLat)*(shopLat-storageLat) + (shopLon-storageLon)*(shopLon-storageLon));
                    if (dshop < minDistance && next.ShopItemsMap.get(itemName).getAmount() >= itemAmount)
                    {
                        minDistance = dshop;
                        shop = next.getShopName();
                    }
                }
            }
            else
            {
                System.out.println("Error !! no shop has this item!");
            }
            return shop;
        }
    }
    
    private static int intParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return -1;
        Parameter param = params.get(index);
        if(param instanceof Numeral) return ((Numeral) param).getValue().intValue();
        return -1;
    }
    private void chooseTask()
    {
        
        //avalable tasks
        for(int i=0; i<takenTasks.size();i++)
        {
            task Ttask = new task(takenTasks.get(i));
            for(int j=0; j<DefinedTasks.size();j++)
            {   
                task Dtask = new task(DefinedTasks.get(j));
                if(Ttask.compareTo(Dtask) == true)
                {
                    DefinedTasks.remove(j);
                    break;
                }
            }
            //System.out.println("#2#TakenTasks : "+takenTasks.get(i).getAction()+takenTasks.get(i).getJob());
        }
        //DefinedTasks.removeAll(takenTasks);
        //for(int i=0; i<DefinedTasks.size();i++)
        //{
            //System.out.println("#3#DefinedTasks : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getJob());
        //}
        //
        task tempTask = new task();
        tempTask = null;
        double dist;
        double minDistance = Double.MAX_VALUE;
        for(int i=0 ; i<DefinedTasks.size() ; i++)
        {
            //System.out.println("choose task -> DefinedTasks.get(i) : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getDestination()+DefinedTasks.get(i).getItem()+DefinedTasks.get(i).getJob()+DefinedTasks.get(i).getAmount());
            switch(DefinedTasks.get(i).getAction())
            {
                
                case "carryToWorkshop":
                   /* if( AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()) 
                      ||
                        AP.getSelfRole().haveTool(DefinedTasks.get(i).getItem())
                       )
                    {
                         tempTask = DefinedTasks.get(i);
                         //System.out.println("initial tempTask");
                    }*/
                    break;
                case "carryToStorage":
                    //System.out.println("#--# carryToStorage case");
                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
                    {
                        //System.out.println("#00# Have item :/)");
                        tempTask = DefinedTasks.get(i);
                       // System.out.println("initial tempTask");
                     //myTask = DefinedTasks.get(i);
                     //takenTasks.add(myTask);
                     //broadcast(new Percept("taskTaken", new Identifier(DefinedTasks.get(i).getJob()),new Identifier(DefinedTasks.get(i).getAction()),new Identifier(DefinedTasks.get(i).getItem()),new Identifier(String.valueOf(DefinedTasks.get(i).getAmount())),new Identifier(DefinedTasks.get(i).getDestination())), getName());
                    }
                    break;
                
                case "assemble":
                    
                    
                    break;
            }
            
        }
        if(tempTask == null || tempTask.getJob() == null)
        {
            for(int i=0 ; i<DefinedTasks.size() ; i++)
            {
                //System.out.println("choose task -> DefinedTasks.get(i) : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getDestination()+DefinedTasks.get(i).getItem()+DefinedTasks.get(i).getJob()+DefinedTasks.get(i).getAmount());
                switch(DefinedTasks.get(i).getAction())
                {
                    case "buy":
        //                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
        //                    {
        //                        //no need to buy and remove this task
        //                        tempTask.setAction("NoAction");
        //                    }
                            dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Shops.get(DefinedTasks.get(i).getDestination()).getShopLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Shops.get(DefinedTasks.get(i).getDestination()).getShopLon(),2) );
                            if(dist < minDistance)
                            {
                                tempTask = DefinedTasks.get(i);
                                minDistance = dist;

                                //System.out.println("initial tempTask");
                            }
                            break;
                }
            }
        }
        
        //System.out.println("ABCDE choose task -> tempTask : "+tempTask.getAction()+tempTask.getDestination()+tempTask.getItem()+tempTask.getJob()+tempTask.getAmount());
            if(tempTask != null && tempTask.getAction() != null )
            {
                
                for(int i=0; i<takenTasks.size();i++)
                {
                    task Ttask = new task(takenTasks.get(i));
                    
                        if(Ttask.compareTo(tempTask) == true)
                        {
                            System.out.println("#!!# You cannot choose any task"+tempTask.getAction()+tempTask.getJob());
                            return;
                        }
                }
                /*if(takenTasks.contains(tempTask) == false)
                {*/
                    myTask = tempTask;
                    takenTasks.add(myTask);
                    hasTask = true;
                    System.out.println("#4#ABCDE choose task -> myTask : "+myTask.getAction()+tempTask.getDestination()+myTask.getItem()+myTask.getJob()+myTask.getAmount());
                    broadcast(new Percept("taskTaken", new Identifier(myTask.getJob()),new Identifier(myTask.getAction()),new Identifier(myTask.getItem()),new Identifier(String.valueOf(myTask.getAmount())),new Identifier(myTask.getDestination())), getName());
                /*}*/
            }
          
    }
    
    private void doTask()
    {
        
        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11ABCDE DoTask myTask : "+myTask.getAction());
        switch(myTask.getAction())
        {
            case "buy":
                buy();
                break;
            case "carryToWorkshop":
                carryToWorkshop();
                break;
            case "carryToStorage":
                carryToStorage();
                break;
            case "assemble":
                assemble();
              break;  
        }
        //check charge
        if( checkCharge() == false )
        {
            //do charge
            actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
            pauseMyTask =true;
            return;
            
        }
    }
    private boolean checkCharge()
    {
        double dist = 10;
        /*switch(myTask.getAction())
        {
            case "buy":
                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Shops.get(myTask.getDestination()).getShopLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Shops.get(myTask.getDestination()).getShopLon(),2) );
                break;
            case "assemble":
                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Workshops.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Workshops.get(myTask.getDestination()).getLon(),2) );
                break;
            case "carryToWorkshop":
                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Workshops.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Workshops.get(myTask.getDestination()).getLon(),2) );
                break;
            case "carryToStorage":
                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Storages.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Storages.get(myTask.getDestination()).getLon(),2) );
                break;
        }
        */
        Pair<String,Double> chargingStationInfo = findNearestChargeStation();
        dist = chargingStationInfo.getRight();
        //System.out.println("--->> dist : "+dist);
        int currentCharge = AP.getSelfInfo().getCharge();
        //System.out.println("--->> currentCharge : "+currentCharge);
        int expectedCharge = (int) ((dist/(AP.getSelfRole().getSpeed()))*10);
        //System.out.println("currentCharge : "+currentCharge+"expectedCharge : "+expectedCharge);
        int chargeTH = 10;
        //if(currentCharge - expectedCharge < chargeTH)
        if(currentCharge <50)
        {
            //pause current task and go to charge station
            return false;
        }
        else
        {
           //do task
            return true;
        }
                
        
    }
     private Pair<String,Double> findNearestChargeStation ()
    {
        double minDistance = Double.MAX_VALUE;
        String chargeStation = "";
        
        for (int i = 0; i < AP.getChargingStations().size(); i++)
        {
            chargingStation next = AP.getChargingStations().get(i);
            double stationLat = next.getLat();
            double stationLon = next.getLon();
            double myLat = AP.getSelfInfo().getLat();
            double myLon = AP.getSelfInfo().getLon();
            double dstation = Math.sqrt((stationLat-myLat)*(stationLat-myLat) + (stationLon-myLon)*(stationLon-myLon));
            if (dstation < minDistance)
            {
                minDistance = dstation;
                chargeStation = next.getName();
            }
        }
        return new Pair<String,Double> (chargeStation,minDistance);
    }
     
     private void charge()
     {
         if( (AP.getSelfInfo().getLastAction().compareTo("charge") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
         {
             if(AP.getSelfInfo().getCharge() > AP.getSelfRole().getBattery()-5)
             {
                pauseMyTask = false;
                actionQueue.clear();
             }
             else
             {
                 actionQueue.add(new Action("charge"));
             }
             return;
         }
         int routeLength = AP.getRouteLength();
         if (AP.getRoutes().size() >1 && routeLength>1)
         {
             if(AP.getSelfInfo().getCharge() < 20)
                actionQueue.add(new Action("recharge"));
             else
                actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
         }
         else
         {
             actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
             actionQueue.add(new Action("charge"));
         }
         
     }
     private void buy()
     {
         if( (AP.getSelfInfo().getLastAction().compareTo("buy") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
         {
            //task is done!
             hasTask = false;
             //myTask = null;
             actionQueue.clear();
             return;
         }
         if( (AP.getSelfInfo().getLastAction().compareTo("buy") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("failed_item_amount") == 0) )
         {
            actionQueue.add(new Action("buy", new Identifier(myTask.getItem()), new Numeral(myTask.getAmount())));
             return;
         }
         int routeLength = AP.getRouteLength();
         if (AP.getRoutes().size() >1 && routeLength>1)
         {
            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
         }
         else
         {
             actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
             actionQueue.add(new Action("buy", new Identifier(myTask.getItem()), new Numeral(myTask.getAmount())));
         }
         
     }
     private void carryToStorage()
     {
         if( (AP.getSelfInfo().getLastAction().compareTo("deliver_job") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
         {
            //task is done!
             //myTask = null;
             hasTask = false;
             actionQueue.clear();
             return;
         }
         int routeLength = AP.getRouteLength();
         if (AP.getRoutes().size() >1 && routeLength>1)
         {
            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
         }
         else
         {
             actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
             actionQueue.add(new Action("deliver_job", new Identifier(myTask.getJob())));
         }
     }
     private void carryToWorkshop()
     {
         System.out.println("Do carry to workshop");
         if( AP.getSelfInfo().getLat() == AP.Workshops.get(myTask.getDestination()).getLat() 
            &&
             AP.getSelfInfo().getLon() == AP.Workshops.get(myTask.getDestination()).getLon()     
           )
         {
             //arrive to workshop
             System.out.println("--- arrive to workshop ---");
             hasTask = false;
             actionQueue.clear();
             return;
         }
        // int routeLength = AP.getRouteLength();
         //if (AP.getRoutes().size() >1 && routeLength>1)
        // {
            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
        // }
        // else
         //{
         //    actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
             //myTask = null;
             //hasTask = false;
        //}
     }
     private void assemble()
     {
          actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
          actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
          //myTask = null;
          hasTask = false;
     }
     public static String stringParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return "";
        Parameter param = params.get(index);
        if(param instanceof Identifier) return ((Identifier) param).getValue();
        return "";
    }
}
