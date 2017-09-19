/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.agents;

import com.sun.javafx.scene.control.skin.VirtualFlow;
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
    private List <task> assembleComplete = new Vector<>();
    private boolean hasJob;
    private int counter;
    public Map<String, List<task>> DoingAssembles = new HashMap<>();
    private Map<String,List<String>> agentsDoingAssemble = new HashMap<>();
            
    //List<Pair<item,Pair<Integer,storage>>> Requirements = new Vector<>();
    
    public MiladAgent(String name, MailService mailbox) {
        super(name, mailbox);
        flag = false;
        pauseMyTask = false;
        myTask = new task();
        hasTask = false;
        hasJob = false;
        counter = 0;
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
            //counter++;
                //if(counter%3 == 0)
            if(counter == 0)
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
                task tempTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),stringParam(message.getParameters(), 3),Integer.parseInt(stringParam(message.getParameters(), 4)),stringParam(message.getParameters(), 5));
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
            case "AssembleTask":
                task assembleTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),stringParam(message.getParameters(), 3),intParam(message.getParameters(), 4),stringParam(message.getParameters(), 5));
                assembleComplete.add(assembleTask);
                List<task> l = DoingAssembles.get(assembleTask.getDestination());
                for(int j=0; j<l.size(); j++)
                {
                    if(l.get(j).compareTo(assembleTask) == true)
                    {
                        DoingAssembles.get(assembleTask.getDestination()).remove(j);
                        agentsDoingAssemble.get(assembleTask.getDestination()).remove(sender);
                    }
                }
                counter = 0;
                break;
            case "IdoAssemble":
                task DoingassembleTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),stringParam(message.getParameters(), 3),intParam(message.getParameters(), 4),stringParam(message.getParameters(), 5));    
                if(DoingAssembles.containsKey(DoingassembleTask.getDestination()) == false)
                {
                    DoingAssembles.putIfAbsent(DoingassembleTask.getDestination(), new ArrayList<task>());
                }
                if(DoingAssembles.containsKey(DoingassembleTask.getDestination()) == true)
                {
                    DoingAssembles.get(DoingassembleTask.getDestination()).add(DoingassembleTask);
                }
                if(agentsDoingAssemble.containsKey(DoingassembleTask.getDestination()) == false)
                {
                    agentsDoingAssemble.putIfAbsent(DoingassembleTask.getDestination(), new ArrayList<String>());
                }
                if(agentsDoingAssemble.containsKey(DoingassembleTask.getDestination()) == true)
                {
                    agentsDoingAssemble.get(DoingassembleTask.getDestination()).add(sender);
                }
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
            //tempJob = availableJobs.get(0);
            DefinedJobs.add(tempJob);
            for(int j=0; j<tempJob.getJobRequireds().size();j++)
            {
                String itemName = tempJob.getJobRequireds().get(j).getLeft();
                Integer itemAmount = tempJob.getJobRequireds().get(j).getRight();
                storage tempStorage = AP.Storages.get(tempJob.getJobStorage());
                item tempItem = AP.ItemsInEnv.get(itemName);
                String WorkShop = findNearestWorkshop(tempStorage.getName());
                ///tempItem , itemAmount
                
                // add tasks to list
                if (tempItem.getSubItems().size() == 0)
                {
                    
                    DefinedTasks.add(new task(tempJob.getJobID(),"buy",itemName,itemName,itemAmount,findNearestshop(tempStorage.getName(),itemName,itemAmount, false)));
                    DefinedTasks.add(new task(tempJob.getJobID(), "carryToStorage", itemName,itemName, itemAmount, tempStorage.getName()));
                     
                }
                else //tempItem is a multiItem
                {
                    counter = 1;
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
                                DefinedTasks.add(new task(tempJob.getJobID(),"buy",tempSubItemName,itemName ,tempSubItemAmount,findNearestshop(tempStorage.getName(),tempSubItemName,tempSubItemAmount, true)));
                                DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempSubItemName,itemName,tempSubItemAmount,findNearestWorkshop(tempStorage.getName())));
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
                                        DefinedTasks.add(new task(tempJob.getJobID(),"buy",subSubItemName,tempSubItemName,subSubItemAmount,findNearestshop(tempStorage.getName(),subSubItemName,subSubItemAmount, true)));
                                        DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subSubItemName,tempSubItemName,subSubItemAmount,WorkShop));
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
                                            DefinedTasks.add(new task(tempJob.getJobID(),"buy",subSubSubItemName,subSubItemName,subSubSubItemAmount,findNearestshop(tempStorage.getName(),subSubSubItemName,subSubSubItemAmount, true)));
                                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subSubSubItemName,subSubItemName,subSubSubItemAmount,WorkShop));
                                        }//tools
                                        for(int k=0 ; k<tempSubSubItem.getTools().size();k++)
                                        {
                                            //System.out.println("##^^^ ##^^^ tempItem subtools :"+tempSubSubItem.getTools().get(k));
                                            //DefinedTasks.add(new task(tempJob.getJobID(),"buy",tempSubSubItem.getTools().get(k),1,findNearestshop(tempStorage.getName(),tempSubSubItem.getTools().get(k),1, true)));
                                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempSubSubItem.getTools().get(k),subSubItemName,1,WorkShop));
                                        }
                                        DefinedTasks.add(new task(tempJob.getJobID(),"assemble",subSubItemName,tempSubItemName,subSubItem.getSubItemAmount(),WorkShop));
                                    }
                                    
                            
                                }
                                //tools
                                for(int v=0; v<subItem.getTools().size() ; v++)
                                {
                                    //System.out.println("##^^^ tempItem subtools :"+subItem.getTools().get(v));
                                    //DefinedTasks.add(new task(tempJob.getJobID(),"buy",subItem.getTools().get(v),1,findNearestshop(tempStorage.getName(),subItem.getTools().get(v),1, true)));
                                    DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",subItem.getTools().get(v),tempSubItemName,1,WorkShop));
                                }
                                DefinedTasks.add(new task(tempJob.getJobID(),"assemble",tempSubItemName,itemName,tempSubItemAmount,WorkShop));
                            }
                            
                        }
                        //tools
                        for(int h = 0; h < tempItem.getTools().size(); h++)
                        {
                            //System.out.println("^^ tempItem subtools :"+tempItem.getTools().get(h));
                            //DefinedTasks.add(new task(tempJob.getJobID(),"buyForAssemble",tempItem.getTools().get(h),1,findNearestshop(tempStorage.getName(),tempItem.getTools().get(h),1, true)));
                            DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempItem.getTools().get(h),itemName,1,WorkShop));
                        }
                        //???
                        DefinedTasks.add(new task(tempJob.getJobID(),"assemble",itemName,itemName,itemAmount,WorkShop));
                        DefinedTasks.add(new task(tempJob.getJobID(),"carryToStorage",itemName,itemName,itemAmount,tempStorage.getName()));
                    }
                }
                if(counter == 1)
                    break;
            }
        }
        /*
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
        for(int i=0; i<availableMissions.size();i++)
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
        //List of tasks which I can do
        List<task> canDo = new Vector<>();
        //avalable tasks
        for(int i=0; i<takenTasks.size();i++)
        {
            task Ttask = new task(takenTasks.get(i));
            for(int j=0; j<DefinedTasks.size();j++)
            {   
                task Dtask = new task(DefinedTasks.get(j));
                if(Ttask.compareTo(Dtask) == true)
                {
                    //System.out.println("Similar");
                    DefinedTasks.remove(j);
                    break;
                }
            }
            //System.out.println("#2#TakenTasks : "+takenTasks.get(i).getAction()+takenTasks.get(i).getJob());
        }
        
        for(int i=0; i<DefinedTasks.size();i++)
        {
           ;//System.out.println("#3#DefinedTasks : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getJob());
        }
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
                    if( AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()) 
                      ||
                        AP.getSelfRole().haveTool(DefinedTasks.get(i).getItem())
                       )
                    {
                         //tempTask = DefinedTasks.get(i);
                        canDo.add(DefinedTasks.get(i));
                         //System.out.println("initial tempTask");
                    }
                    break;
                case "carryToStorage":
                    //System.out.println("#--# carryToStorage case");
                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
                    {
                        //System.out.println("#00# Have item :/)");
                        //tempTask = DefinedTasks.get(i);
                        canDo.add(DefinedTasks.get(i));
                       // System.out.println("initial tempTask");
                     //myTask = DefinedTasks.get(i);
                     //takenTasks.add(myTask);
                     //broadcast(new Percept("taskTaken", new Identifier(DefinedTasks.get(i).getJob()),new Identifier(DefinedTasks.get(i).getAction()),new Identifier(DefinedTasks.get(i).getItem()),new Identifier(String.valueOf(DefinedTasks.get(i).getAmount())),new Identifier(DefinedTasks.get(i).getDestination())), getName());
                    }
                    break;
                
                case "assemble":
                    if( AP.getSelfInfo().getLat() == AP.Workshops.get(DefinedTasks.get(i).getDestination()).getLat()
                       &&
                        AP.getSelfInfo().getLon() == AP.Workshops.get(DefinedTasks.get(i).getDestination()).getLon()
                      )
                    {
                        canDo.add(DefinedTasks.get(i));
                        //tempTask = DefinedTasks.get(i);
                    }
                    
                    break;
                case "buy":
                    canDo.add(DefinedTasks.get(i));
                    break;
            }
            
        }
        //***
        Map <String,List<task>>  currentTasks = new HashMap<>();
        List<task> assembleTasks = new Vector<>();
        List<task> carryToStorageTasks = new Vector<>();
        List<task> CarryToWorkshopTasks = new Vector<>();
        List<task> buyTasks = new Vector<>();
        for(int i=0; i<canDo.size() ; i++)
        {
            task t = new task();
            t = canDo.get(i);
            System.out.println("canDo : "+t.getJob()+t.getAction()+t.getItem());
            if((currentTasks.containsKey(t.getAction())) == false)
            {
          //      System.out.println("First of add action "+ t.getAction());
                    currentTasks.putIfAbsent(t.getAction(), new ArrayList<task>());
            }
            if((currentTasks.containsKey(t.getAction())) == true )
            {
            //    System.out.println("Add action "+t.getAction());
                currentTasks.get(t.getAction()).add(t);
                if(t.getAction() == "assemble")
                {
                    assembleTasks.add(t);
                }
                else if(t.getAction() == "carryToStorage")
                {
                    carryToStorageTasks.add(t);
                }
                else if(t.getAction() == "carryToWorkshop")
                {
                    CarryToWorkshopTasks.add(t);
                }
                else if(t.getAction() == "buy")
                {
                    buyTasks.add(t);
                }
            }
        }
        
               for(int i=0; i<assembleTasks.size() ; i++)
                   System.out.println("*1* assembleTasks : "+assembleTasks.get(i).getJob()+assembleTasks.get(i).getAction()+assembleTasks.get(i).getItem());
        
                for(int i=0; i<carryToStorageTasks.size() ; i++)
                   System.out.println("*2* carryToStorage : "+carryToStorageTasks.get(i).getJob()+carryToStorageTasks.get(i).getAction()+carryToStorageTasks.get(i).getItem());
        
                 for(int i=0; i<CarryToWorkshopTasks.size() ; i++)
                   System.out.println("*3* CarryToWorkshop : "+CarryToWorkshopTasks.get(i).getJob()+CarryToWorkshopTasks.get(i).getAction()+CarryToWorkshopTasks.get(i).getItem());
        
            for(int i=0; i<buyTasks.size() ; i++)
                   System.out.println("*4*buy : "+buyTasks.get(i).getJob()+buyTasks.get(i).getAction()+buyTasks.get(i).getItem());
        
        //***
        boolean chTask = false;
        if(assembleTasks != null && assembleTasks.size() > 0)
        {
            tempTask = assembleTasks.get(0);
            broadcast(new Percept("IdoAssemble", new Identifier(myTask.getJob()),new Identifier(myTask.getAction()),new Identifier(myTask.getItem()),new Identifier(myTask.getTopItem()),new Identifier(String.valueOf(myTask.getAmount())),new Identifier(myTask.getDestination())), getName());
            chTask = true;
        }
        else if(carryToStorageTasks != null && carryToStorageTasks.size() > 0)
        {
            for(int i=0 ; i<carryToStorageTasks.size() ; i++)
            {
                if(AP.Jobs.get(carryToStorageTasks.get(i).getJob()).isIsSimple() == true)
                {
                    tempTask = carryToStorageTasks.get(i);
                    chTask = true;
                    break;
                }
            }
        }
        if(chTask == false)
        {
            if(CarryToWorkshopTasks !=null && CarryToWorkshopTasks.size() > 0)
            {
                tempTask = CarryToWorkshopTasks.get(0);
                chTask = true;
            }
            else if(carryToStorageTasks != null && carryToStorageTasks.size() > 0)
            {
                tempTask = carryToStorageTasks.get(0);
                chTask = true;
            }
            else
            {
                if(buyTasks != null)
                {
                for(int i=0; i<buyTasks.size() ; i++)
                {
                     if(AP.Jobs.get(buyTasks.get(i).getJob()) != null && AP.Jobs.get(buyTasks.get(i).getJob()).isIsSimple() == true)
                     {
                        tempTask = buyTasks.get(i);
                        chTask = true;
                        break;
                     }
                }
                }
            }
        }
        if(chTask == false)
        {
            if(buyTasks != null && buyTasks.size() > 0)
            {
                for(int i=0 ; i<buyTasks.size() ; i++)
                {
                    dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Shops.get(buyTasks.get(i).getDestination()).getShopLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Shops.get(buyTasks.get(i).getDestination()).getShopLon(),2) );
                            if(dist < minDistance)
                            {
                                tempTask = buyTasks.get(i);
                                minDistance = dist;

                                //System.out.println("initial tempTask");
                            }
                }
            }
            else
            {
                //there is no task to do
            }
        }
        /*
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
        */
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
                    System.out.println(String.valueOf(myTask.getAmount()));
                    System.out.println("#4#ABCDE choose task -> myTask : "+myTask.getAction()+tempTask.getDestination()+myTask.getItem()+myTask.getJob()+myTask.getAmount());
                    broadcast(new Percept("taskTaken", new Identifier(myTask.getJob()),new Identifier(myTask.getAction()),new Identifier(myTask.getItem()),new Identifier(myTask.getTopItem()),new Identifier(String.valueOf(myTask.getAmount())),new Identifier(myTask.getDestination())), getName());
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
             //waiting test
             List<task> doingAssembleTasks = new Vector<>();
             doingAssembleTasks = DoingAssembles.get(myTask.getDestination());
             if(doingAssembleTasks != null && doingAssembleTasks.size() > 0)
             {
                 for(int i=0 ; i<agentsDoingAssemble.get(myTask.getDestination()).size() ; i++)
                 {
                     actionQueue.add(new Action("assist_assemble", new Identifier(agentsDoingAssemble.get(myTask.getDestination()).get(i))));
                 }
             }
             else
             {
                hasTask = false;
                actionQueue.clear();
             }
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
          if(AP.getSelfInfo().haveItem(myTask.getItem(), myTask.getAmount()) || assembleComplete.contains(myTask.getJob()) == true)
          {
           hasTask = false;  
           actionQueue.clear();
           broadcast(new Percept("AssembleTask", new Identifier(myTask.getJob()),new Identifier(myTask.getAction()),new Identifier(myTask.getItem()),new Identifier(myTask.getTopItem()),new Identifier(String.valueOf(myTask.getAmount())),new Identifier(myTask.getDestination())), getName());
           return;
          }
          
          actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
//        List <String> inWorkshop = new Vector <>();
//        for (int i = 1 ; i < 29 ; i++)
//        {
//            String agentName = "agentA" + i ;
//            System.out.println("ABCDEF "+agentName);
//            if (AP.Entities.get(agentName).getLat() == AP.getSelfInfo().getLat() && AP.Entities.get(agentName).getLon() == AP.getSelfInfo().getLon() && agentName != AP.getSelfInfo().getName())
//            {
//                System.out.println(agentName +"IS in Workshop");
//                inWorkshop.add(agentName);
//                actionQueue.add(new Action("assist_assemble", new Identifier(agentName)));
//            }
//        }
          //actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
          //myTask = null;
          
     }
     public static String stringParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return "";
        Parameter param = params.get(index);
        if(param instanceof Identifier) return ((Identifier) param).getValue();
        return "";
    }
     private void chooseTask1()
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
        for(int i=0; i<DefinedTasks.size();i++)
        {
            System.out.println("#3#DefinedTasks : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getJob());
        }
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
                    if( AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()) 
                      ||
                        AP.getSelfRole().haveTool(DefinedTasks.get(i).getItem())
                       )
                    {
                         tempTask = DefinedTasks.get(i);
                         //System.out.println("initial tempTask");
                    }
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
                    if( AP.getSelfInfo().getLat() == AP.Workshops.get(DefinedTasks.get(i).getDestination()).getLat()
                       &&
                        AP.getSelfInfo().getLon() == AP.Workshops.get(DefinedTasks.get(i).getDestination()).getLon()
                      )
                    {
                        tempTask = DefinedTasks.get(i);
                    }
                    break;
                 
            }
            
        }
        /*
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
        }*/
        
        System.out.println("ABCDE choose task -> tempTask : "+tempTask.getAction()+tempTask.getDestination()+tempTask.getItem()+tempTask.getJob()+tempTask.getAmount());
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
}
