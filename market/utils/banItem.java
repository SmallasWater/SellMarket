package market.utils;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

public class banItem {

    private Item item;

    public banItem(Item item){
        this.item = item;
    }


    public banItem(int id,int mate,int count){
        this(Item.get(id,mate,count).clone());
    }


    /**
     * tag 必须为16进制字符或not
     * */
    private banItem(int id, int mate, int count, String tag){
        Item item = Item.get(id,mate,count);
        if(!"not".equals(tag)){
            CompoundTag compoundTag = Item.parseCompoundTag(Tools.hexStringToBytes(tag));
            item.setNamedTag(compoundTag);
        }
        this.item = item;
    }

    public static banItem get(Item item){
        return new banItem(item);
    }

    public static banItem get(int id,int mate,int count,String tag){
        return new banItem(id,mate,count,tag);
    }


    public String getName(){
        return ItemIDSunName.getIDByName(item);
    }

    /**
     * 格式: id:damage:count:tag
     * */


    public Item getItem() {
        return item;
    }

    @Override
    public String toString(){
        return item.getId()+":"+item.getDamage()+":"+item.getCount()+":"
                +((item.hasCompoundTag())?Tools.bytesToHexString(item.getCompoundTag()):"not");
    }



    public static banItem toItem(String defaultString){
        String[] strings = defaultString.split(":");
        return new banItem(Integer.parseInt(strings[0]),Integer.parseInt(strings[1])
                ,Integer.parseInt(strings[2]),strings[3]);
    }



    public boolean equals(banItem itemClass) {
        if (item.hasCompoundTag() || itemClass.getItem().hasCompoundTag()) {
            Item item1 = itemClass.getItem();
            CompoundTag tag1 = item1.getNamedTag();
            CompoundTag tag2 = item.getNamedTag();
            if(item != null){
                if(tag1 != null && tag2 != null){
                    if(tag1.equals(tag2)){
                        return item1.getId() == item.getId() && item1.getDamage() == item.getDamage();
                    }
                }
            }
        } else {
            return (item.getId() + ":" + item.getDamage()).equals(itemClass.item.getId() + ":" + itemClass.item.getDamage());
        }
        return false;
    }
}
