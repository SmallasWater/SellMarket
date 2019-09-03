package market.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import market.load.sMarket;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class pItems {

    private String player;

    private Config config;

    private static LinkedHashMap<sType, LinkedList<iTypes>> sellItems = new LinkedHashMap<>();

    public static pItems getInstance(String player){
        return sMarket.getApi().playerItems.get(player);
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
        for(sType type:sType.values()){
            types = new LinkedList<>();
            Object map = config.get(type.getName());
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

    public Config getConfig() {
        return config;
    }

    public String getPlayer() {
        return player;
    }

    public LinkedList<iTypes> getSellItems(sType type) {
        return sellItems.get(type);
    }

    public void setSellItems(sType type,LinkedList<iTypes> sellItem) {
        sellItems.put(type,sellItem);
    }

    public void save(){
        for(sType type:sType.values()){
            config.set(type.getName(),iTypes.toSave(getSellItems(type)));
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
        for(iTypes iTypes2:getSellItems(iType.type)) {
            if (iType.equals(iTypes2)){
                return iTypes2;
            }
        }
        return null;
    }


    public void removeSellItem(iTypes iTypes){
        LinkedList<iTypes> iTypes1 = getSellItems(iTypes.type);
        for(iTypes iTypes2:iTypes1){
            if(iTypes.equals(iTypes2)){
                iTypes2.setCount(iTypes2.getCount()-iTypes.count);
                if(iTypes2.count <= 0){
                    iTypes1.remove(iTypes2);
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
        for(sType t:sType.values()){
            types.addAll(getSellItems(t));
        }
        return types;
    }

//    public LinkedList<iTypes> seekItems(String id){
//        LinkedList<iTypes> types = new LinkedList<>();
//        for (sType type:sellItems.keySet()){
//            for(iTypes item:getSellItems(type)){
//                if(item.toSeekItem().equals(iTypes.toSeekItem(id),true,false)){
//                    types.add(item);
//                }
//            }
//        }
//        return types;
//    }
}
