package market.player;

import cn.nukkit.item.Item;
import market.utils.ItemIDSunName;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static market.utils.Tools.hexStringToBytes;

public class iTypes implements Cloneable{

    public String master;

    public String type;

    public double money;

    public String tag;

    public String id;

    public int count;

    public String showName;

    public String message;

    public iTypes(String master,String type,String id,int count){
       this(master,type,id,count,10D);
    }

    public iTypes(String master,String type,String id,int count,double money){
        this(master,type,id,count,money,"");
    }

    public iTypes(String master,String type,String id,int count,double money,String tag){
        this(master,type,id,count,money,tag,"欢迎购买");
    }

    public iTypes(String master,String type,String id,int count,double money,String tag,String message,String showName){
        this.count = count;
        this.id = id;
        this.money = money;
        this.tag = tag;
        this.message = message;
        this.master = master;
        this.type = type;
        this.showName = showName;

    }

    public iTypes(String master,String type,String id,int count,double money,String tag,String message){
        this.count = count;
        this.id = id;
        this.money = money;
        this.tag = tag;
        this.message = message;
        this.master = master;
        this.type = type;
        this.showName = getName();
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaster() {
        return master;
    }

    public double getMoney() {
        return money;
    }

    public void setCount(int count) {
        this.count = count;
    }


    void setMessage(String message) {
        this.message = message;
    }

    void setMoney(double money) {
        this.money = money;
    }

    public int getCount() {
        return count;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public boolean hasTag(){
        return getItem().hasCompoundTag();
    }

    private static Item toSeekItem(String id){
        if(id != null && !"".equals(id)) {
            int i;
            int damage = 0;
            if (id.split(":").length > 1) {
                i = Integer.parseInt(id.split(":")[0]);
                damage = Integer.parseInt(id.split(":")[1]);
            } else {
                i = Integer.parseInt(id.split(":")[0]);
            }
            return new Item(i, damage);
        }
        return null;

    }

    private Item toSeekItem(){
        return toSeekItem(id);
    }


    public Item getItem(){
        if(id != null && !"".equals(id)){
            Item item = toSeekItem();
            item.setCount(count);
            if(!"".equals(tag)){
                item.setNamedTag(Item.parseCompoundTag(hexStringToBytes(tag)));
            }

            return item;
        }
        return null;
    }
    /**
     * 食物:
     *   id:damage:
     *    count:
     *    nbt:
     *    message:
     *    one-Money*/
    static  iTypes toType(String master, String type, String id, Map map){
        int count = 0;
        String nbt = null;
        double money = 10D;
        String message = "";
        String showName = "";
        if(map.containsKey("count")){
            count = (int) map.get("count");
        }
        if(map.containsKey("nbt")){
            nbt = (String) map.get("nbt");
        }
        if(map.containsKey("one-Money")){
            money = (double) map.get("one-Money");
        }
        if(map.containsKey("message")){
            message = (String) map.get("message");
        }
        if(map.containsKey("showName")){
            showName = (String) map.get("showName");
        }
        return new iTypes(master,type,id,count,money,nbt,message,showName);
    }

    boolean equals(iTypes type) {
        return id.equals(type.id) && tag.equals(type.tag) && master.equals(type.master);
    }

    static LinkedHashMap<String,Object> toSave(LinkedList<iTypes> sellItems) {
        LinkedHashMap<String,Object> o = new LinkedHashMap<>();
        for(iTypes types:sellItems){
            o.put(types.id,new LinkedHashMap<String,Object>(){
                {
                    put("count",types.count);
                    put("nbt",types.tag);
                    put("one-Money",types.money);
                    put("message",types.message);
                    put("showName",types.showName);
                }
            });
        }
        return o;
    }

    @Override
    public String toString() {
        return "{master:"+master+"id:"+id+"type:"+type+"count: "+count+"money:"+money+"}";
    }

    public String getName(){
        if(showName != null &&!"".equals(showName)){
            return showName;
        }
        return ItemIDSunName.getIDByName(getItem());
    }

    @Override
    public iTypes clone() {
        try {
            return  (iTypes) super.clone();
        }catch (CloneNotSupportedException e){
            return null;
        }
    }
}
