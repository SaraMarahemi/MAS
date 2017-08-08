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
    private int battery;
    private int load;
    private String name;
    private int speed;
    private List<String> tools = new Vector<>();
    //item
    private List<item> itemsInEnv = new Vector<>();
    
    

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
                    name = agName.getValue();
                    //speed
                    eis.iilang.Numeral agSpeed = (eis.iilang.Numeral) p.getParameters().toArray()[1];
                    speed = agSpeed.getValue().intValue();
                    //load
                    eis.iilang.Numeral agLoad = (eis.iilang.Numeral) p.getParameters().toArray()[2];
                    load = agLoad.getValue().intValue();
                    //battery
                    eis.iilang.Numeral agBattery = (eis.iilang.Numeral) p.getParameters().toArray()[3];
                    battery = agBattery.getValue().intValue();
                    //tools
                    ParameterList agTools = listParam(p, 4);
                    for (Parameter tool : agTools) 
                    {
                            if(tool instanceof Identifier) {
                                String toolName = ((Identifier) tool).getValue();
                                tools.add(toolName);
                            }
                    }   
                    break;
                    
                case "item" :
                    System.out.println("ABCDEF : item"+p.toProlog());
                    
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
                  
                    
                    //Cheking
                    /*System.out.println("item name : "+itemName);
                    System.out.println("item volume : "+itemVolume);
                    
                    for(int i=0; i<itemTools.size() ; i++)
                    {
                        System.out.println("item tool : "+itemTools.get(i));
                    }
                    
                    for(int i=0; i<itemParts.size() ; i++)
                    {
                        System.out.println("sub item name : "+itemParts.get(i).getSubItemName());
                        System.out.println("sub item amount : "+itemParts.get(i).getSubItemAmount());
                    }*/
                    break;
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