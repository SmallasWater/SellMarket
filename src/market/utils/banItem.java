package market.utils;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

public class banItem {

    private Item item;

    public banItem(Item item){
        this.item = item;
    }


    /**
     * tag 必须为16进制字符或not
     * */
    private banItem(int id, int mate, String tag){
        Item item = Item.get(id,mate);
        if(!"not".equals(tag)){
            CompoundTag compoundTag = Item.parseCompoundTag(Tools.hexStringToBytes(tag));
            item.setNamedTag(compoundTag);
        }
        this.item = item;
    }

    public static banItem get(Item item){
        return new banItem(item);
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
        return item.getId()+":"+item.getDamage()+":"
                +((item.hasCompoundTag())?Tools.bytesToHexString(item.getCompoundTag()):"not");
    }



    public static banItem toItem(String defaultString){
        String[] strings = defaultString.split(":");
        return new banItem(Integer.parseInt(strings[0]),Integer.parseInt(strings[1]),strings[2]);
    }



    @Override
    public boolean equals(Object itemClass) {
        if(itemClass instanceof banItem){
            if (item.hasCompoundTag() || ((banItem) itemClass).getItem().hasCompoundTag()) {
                Item item1 = ((banItem) itemClass).getItem();
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
                return (item.getId() + ":" + item.getDamage()).equals(((banItem) itemClass).item.getId() + ":" + ((banItem) itemClass).item.getDamage());
            }
        }

        return false;
    }

    public String getId(){
        return item.getId()+":"+item.getDamage();
    }
}
