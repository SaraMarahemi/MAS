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
import massim.javaagents.percept.Pair;
import massim.javaagents.percept.auction;
import massim.javaagents.percept.chargingStation;
import massim.javaagents.percept.item;
import massim.javaagents.percept.job;
import massim.javaagents.percept.shop;
import massim.javaagents.percept.storage;
import massim.javaagents.percept.task;
import massim.javaagents.percept.workshop;

/**
 *
 * @author Sarah
 */
public class PeymanAgent extends Agent{
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
    //List<Pair<item,Pair<Integer,storage>>> Requirements = new Vector<>();
    
    public PeymanAgent(String name, MailService mailbox) {
        super(name, mailbox);
        flag = false;
        pauseMyTask = false;
        myTask = new task();
        hasTask = false;
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
        System.out.println("Start me : "+getName());
        //Percept
        makePerceptObjects();
        //Check AP
        
        
        if(actionQueue.size() > 0) 
        {
            return actionQueue.poll();
        }
        
        System.out.println("End me : "+getName());
        return actionQueue.peek() != null? actionQueue.poll() : new Action("recharge");
        
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        switch (message.getName()){
            case "taken":
                jobsTaken.add(stringParam(message.getParameters(), 0));
                break;
            case "taskTaken":
               // task tempTask = new task(stringParam(message.getParameters(), 0),stringParam(message.getParameters(), 1),stringParam(message.getParameters(), 2),intParam(message.getParameters(), 3),stringParam(message.getParameters(), 4));
//                System.out.println("TakenTasks Handle Message :  agentName"+getName()+tempTask.getAction()+tempTask.getDestination() );
 //               takenTasks.add(tempTask);
                break;
        }
    }
//    
//    private void DefineRequirement()
//    {
//        List<job> availableJobs = new Vector<>();
//        availableJobs = AP.getJobs();
//        //System.out.println("AvailableJobs : "+availableJobs.size());
//        availableJobs.removeAll(DefinedJobs);
//        for(int i=0; i<availableJobs.size();i++)
//        {
//            job tempJob = new job();
//            tempJob = availableJobs.get(i);
//            DefinedJobs.add(tempJob);
//            for(int j=0; j<tempJob.getJobRequireds().size();j++)
//            {
//                String itemName = tempJob.getJobRequireds().get(j).getLeft();
//                Integer itemAmount = tempJob.getJobRequireds().get(j).getRight();
//                storage tempStorage = AP.Storages.get(tempJob.getJobStorage());
//                item tempItem = AP.ItemsInEnv.get(itemName);
//                /*Pair<item,Pair<Integer,storage>> requirement = new Pair(tempItem,new Pair(itemAmount,tempStorage));
//                Requirements.add(requirement);*/
//                // add tasks to list
//                if (tempItem.getSubItems().size() == 0)
//                {
//                    DefinedTasks.add(new task(tempJob.getJobID(),"buy",itemName,itemAmount,findNearestshop(tempStorage.getName(), false)));
//                    DefinedTasks.add(new task(tempJob.getJobID(), "carryToStorage", itemName, itemAmount, tempStorage.getName()));
//                     
//                }
//                else
//                {
//                    for(int h = 0; h < tempItem.getSubItems().size(); h++)
//                    {
//                        DefinedTasks.add(new task(tempJob.getJobID(),"buy",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),findNearestshop(tempStorage.getName(), true)));
//                        DefinedTasks.add(new task(tempJob.getJobID(),"carryToWorkshop",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),findNearestWorkshop(tempStorage.getName())));
//                         
//                    }
//                    DefinedTasks.add(new task(tempJob.getJobID(),"assemble",itemName,itemAmount,findNearestWorkshop(tempStorage.getName())));
//                    DefinedTasks.add(new task(tempJob.getJobID(),"carryToStorage",itemName,itemAmount,tempStorage.getName()));
//                }
//            }
//        }
//        
//        List<auction> availableMissions = new Vector<>();
//        availableMissions = AP.getMissions();
//        availableMissions.removeAll(DefinedMissions);
//        for(int i=0; i<availableMissions.size();i++)
//        {
//            auction tempJob = new auction();
//            tempJob = availableMissions.get(i);
//            
//            DefinedMissions.add(tempJob);
//            for(int j=0; j<tempJob.getAuctionRequireds().size();j++)
//            {
//                String itemName = tempJob.getAuctionRequireds().get(j).getLeft();
//                Integer itemAmount = tempJob.getAuctionRequireds().get(j).getRight();
//                storage tempStorage = AP.Storages.get(tempJob.getAuctionStorage());
//                item tempItem = AP.ItemsInEnv.get(itemName);
//                /*Pair<item,Pair<Integer,storage>> requirement = new Pair(tempItem,new Pair(itemAmount,tempStorage));
//                Requirements.add(requirement);*/
//                if (tempItem.getSubItems().size() == 0)
//                {
//                    DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",itemName,itemAmount,findNearestshop(tempStorage.getName(), false)));
//                    DefinedTasks.add(new task(tempJob.getAuctionID(), "carryToStorage", itemName, itemAmount, tempStorage.getName()));
//                }
//                else
//                {
//                    for(int h = 0; h < tempItem.getSubItems().size(); h++)
//                    {
//                        DefinedTasks.add(new task(tempJob.getAuctionID(),"buy",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),findNearestshop(tempStorage.getName(), true)));
//                        DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToWorkshop",tempItem.getSubItems().get(h).getSubItemName(),tempItem.getSubItems().get(h).getSubItemAmount(),findNearestWorkshop(tempStorage.getName())));
//                    }
//                    
//                    DefinedTasks.add(new task(tempJob.getAuctionID(),"assemble",itemName,itemAmount,findNearestWorkshop(tempStorage.getName())));
//                    DefinedTasks.add(new task(tempJob.getAuctionID(),"carryToStorage",itemName,itemAmount,tempStorage.getName()));
//                }
//            }
//        }
//        for(int i=0; i<DefinedTasks.size();i++)
//        {
//            System.out.println("ABCDE  DefinedRequirementAndTasks.get(i) : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getDestination()+DefinedTasks.get(i).getItem()+DefinedTasks.get(i).getJob()+DefinedTasks.get(i).getAmount());
//        }
//    }
//    
//    private String findNearestWorkshop (String storage)
//    {
//        double minDistance = Double.MAX_VALUE;
//        String workshop = "";
//        storage tempStorage = AP.Storages.get(storage);
//        for (int i = 0; i < AP.getWorkshops().size(); i++)
//        {
//            workshop next = AP.getWorkshops().get(i);
//            double workshopLat = next.getLat();
//            double workshopLon = next.getLon();
//            double storageLat = tempStorage.getLat();
//            double storageLon = tempStorage.getLon();
//            double dworkshop = Math.sqrt((workshopLat-storageLat)*(workshopLat-storageLat) + (workshopLon-storageLon)*(workshopLon-storageLon));
//            if (dworkshop < minDistance)
//            {
//                minDistance = dworkshop;
//                workshop = next.getName();
//            }
//        }
//        return workshop;
//    }
//    
//    private String findNearestshop (String storage , boolean isMultiItem)
//    {
//        if (isMultiItem)
//        {
//            String workshop = findNearestWorkshop(storage);
//            double minDistance = Double.MAX_VALUE;
//            String shop = "";
//            workshop tempWorkshop = AP.Workshops.get(workshop);
//            for (int i = 0; i < AP.getShops().size(); i++)
//            {
//                shop next = AP.getShops().get(i);
//                double workshopLat = tempWorkshop.getLat();
//                double workshopLon = tempWorkshop.getLon();
//                double shopLat = next.getShopLat();
//                double shopLon = next.getShopLon();
//                double dshop = Math.sqrt((workshopLat-shopLat)*(workshopLat-shopLat) + (workshopLon-shopLon)*(workshopLon-shopLon));
//                if (dshop < minDistance)
//                {
//                    minDistance = dshop;
//                    shop = next.getShopName();
//                }
//            }
//            return shop;
//        }
//        else
//        {
//            double minDistance = Double.MAX_VALUE;
//            String shop = "";
//            storage tempStorage = AP.Storages.get(storage);
//            for (int i = 0; i < AP.getShops().size(); i++)
//            {
//                shop next = AP.getShops().get(i);
//                double shopLat = next.getShopLat();
//                double shopLon = next.getShopLon();
//                double storageLat = tempStorage.getLat();
//                double storageLon = tempStorage.getLon();
//                double dshop = Math.sqrt((shopLat-storageLat)*(shopLat-storageLat) + (shopLon-storageLon)*(shopLon-storageLon));
//                if (dshop < minDistance)
//                {
//                    minDistance = dshop;
//                    shop = next.getShopName();
//                }
//            }
//            return shop;
//        }
//    }
//    
//    private static int intParam(List<Parameter> params, int index){
//        if(params.size() < index + 1) return -1;
//        Parameter param = params.get(index);
//        if(param instanceof Numeral) return ((Numeral) param).getValue().intValue();
//        return -1;
//    }
//    private void chooseTask()
//    {
//        
//        //avalable tasks
//        for(int i=0; i<takenTasks.size();i++)
//        {
//            task Ttask = new task(takenTasks.get(i));
//            for(int j=0; j<DefinedTasks.size();j++)
//            {   
//                task Dtask = new task(DefinedTasks.get(j));
//                if(Ttask.compareTo(Dtask) == true)
//                {
//                    DefinedTasks.remove(j);
//                    break;
//                }
//            }
//            //System.out.println("TakenTasks : "+takenTasks.get(i).getAction()+takenTasks.get(i).getJob());
//        }
//        //DefinedTasks.removeAll(takenTasks);
//        for(int i=0; i<DefinedTasks.size();i++)
//        {
//            System.out.println("DefinedTasks : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getJob());
//        }
//        //
//        task tempTask = new task();
//        double dist;
//        double minDistance = Double.MAX_VALUE;
//        for(int i=0 ; i<DefinedTasks.size() ; i++)
//        {
//            //System.out.println("choose task -> DefinedTasks.get(i) : "+DefinedTasks.get(i).getAction()+DefinedTasks.get(i).getDestination()+DefinedTasks.get(i).getItem()+DefinedTasks.get(i).getJob()+DefinedTasks.get(i).getAmount());
//            switch(DefinedTasks.get(i).getAction())
//            {
//                
//                case "carryToWorkshop":
//                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
//                    {
//                         tempTask = DefinedTasks.get(i);
//                         //System.out.println("initial tempTask");
//                    }
//                    break;
//                case "carryToStorage":
//                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
//                    {
//                        tempTask = DefinedTasks.get(i);
//                       // System.out.println("initial tempTask");
//                     //myTask = DefinedTasks.get(i);
//                     //takenTasks.add(myTask);
//                     //broadcast(new Percept("taskTaken", new Identifier(DefinedTasks.get(i).getJob()),new Identifier(DefinedTasks.get(i).getAction()),new Identifier(DefinedTasks.get(i).getItem()),new Identifier(String.valueOf(DefinedTasks.get(i).getAmount())),new Identifier(DefinedTasks.get(i).getDestination())), getName());
//                    }
//                    break;
//                case "assemble":
//                    
//                    break;
//            }
//            
//        }
//        //if(tempTask == null)
//        //{
//            for(int i=0 ; i<DefinedTasks.size() ; i++)
//            {
//        
//                switch(DefinedTasks.get(i).getAction())
//                {
//                    case "buy":
//    //                    if(AP.getSelfInfo().haveItem(DefinedTasks.get(i).getItem(), DefinedTasks.get(i).getAmount()))
//    //                    {
//    //                        //no need to buy and remove this task
//    //                        tempTask.setAction("NoAction");
//    //                    }
//                        dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Shops.get(DefinedTasks.get(i).getDestination()).getShopLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Shops.get(DefinedTasks.get(i).getDestination()).getShopLon(),2) );
//                        if(dist < minDistance)
//                        {
//                            tempTask = DefinedTasks.get(i);
//                            minDistance = dist;
//        
//                            //System.out.println("initial tempTask");
//                        }
//                        break;
//                }
//            }
//        //}
//        System.out.println("ABCDE choose task -> tempTask : "+tempTask.getAction()+tempTask.getDestination()+tempTask.getItem()+tempTask.getJob()+tempTask.getAmount());
//            if(tempTask != null && tempTask.getAction() != null )
//            {
//                if(takenTasks.contains(tempTask) == false)
//                {
//                    myTask = tempTask;
//                    //takenTasks.add(myTask);
//                    hasTask = true;
//                    System.out.println("ABCDE choose task -> myTask : "+myTask.getAction()+tempTask.getDestination()+myTask.getItem()+myTask.getJob()+myTask.getAmount());
//                    broadcast(new Percept("taskTaken", new Identifier(myTask.getJob()),new Identifier(myTask.getAction()),new Identifier(myTask.getItem()),new Identifier(String.valueOf(myTask.getAmount())),new Identifier(myTask.getDestination())), getName());
//                }
//            }
//          
//    }
//    
//    private void doTask()
//    {
//        //check charge
//        /*if( checkCharge() == false )
//        {
//            //do charge
//            actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
//            pauseMyTask =true;
//            return;
//            
//        }*/
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11ABCDE DoTask myTask : "+myTask.getAction());
//        switch(myTask.getAction())
//        {
//            case "buy":
//                buy();
//                break;
//            case "carryToWorkshop":
//                carryToWorkshop();
//                break;
//            case "carryToStorage":
//                carryToStorage();
//                break;
//            case "assemble":
//                assemble();
//              break;  
//        }
//    }
//    private boolean checkCharge()
//    {
//        double dist = 10;
//        /*switch(myTask.getAction())
//        {
//            case "buy":
//                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Shops.get(myTask.getDestination()).getShopLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Shops.get(myTask.getDestination()).getShopLon(),2) );
//                break;
//            case "assemble":
//                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Workshops.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Workshops.get(myTask.getDestination()).getLon(),2) );
//                break;
//            case "carryToWorkshop":
//                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Workshops.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Workshops.get(myTask.getDestination()).getLon(),2) );
//                break;
//            case "carryToStorage":
//                dist = Math.sqrt( Math.pow(AP.getSelfInfo().getLat()-AP.Storages.get(myTask.getDestination()).getLat(),2) + Math.pow(AP.getSelfInfo().getLon()-AP.Storages.get(myTask.getDestination()).getLon(),2) );
//                break;
//        }
//        */
//        Pair<String,Double> chargingStationInfo = findNearestChargeStation();
//        dist = chargingStationInfo.getRight();
//        int currentCharge = AP.getSelfInfo().getCharge();
//        int expectedCharge = (int) ((dist/(AP.getSelfRole().getSpeed()))*10);
//        System.out.println("currentCharge : "+currentCharge+"expectedCharge : "+expectedCharge);
//        int chargeTH = 10;
//        if(currentCharge - expectedCharge < chargeTH)
//        {
//            //pause current task and go to charge station
//            return false;
//        }
//        else
//        {
//           //do task
//            return true;
//        }
//                
//        
//    }
//     private Pair<String,Double> findNearestChargeStation ()
//    {
//        double minDistance = Double.MAX_VALUE;
//        String chargeStation = "";
//        
//        for (int i = 0; i < AP.getChargingStations().size(); i++)
//        {
//            chargingStation next = AP.getChargingStations().get(i);
//            double stationLat = next.getLat();
//            double stationLon = next.getLon();
//            double myLat = AP.getSelfInfo().getLat();
//            double myLon = AP.getSelfInfo().getLon();
//            double dstation = Math.sqrt((stationLat-myLat)*(stationLat-myLat) + (stationLon-myLon)*(stationLon-myLon));
//            if (dstation < minDistance)
//            {
//                minDistance = dstation;
//                chargeStation = next.getName();
//            }
//        }
//        return new Pair<String,Double> (chargeStation,minDistance);
//    }
//     
//     private void charge()
//     {
//         if( (AP.getSelfInfo().getLastAction().compareTo("charge") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
//         {
//             if(AP.getSelfInfo().getCharge() > AP.getSelfRole().getBattery()-5)
//             {
//                pauseMyTask = false;
//                actionQueue.clear();
//             }
//             else
//             {
//                 actionQueue.add(new Action("charge"));
//             }
//             return;
//         }   
//         int routeLength = AP.getRouteLength();
//         if (AP.getRoutes().size() >1 && routeLength>1)
//         {
//             actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
//         }
//         else
//         {
//             actionQueue.add(new Action("goto", new Identifier(findNearestChargeStation().getLeft())));
//             actionQueue.add(new Action("charge"));
//         }
//         
//     }
//     private void buy()
//     {
//         if( (AP.getSelfInfo().getLastAction().compareTo("buy") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
//         {
//            //task is done!
//             hasTask = false;
//             //myTask = null;
//             actionQueue.clear();
//             return;
//         }
//         int routeLength = AP.getRouteLength();
//         if (AP.getRoutes().size() >1 && routeLength>1)
//         {
//            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//         }
//         else
//         {
//             actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//             actionQueue.add(new Action("buy", new Identifier(myTask.getItem()), new Numeral(myTask.getAmount())));
//         }
//         
//     }
//     private void carryToStorage()
//     {
//         if( (AP.getSelfInfo().getLastAction().compareTo("deliver_job") == 0) &&  (AP.getSelfInfo().getLastActionResult().compareTo("successful") == 0) )
//         {
//            //task is done!
//             //myTask = null;
//             hasTask = false;
//             actionQueue.clear();
//             return;
//         }
//         int routeLength = AP.getRouteLength();
//         if (AP.getRoutes().size() >1 && routeLength>1)
//         {
//            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//         }
//         else
//         {
//             actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//             actionQueue.add(new Action("deliver_job", new Identifier(myTask.getJob())));
//         }
//     }
//     private void carryToWorkshop()
//     {
//         int routeLength = AP.getRouteLength();
//         if (AP.getRoutes().size() >1 && routeLength>1)
//         {
//            actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//         }
//         else
//         {
//             actionQueue.add(new Action("goto", new Identifier(myTask.getDestination())));
//             //myTask = null;
//             hasTask = false;
//         }
//     }
//     private void assemble()
//     {
//          actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
//          actionQueue.add(new Action("assemble", new Identifier(myTask.getItem())));
//          //myTask = null;
//          hasTask = false;
//     }
}
