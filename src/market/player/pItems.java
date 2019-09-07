package market.player;


import cn.nukkit.utils.Config;
import market.sMarket;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class pItems {

    private String player;

    private Config config;

    private LinkedHashMap<String, LinkedList<iTypes>> sellItems = new LinkedHashMap<>();

    public static pItems getInstance(String player){
        return sMarket.playerItems.get(player);
    }

    @Override
    public String toString() {
        return "player: "+player+"Config:"+config.getAll().toString();
    }

    public pItems(String player, Config config){
        this.player = player;
        this.config = config;
        init();
    }

    private void init(){
        LinkedList<iTypes> types;
        for(String type:sMarket.sType.keySet()){
            types = new LinkedList<>();
            Object map = config.get(type);
            if(map instanceof Map){
                if(((Map) map).size() > 0){
                    for (Object o:((Map) map).keySet()){
                        if(o instanceof String){
                            Object map1 = ((Map) map).get(o);
                            if(map1 instanceof Map){
                                types.add(iTypes.toType(player,type,(String) o,(Map) map1));
                            }
                        }
                    }
                }
            }
            sellItems.put(type,types);
        }
    }

    public String getPlayer() {
        return player;
    }

    public LinkedList<iTypes> getSellItems(String type) {
        if(!sellItems.containsKey(type)) {
            return null;
        }
        return sellItems.get(type);
    }

    public void save(){
        for(String type:sMarket.sType.keySet()){
            LinkedList<iTypes> types = getSellItems(type);
            if(types != null){
                config.set(type,iTypes.toSave(types));
            }
        }
        config.save();
    }

    public void addSellItem(iTypes iTypes){
       iTypes iTypes1 = inArray(iTypes);
       if(iTypes1 != null){
           iTypes1.setMoney(iTypes.money);
           iTypes1.setCount(iTypes1.getCount()+iTypes.count);
           iTypes1.setMessage(iTypes.message);
       }else{
           getSellItems(iTypes.type).add(iTypes);
       }

    }

    public iTypes inArray(iTypes iType){
        LinkedList<iTypes> types = getSellItems(iType.type);
        if(types != null){
            for(iTypes iTypes2:types) {
                if (iType.equals(iTypes2)){
                    return iTypes2;
                }
            }
        }
        return null;
    }


    public void removeSellItem(iTypes iTypes){
        LinkedList<iTypes> iTypes1 = getSellItems(iTypes.type);
        if(iTypes1 != null){
            for(iTypes iTypes2:iTypes1){
                if(iTypes.equals(iTypes2)){
                    iTypes2.setCount(iTypes2.getCount() - iTypes.count);
                    if(iTypes2.count <= 0){
                        iTypes1.remove(iTypes2);
                    }
                }
            }
        }
    }

    public void setSellItem(iTypes types){
        iTypes iTypes1 = inArray(types);
        if(iTypes1 != null){
            iTypes1.setMessage(types.message);
            iTypes1.setMoney(types.money);
            iTypes1.setShowName(types.showName);
        }
    }

    public LinkedList<iTypes> getAllItems(){
        LinkedList<iTypes> types = new LinkedList<>();
        for(String t:sMarket.sType.keySet()){
            if(sellItems.containsKey(t)){
                types.addAll(sellItems.get(t));
            }
        }
        return types;
    }

}
