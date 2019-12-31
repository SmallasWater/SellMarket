package market.from;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import market.sMarket;
import market.player.iTypes;
import market.player.pItems;
import market.utils.ItemIDSunName;
import market.utils.Tools;
import market.utils.banItem;
import market.utils.seekSetting;



import java.util.LinkedList;
import java.util.Map;

public class create {

    /** 菜单*/
    static final int MENU = 0xAAA001;

    /** 类型*/
    static final int TYPES = 0xAAA002;

    /** 查找*/
    static final int SEEK = 0xAAA003;

    /** 查找显示*/
    static final int SEEK_MENU = 0xAAA004;

    /** 物品设置*/
    static final int SETTING = 0xAAA005;

    /** 上架物品*/
    static final int UPDATA = 0xAAA006;


    /** 我的选择*/
    static final int CHOSE = 0xAAA008;

    /** 类型显示*/
    static final int TYPES_SHOW = 0xAAA009;

    /** 购买界面*/
    static final int BUY_MENU = 0xAAA0010;

    /** 黑名单*/
    private static final int BLACK = 0xAAA0011;

    /** 物品信息*/
    private static final int MESSAGE = 0xAAA0012;

    /** 账单*/
    private static final int BILL = 0xAAA0013;

    /** 账单*/
    static final int ADD_INVENTORY = 0xAAA0014;



    /** 主页菜单*/
    public static void sendMenu(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Home page",
                sMarket.getApi().config.getString("Cunstom Post").replace("{End Line}","\n"));
        for(String s:sMarket.menu.keySet()){
            Object obj = sMarket.menu.get(s);
            if(obj instanceof Map){
                String message = (String) ((Map)obj).get("Alias");
                String path = (String) ((Map)obj).get("path to icon");
                simple.addButton(new ElementButton(message,getImage(path)));
            }else if(obj instanceof String){
                simple.addButton(new ElementButton(s,getImage((String) sMarket.menu.get(s))));
            }
        }
        send(player,simple,MENU);
    }

    /** 玩家设置*/
    static void sendSetting(Player player){
        iTypes item = sMarket.clickItem.get(player);
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--Edit");
        String s = "§r§lItem Name:"+item.getName()+"\n\n"+
                  "§r§lItem amount:§a"+item.getCount()+"\n\n"+
                  "§r§lProduct Description:§r"+item.getMessage()+"\n\n"+
                  "§r§lCommodity price:§c"+(sMarket.money.getMonetaryUnit()+item.getMoney())+"\n";
        custom.addElement(new ElementLabel(s));
        custom.addElement(new ElementDropdown("Modify classification",Tools.toList(sMarket.sType.keySet().toArray()),Tools.getIntByType(item.type)));
        custom.addElement(new ElementInput("Shown name","Please modify the name",item.getName()));
        custom.addElement(new ElementInput("Modify description","Please enter content",item.message));
        custom.addElement(new ElementInput("Change single project price","Please enter a number",String.valueOf(item.money)));
        LinkedList<String> list = new LinkedList<>();
        for(int i = 0;i <= item.getCount();i++){
            if(i == 0){
                list.add("§cRemove items from list");
            }else if(i == item.count){
                list.add("§aItems Unchanged");
            }else{
                list.add("§bDecrease"+(item.getCount()-i));
            }
        }

        custom.addElement(new ElementStepSlider("Modify the numbers of the item (if you want to increase the number just upload items)",list,item.count));
        send(player,custom,SETTING);
    }

    /** 查找列表*/
    static void sendSeekShow(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Search results","");
        seekSetting setting = sMarket.seekSetting.get(player);
        LinkedList<iTypes> seekItems = new LinkedList<>();
        for(iTypes item:Tools.seekItemById(setting.message,setting.type,setting.sqrt)){
            seekItems.add(item);
            simple.addButton(getButton(item));
        }
        sMarket.seekItem.put(player,seekItems);
        simple.setContent("Totally found:"+simple.getButtons().size()+"   Index Setting:"+Tools.getStringBySeekSetting(setting.type)+" §7Index content:  "+setting.message);
        send(player,simple,SEEK_MENU);
    }

    private static ElementButton getButton(iTypes item){
        ElementButtonImageData imageData = new ElementButtonImageData("path",ItemIDSunName.getIDByPath(item.getItem()));
        ElementButton button = new ElementButton(item.getName()+" §7("+item.getId()+")§r"+"\nSingle Item Price:"
                +(sMarket.money.getMonetaryUnit())+(item.getMoney())+"  "+item.getCount()+" left"+"§e(Click to buy!))");
        button.addImage(imageData);
        return button;
    }

    /** 购买页面*/
    static void sendBuyMenu(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--Buy");
        iTypes item = sMarket.clickItem.get(player);
        int count = 0;
        Item item1 = item.getItem();
        if(item1.hasCompoundTag()){
            count = item.getItem().getNamedTag().getAllTags().size();
        }
        String s = "§r§lItem name:  "+item.getName()+"\n\n"+
                   "§r§lSeller: "+item.getMaster()+"\n\n"+
                   "§r§lNBT Tag: §e"+count+"\n\n"+
                   "§r§lItem Amount: §a"+item.getCount()+"\n\n"+
                   "§r§lItem Description: §r"+item.getMessage()+"\n\n"+
                   "§r§lSingle Item Price: §c"+(sMarket.money.getMonetaryUnit()+item.getMoney())+"\n";
        custom.addElement(new ElementLabel(s));
        LinkedList<String> list = new LinkedList<>();
        for(int i = 0;i <= item.getCount();i++){
            if(i == 0){
                list.add("§cBuy No More");
            }else if(i == item.count){
                list.add("§aBuy All Expected to cost "+(sMarket.money.getMonetaryUnit())+(item.getCount() * item.getMoney()));
            }else{
                list.add("§bBuy"+i+" Expected to cost "+(sMarket.money.getMonetaryUnit())+(i * item.getMoney()));
            }

        }
        custom.addElement(new ElementStepSlider("Purchase quantity",list,0));
        send(player,custom,BUY_MENU);
    }

    /** 黑名单*/
    static void sendBlack(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--BlackList","");
        if(sMarket.banItems.size() > 0){
            StringBuilder builder = new StringBuilder();
            for(banItem item:sMarket.banItems){
                builder.append("§r>> ").append(item.getName()).append("§7 (").append(item.getId()).append(")").append("\n");
            }
            simple.setContent(builder.toString());
        }else{
            simple.setContent("Temporarily Unavaliable");
        }
        send(player,simple,BLACK);
    }

    /** 信息*/
    static void sendMessage(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Market Information","");
        StringBuilder builder = new StringBuilder();
        builder.append("§l§aRegistered User: ");
        if(sMarket.playerItems.size() > 0){
            builder.append("\n");
            for(String name:sMarket.playerItems.keySet()){
                Player player1 = Server.getInstance().getPlayer(name);
                builder.append("§r>>  ").append(name).append(player1!= null?"§a[Online]":"§7[Offline]").append("\n");
            }
        }else{
            builder.append(" §Temporarily Unavaliable").append("\n");
        }
        builder.append("\n§l§aTotal Amount of Items: §e").append(Tools.getItemsAll().size()).append(" §aleft");
        for(String type:sMarket.sType.keySet()){
            builder.append("\n§l§a").append(type).append("Type: §e").append(Tools.getItemsByType(type).size()).append(" §aleft");
        }
        simple.setContent(builder.toString());
        send(player,simple,MESSAGE);
    }

    /** 商品列表(类型)*/
    static void sendTypeShow(Player player){
        String t = sMarket.clickPos.get(player);
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Market("+t+")","");
        for(iTypes items: Tools.getItemsByType(t)){
            simple.addButton(getButton(items));
        }
        simple.setContent("Current page amount: "+simple.getButtons().size());
        send(player,simple,TYPES_SHOW);
    }

    /** 查找*/
    static void sendSeekItems(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--Search");
        custom.addElement(new ElementLabel("Different search options will affect search results"));
        custom.addElement(new ElementDropdown("Index settings",Tools.strings,0));
        custom.addElement(new ElementToggle("Automatic sorting",false));
        custom.addElement(new ElementInput("Enter Index","Please enter according to the Index settings"));
        send(player,custom,SEEK);
    }

    /** 我的商品*/
    static void sendItems(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--My Items","");
        pItems items = pItems.getInstance(player.getName());
        LinkedList<iTypes> types = items.getAllItems();
        for(iTypes item:types){
            simple.addButton(getButton(item));
        }
        simple.setContent("Current Amount: "+simple.getButtons().size());
        send(player,simple,CHOSE);
    }

    /** 商品分类*/
    static void sendTypes(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--classify","");
        for(String type:sMarket.sType.keySet()){
            simple.addButton(new ElementButton(type,getImage(sMarket.sType.get(type))));
        }
        send(player,simple,TYPES);
    }

    /** 背包选择*/
    public static void sendAddInventory(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Sold Item Selection","");
        LinkedList<Item> items = new LinkedList<>(player.getInventory().getContents().values());
        for (Item item:items){
            ElementButton button = new ElementButton(ItemIDSunName.getIDByName(item)+" §7("+item.getId()+":"+item.getDamage()+")"+" * §a"+item.getCount());
            button.addImage(new ElementButtonImageData("path",ItemIDSunName.getIDByPath(item)));
            simple.addButton(button);
        }
        sMarket.invItems.put(player,items);
        send(player,simple,ADD_INVENTORY);
    }

    /** 上架商品*/
    public static void sendAddSetting(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--上架");
        Item hand = sMarket.handItem.get(player);
        String message = "§l§aItems on the market: §r"+ ItemIDSunName.getIDByName(hand)+" §7("+hand.getId()+":"+hand.getDamage()+")§r"+"\n\n"+
                         "§l§aCurrent Amount: §r§4"+hand.getCount();
        custom.addElement(new ElementLabel(message));
        //分类
        int diy = 0;
        if(hand.isTool() || hand.isArmor()){
            diy = 1;
        }
        custom.addElement(new ElementDropdown("§l§eItem classify",Tools.toList(sMarket.sType.keySet().toArray()),diy));
        custom.addElement(new ElementInput("§l§ePlease enter description","Please enter contents","On Sale!"));
        custom.addElement(new ElementInput("§l§ePlease set single item price","Please enter a number","10.0"));
        LinkedList<String> strings = new LinkedList<>();
        for(int i = 0;i <= hand.count;i++){
            if(i == 0){
                strings.add("§cRefused to be put on market");
            }else if(i == hand.count){
                strings.add("§aPut all on to market");
            }else{
                strings.add("§bPut on market"+i);
            }
        }
        custom.addElement(new ElementStepSlider("§l§ePlease set the amount that will be on the market",strings,hand.count));
        send(player,custom,UPDATA);
    }

    public static void sendBill(Player player,String s){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--Bill","");
        simple.setContent(s);
        send(player,simple,BILL);
    }

    private static ElementButtonImageData getImage(String path){
        String type = "path";
        if(path.split("@").length > 1){
            type = path.split("@")[1];
            path = path.split("@")[0];
        }
        return new ElementButtonImageData(type,path);
    }

    private static void send(Player player, FormWindow window, int id){
        player.showFormWindow(window,id);
    }
}
