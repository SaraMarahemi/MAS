///***
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package massim.javaagents.agents;

import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import java.util.List;
import java.util.Vector;

import static massim.javaagents.agents.AgentPercepts.stringParam;

/**
 *
 * @author Sarah
 */
public class AgentPercepts {
    
    private List<Percept> percepts = new Vector<>();
    //Initial Percept
    //role
    role selfRole;
    //item
    private List<massim.javaagents.agents.item> itemsInEnv = new Vector<>();
    //self
    private self selfInfo = new self();
    //entity
    private List<entity> entities = new Vector<>();
    //shop
    private List<shop> shops = new Vector<>();
    //workshop
    private List<workshop> workshops = new Vector<>();
    //dumps
    private List<dump> dumps = new Vector<>();
    //chargingStation
    private List<chargingStation> chargingStations = new Vector<>();
    //storage
    private List<storage> storages = new Vector<>();
    //resourceNode
    private List<resourceNode> resourceNodes = new Vector<>();
            
    

    public List<Percept> getPercepts() {
        return percepts;
    }

    public role getRole() {
        return selfRole;
    }

    public List<item> getItems() {
        return itemsInEnv;
    }
    
    

    public AgentPercepts() 
    {
        
    }
    
    void setPercepts (List<Percept> p)
    {
        percepts = p;
    }
    
    void initialize ()
    {
        for (Percept p: percepts){  
            switch(p.getName())
            {
                case "role" :
                    
                    //name 
                    eis.iilang.Identifier agName = (eis.iilang.Identifier) p.getParameters().toArray()[0];
                    String name = agName.getValue();
                    //speed
                    eis.iilang.Numeral agSpeed = (eis.iilang.Numeral) p.getParameters().toArray()[1];
                    int speed = agSpeed.getValue().intValue();
                    //load
                    eis.iilang.Numeral agLoad = (eis.iilang.Numeral) p.getParameters().toArray()[2];
                    int load = agLoad.getValue().intValue();
                    //battery
                    eis.iilang.Numeral agBattery = (eis.iilang.Numeral) p.getParameters().toArray()[3];
                    int battery = agBattery.getValue().intValue();
                    //tools
                    List<String> tools = new Vector<>();
                    ParameterList agTools = listParam(p, 4);
                    for (Parameter tool : agTools) 
                    {
                            if(tool instanceof Identifier) {
                                String toolName = ((Identifier) tool).getValue();
                                tools.add(toolName);
                            }
                    }   
                    selfRole = new role(battery,load,name,speed,tools);
                    break;
                    
                case "item" :
                    
                    //item name
                    eis.iilang.Identifier itName = (eis.iilang.Identifier) p.getParameters().toArray()[0];
                    String itemName = itName.getValue();
                    //item volume
                    eis.iilang.Numeral itVolume = (eis.iilang.Numeral) p.getParameters().toArray()[1];
                    int itemVolume = itVolume.getValue().intValue();
                    //item tools
                    List<String> itemTools = new Vector<>();
                    eis.iilang.Function toolFunction = (eis.iilang.Function) p.getParameters().toArray()[2];
                    ParameterList toolsRequired = (ParameterList) toolFunction.getParameters().get(0);
                    String toolName;
                    for(int j=0; j<toolsRequired.size();j++)
                    {
                        eis.iilang.Identifier itTool = (eis.iilang.Identifier) toolsRequired.get(j);
                        toolName = itTool.getValue();
                        itemTools.add(toolName);
                    }
                    
                    //item parts
                    List<subItem> itemParts = new Vector<>();
                    eis.iilang.Function partFunction = (eis.iilang.Function) p.getParameters().toArray()[3];
                    ParameterList partsRequired = (ParameterList) partFunction.getParameters().get(0);
                   
                    for(int i= 0; i< partsRequired.size();i++)
                    {
                        eis.iilang.ParameterList subItem = (eis.iilang.ParameterList) partsRequired.get(i);
                        eis.iilang.Identifier sn = (eis.iilang.Identifier) subItem.get(0);
                        eis.iilang.Numeral sa = (eis.iilang.Numeral) subItem.get(1);
                        String subItemName = sn.getValue();
                        int subItemAmount = sa.getValue().intValue();
                        subItem newSubItem = new subItem(subItemName, subItemAmount);
                        itemParts.add(newSubItem);
                    }
                    massim.javaagents.agents.item newItem = new item(itemName, itemVolume, itemTools, itemParts);
                    itemsInEnv.add(newItem);
                    break;
            }
        }
    }
    
    void stepPercept()
    {
        for (Percept p: percepts){  
            //System.out.println("ABCDEF : All : "+p.toProlog());
            switch(p.getName())
            {
                case "charge" :
                    eis.iilang.Numeral batteryInfo = (eis.iilang.Numeral) p.getParameters().toArray()[0];
                    int battery = batteryInfo.getValue().intValue();
                    selfInfo.setCharge(battery);
                    break;
                    
                case "lat":
                    eis.iilang.Numeral latInfo = (eis.iilang.Numeral) p.getParameters().toArray()[0];
                    double lat = latInfo.getValue().doubleValue();
                    selfInfo.setLat(lat);
                    break;
                    
                case "load":
                    eis.iilang.Numeral loadInfo = (eis.iilang.Numeral) p.getParameters().toArray()[0];
                    int load = loadInfo.getValue().intValue();
                    selfInfo.setLoad(load);
                    break;
                    
                case "lon":
                    eis.iilang.Numeral lonInfo = (eis.iilang.Numeral) p.getParameters().toArray()[0];
                    double lon = lonInfo.getValue().doubleValue();
                    selfInfo.setLon(lon);
                    break;
                    
                case "name":
                    //It has a problem!
                    //System.out.println("ABCDEF : name : "+p.toProlog());
                    String name ="";
                    name = p.toProlog();
                    //System.out.println("ABCDEF : name : "+name);
                    selfInfo.setName(name);
                    break;
               
                case "team":
                    String team = stringParam(p.getParameters(), 0);
                    selfInfo.setTeam(team);
                    break;
                    
                case "money":
                    eis.iilang.Numeral moneyInfo = (eis.iilang.Numeral) p.getParameters().toArray()[0];
                    int money = moneyInfo.getValue().intValue();
                    selfInfo.setTeamMoney(money);
                    break;
                    
                case "lastAction":  
                    String lastAction = stringParam(p.getParameters(), 0);
                    selfInfo.setLastAction(lastAction);
                    break;
                    
                case "lastActionResult":
                    String lastActionResult = stringParam(p.getParameters(), 0);
                    selfInfo.setLastActionResult(lastActionResult);
                    break;
                    
                case "lastActionParams":
                    break;
                
                case "entity" :
                    //entity(agentB1,B,48.84811,2.34406,car)
                    //entityname
                    eis.iilang.Identifier Name = (eis.iilang.Identifier) p.getParameters().toArray()[0];
                    String entityname = Name.getValue();
                    
                    //entityteam
                    eis.iilang.Identifier Team = (eis.iilang.Identifier) p.getParameters().toArray()[1];
                    String entityteam = Team.getValue();
                    
                    //entitylat
                    eis.iilang.Numeral Lat = (eis.iilang.Numeral) p.getParameters().toArray()[2];
                    double entitylat = Lat.getValue().doubleValue();
                    
                    //entitylon
                    eis.iilang.Numeral Lon = (eis.iilang.Numeral) p.getParameters().toArray()[3];
                    double entitylon = Lon.getValue().doubleValue();
                    
                    //entityrole
                    eis.iilang.Identifier eRole = (eis.iilang.Identifier) p.getParameters().toArray()[4];
                    String entityrole = eRole.getValue();
                    
                    entity newEntity = new entity(entityname, entityteam, entitylat, entitylon, entityrole);
                    entities.add(newEntity);
                    break;
                    
                case "shop" :
                    
                    //name
                    eis.iilang.Identifier shopNameInfo = (eis.iilang.Identifier) p.getParameters().toArray()[0];
                    String shopName = shopNameInfo.getValue();
                    
                    //lat
                    eis.iilang.Numeral shopLatInfo = (eis.iilang.Numeral) p.getParameters().toArray()[1];
                    double shopLat = shopLatInfo.getValue().doubleValue();
                    
                    //lon
                    eis.iilang.Numeral shopLonInfo = (eis.iilang.Numeral) p.getParameters().toArray()[2];
                    double shopLon = shopLonInfo.getValue().doubleValue();
                    
                    
                    //restock
                    eis.iilang.Numeral shopRestockInfo = (eis.iilang.Numeral) p.getParameters().toArray()[3];
                    int shopRestock = shopRestockInfo.getValue().intValue();
                    
                    //item
                    List<shopItem> shopItems = new Vector<>();
                    ParameterList shopItemInfo = (ParameterList) p.getParameters().toArray()[4];
                    for(int i= 0; i< shopItemInfo.size();i++)
                    {
                        eis.iilang.Function shItem = (eis.iilang.Function) shopItemInfo.get(0);
                        eis.iilang.Identifier shItemName = (eis.iilang.Identifier)shItem.getParameters().get(0);
                        String shopItemName = shItemName.getValue();
                        eis.iilang.Numeral shItemPrice = (eis.iilang.Numeral) shItem.getParameters().get(1);
                        int price = shItemPrice.getValue().intValue();
                        eis.iilang.Numeral shItemAmount = (eis.iilang.Numeral) shItem.getParameters().get(2);
                        int amount = shItemAmount.getValue().intValue();
                        shopItem newShopItem = new shopItem(amount, shopItemName, price);
                        shopItems.add(newShopItem);
                    }
                    shop newShop = new shop(shopLat, shopLon, shopName, shopRestock, shopItems);
                    shops.add(newShop);
                    break;
                    
                case "workshop" :
                    
                    //name
                    eis.iilang.Identifier workshopName = (eis.iilang.Identifier)p.getParameters().toArray()[0];
                    String wName = workshopName.getValue();
                    
                    //lat
                    eis.iilang.Numeral workShopLat = (eis.iilang.Numeral) p.getParameters().get(1);
                    double wLat = workShopLat.getValue().doubleValue();
                    
                    //lon
                    eis.iilang.Numeral workShopLon = (eis.iilang.Numeral) p.getParameters().get(2);
                    double wLon = workShopLon.getValue().doubleValue();
                    
                    workshop newWorkshop = new workshop(wName, wLat, wLon);
                    workshops.add(newWorkshop);
                    break;
                    
                case "chargingStation" :
                    System.out.println("ABCDEF : chargingStation"+p.toProlog());
                    //name
                    eis.iilang.Identifier chargingStationName = (eis.iilang.Identifier)p.getParameters().toArray()[0];
                    String csName = chargingStationName.getValue();
                    
                    //lat
                    eis.iilang.Numeral chargingStationLat = (eis.iilang.Numeral) p.getParameters().get(1);
                    int csLat = chargingStationLat.getValue().intValue();
                    
                    //lon
                    eis.iilang.Numeral chargingStationLon = (eis.iilang.Numeral) p.getParameters().get(2);
                    int csLon = chargingStationLon.getValue().intValue();
                    
                    //rate
                    eis.iilang.Numeral chargingStationRate = (eis.iilang.Numeral) p.getParameters().get(3);
                    int csRate = chargingStationRate.getValue().intValue();
                    
                    chargingStation newchargingStation = new chargingStation(csName, csLat, csLon,csRate);
                    chargingStations.add(newchargingStation);
                    break;
                    
                case "dump" :
                    
                    //name
                    eis.iilang.Identifier dumpName = (eis.iilang.Identifier)p.getParameters().toArray()[0];
                    String dName = dumpName.getValue();
                    
                    //lat
                    eis.iilang.Numeral dumpLat = (eis.iilang.Numeral) p.getParameters().get(1);
                    double dLat = dumpLat.getValue().doubleValue();
                    
                    //lon
                    eis.iilang.Numeral dumpLon = (eis.iilang.Numeral) p.getParameters().get(2);
                    double dLon = dumpLon.getValue().doubleValue();
                    
                    dump newdump = new dump(dName, dLat, dLon);
                    dumps.add(newdump);
                    
                    break;
                case "storage" :
                    
                    //name
                    eis.iilang.Identifier stName = (eis.iilang.Identifier)p.getParameters().toArray()[0];
                    String storageName = stName.getValue();
                    
                    //lat
                    eis.iilang.Numeral stLat = (eis.iilang.Numeral) p.getParameters().get(1);
                    double storageLat = stLat.getValue().doubleValue();
                    
                    //lon
                    eis.iilang.Numeral stLon = (eis.iilang.Numeral) p.getParameters().get(2);
                    double storageLon = stLon.getValue().doubleValue();
                    
                    //TotalCapacity
                    eis.iilang.Numeral stTotalCapacity = (eis.iilang.Numeral) p.getParameters().get(3);
                    int storageTotalCapacity = stTotalCapacity.getValue().intValue();
                    
                    //UsedCapacity
                    eis.iilang.Numeral stUsedCapacity = (eis.iilang.Numeral) p.getParameters().get(4);
                    int storageUsedCapacity = stUsedCapacity.getValue().intValue();
                    
                    //items
                    List<storageItem> storageItems = new Vector<>();
                    ParameterList storageItemInfo = (ParameterList) p.getParameters().toArray()[5];
                    for(int i= 0; i< storageItemInfo.size();i++)
                    {
                        eis.iilang.Function stItem = (eis.iilang.Function) storageItemInfo.get(0);
                        eis.iilang.Identifier stItemName = (eis.iilang.Identifier)stItem.getParameters().get(0);
                        String storageItemName = stItemName.getValue();
                        eis.iilang.Numeral stItemDelivered = (eis.iilang.Numeral) stItem.getParameters().get(1);
                        int storageItemDelivered = stItemDelivered.getValue().intValue();
                        eis.iilang.Numeral stItemStored = (eis.iilang.Numeral) stItem.getParameters().get(2);
                        int storageItemStored = stItemStored.getValue().intValue();
                        
                        storageItem newStorageItem = new storageItem(storageItemName, storageItemDelivered, storageItemStored);
                        storageItems.add(newStorageItem);
                    }
                    storage newStorage = new storage(storageName,storageLat, storageLon,  storageTotalCapacity, storageUsedCapacity,storageItems);
                    storages.add(newStorage);
                    
                    break;
                case "resourceNode" :
                    
                    //name
                    eis.iilang.Identifier rnName = (eis.iilang.Identifier)p.getParameters().toArray()[0];
                    String resourceNodeName = rnName.getValue();
                    
                    //lat
                    eis.iilang.Numeral rnLat = (eis.iilang.Numeral) p.getParameters().get(1);
                    double resourceNodeLat = rnLat.getValue().doubleValue();
                    
                    //lon
                    eis.iilang.Numeral rnLon = (eis.iilang.Numeral) p.getParameters().get(2);
                    double resourceNodeLon = rnLon.getValue().doubleValue();
                    
                    //resource
                     eis.iilang.Identifier rnResource = (eis.iilang.Identifier)p.getParameters().toArray()[3];
                    String resourceNodeResource = rnResource.getValue();
                    
                    resourceNode newResourceNode = new resourceNode(resourceNodeName, resourceNodeLat, resourceNodeLon, resourceNodeResource);
                    resourceNodes.add(newResourceNode);
                    
                    break;
                    
                case "step":
                case "route":
                case "seedCapital":
                case "steps":
                case "map":
                case "routeLength":
                case "actionID":
                case "job":
            }
        }
    }
    
      /**
     * Tries to extract a parameter from a list of parameters.
     * @param params the parameter list
     * @param index the index of the parameter
     * @return the string value of that parameter or an empty string if there is no parameter or it is not an identifier
     */
    public static String stringParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return "";
        Parameter param = params.get(index);
        if(param instanceof Identifier) return ((Identifier) param).getValue();
        return "";
    }

    /**
     * Tries to extract an int parameter from a list of parameters.
     * @param params the parameter list
     * @param index the index of the parameter
     * @return the int value of that parameter or -1 if there is no parameter or it is not an identifier
     */
    private static int intParam(List<Parameter> params, int index){
        if(params.size() < index + 1) return -1;
        Parameter param = params.get(index);
        if(param instanceof Numeral) return ((Numeral) param).getValue().intValue();
        return -1;
    }

    /**
     * Tries to extract a parameter from a percept.
     * @param p the percept
     * @param index the index of the parameter
     * @return the string value of that parameter or an empty string if there is no parameter or it is not an identifier
     */
    private static ParameterList listParam(Percept p, int index){
        List<Parameter> params = p.getParameters();
        if(params.size() < index + 1) return new ParameterList();
        Parameter param = params.get(index);
        if(param instanceof ParameterList) return (ParameterList) param;
        return new ParameterList();
    }
}

///***