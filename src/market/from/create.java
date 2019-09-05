package market.from;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import market.load.sMarket;
import market.player.iTypes;
import market.player.pItems;
import market.player.sType;
import market.utils.ItemIDSunName;
import market.utils.Tools;
import market.utils.banItem;
import market.utils.seekSetting;


import java.util.LinkedHashMap;
import java.util.LinkedList;

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


    private static LinkedHashMap<String,String> menu = new LinkedHashMap<String, String>(){
        {
            put("物品市场","textures/ui/MashupIcon");
            put("我的商品","textures/ui/Friend2");
            put("查找商品","textures/ui/magnifyingGlass");
            put("黑名单物品","textures/blocks/barrier");
            put("市场信息","textures/ui/mute_off");
        }
    };

    /** 主页菜单*/
    public static void sendMenu(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--主页",
                sMarket.getApi().config.getString("自定义公告").replace("{换行}","\n"));
        for(String s:menu.keySet()){
            simple.addButton(new ElementButton(s,new ElementButtonImageData("path",menu.get(s))));
        }
        send(player,simple,MENU);
    }

    /** 玩家设置*/
    static void sendSetting(Player player){
        iTypes item = sMarket.clickItem.get(player);
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--编辑");
        String s = "§l§e商品名称: "+item.getName()+"\n\n"+
                  "§l§e商品数量: §a"+item.getCount()+"\n\n"+
                  "§l§e商品简介: §r"+item.getMessage()+"\n\n"+
                  "§l§e商品单价: §c"+(sMarket.money.getMonetaryUnit()+item.getMoney())+"\n";
        custom.addElement(new ElementLabel(s));
        custom.addElement(new ElementDropdown("修改分类",new LinkedList<String>(){
            {
                add("方块 (干垃圾)");
                add("工具 (可回收垃圾)");
                add("食物 (湿垃圾)");
                add("其他 (有害垃圾)");
            }
        },Tools.getIntByType(item.type)));
        custom.addElement(new ElementInput("显示名称","请修改名称",item.getName()));
        custom.addElement(new ElementInput("修改简介","请输入内容",item.message));
        custom.addElement(new ElementInput("修改单价","请输入数值",String.valueOf(item.money)));
        LinkedList<String> list = new LinkedList<>();
        for(int i = 0;i <= item.getCount();i++){
            if(i == 0){
                list.add("§c下架物品");
            }else if(i == item.count){
                list.add("§a不改变物品");
            }else{
                list.add("§b减少"+(item.getCount()-i)+"个");
            }
        }

        custom.addElement(new ElementStepSlider("修改数量(如果想增加数量上传物品即可)",list,item.count));
        send(player,custom,SETTING);
    }

    /** 查找列表*/
    static void sendSeekShow(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--查询结果","");
        seekSetting setting = sMarket.seekSetting.get(player);
        LinkedList<iTypes> seekItems = new LinkedList<>();
        for(iTypes item:Tools.seekItemById(setting.message,setting.type,setting.sqrt)){
            seekItems.add(item);
            simple.addButton(getButton(item));
        }
        sMarket.seekItem.put(player,seekItems);
        simple.setContent("共查找到: "+simple.getButtons().size()+" 个  索引设置: "+Tools.getStringBySeekSetting(setting.type));
        send(player,simple,SEEK_MENU);
    }

    private static ElementButton getButton(iTypes item){
        ElementButtonImageData imageData = new ElementButtonImageData("path",ItemIDSunName.getIDByPath(item.getItem()));
        ElementButton button = new ElementButton(item.getName()+" §7("+item.getId()+")§r"+"\n单价: "
                +(sMarket.money.getMonetaryUnit())+(item.getMoney())+" 剩余:"+item.getCount()+"§e(点击购买)");
        button.addImage(imageData);
        return button;
    }

    /** 购买页面*/
    static void sendBuyMenu(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--购买");
        iTypes item = sMarket.clickItem.get(player);
        String s = "§l§e商品名称: "+item.getName()+"\n\n"+
                   "§l§e出售者: "+item.getMaster()+"\n\n"+
                   "§l§e是否含有NBT: "+(item.hasTag()?"§a是":"§c否")+"\n\n"+
                   "§l§e商品数量: §a"+item.getCount()+"\n\n"+
                   "§l§e商品简介: §r"+item.getMessage()+"\n\n"+
                   "§l§e商品单价: §c"+(sMarket.money.getMonetaryUnit()+item.getMoney())+"\n";
        custom.addElement(new ElementLabel(s));
        LinkedList<String> list = new LinkedList<>();
        for(int i = 0;i <= item.getCount();i++){
            if(i == 0){
                list.add("§c不买了");
            }else if(i == item.count){
                list.add("§a购买全部 预计花费 "+(sMarket.money.getMonetaryUnit())+(item.getCount() * item.getMoney()));
            }else{
                list.add("§b购买"+i+"个 预计花费 "+(sMarket.money.getMonetaryUnit())+(i * item.getMoney()));
            }

        }
        custom.addElement(new ElementStepSlider("购买数量",list,0));
        send(player,custom,BUY_MENU);
    }

    /** 黑名单*/
    static void sendBlack(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--黑名单","");
        if(sMarket.banItems.size() > 0){
            StringBuilder builder = new StringBuilder();
            for(banItem item:sMarket.banItems){
                builder.append("§r>> ").append(item.getName()).append("§7 (").append(item.getId()).append(")").append("\n");
            }
            simple.setContent(builder.toString());
        }else{
            simple.setContent("暂无");
        }
        send(player,simple,BLACK);
    }

    /** 信息*/
    static void sendMessage(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--商城信息","");
        StringBuilder builder = new StringBuilder();
        builder.append("§l§a注册用户: ");
        if(sMarket.playerItems.size() > 0){
            builder.append("\n");
            for(String name:sMarket.playerItems.keySet()){
                Player player1 = Server.getInstance().getPlayer(name);
                builder.append("§r>>  ").append(name).append(player1!= null?"§a[在线]":"§7[离线]").append("\n");
            }
        }else{
            builder.append(" §c暂无").append("\n");
        }
        builder.append("\n§l§a货物总数: §e").append(Tools.getItemsAll().size()).append(" §a个");
        builder.append("\n§l§a方块类: §e").append(Tools.getItemsByType(sType.Block).size()).append(" §a个");
        builder.append("\n§l§a工具类: §e").append(Tools.getItemsByType(sType.Tools).size()).append(" §a个");
        builder.append("\n§l§a食物类: §e").append(Tools.getItemsByType(sType.Food).size()).append(" §a个");
        builder.append("\n§l§a其他: §e").append(Tools.getItemsByType(sType.Author).size()).append(" §a个");
        simple.setContent(builder.toString());
        send(player,simple,MESSAGE);
    }

    /** 商品列表(类型)*/
    static void sendTypeShow(Player player){
        sType t = sMarket.clickPos.get(player);
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--市场("+t.getName()+")","");
        for(iTypes items: Tools.getItemsByType(t)){
            simple.addButton(getButton(items));
        }
        simple.setContent("当前页数量: "+simple.getButtons().size());
        send(player,simple,TYPES_SHOW);
    }

    /** 查找*/
    static void sendSeekItems(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--查找");
        custom.addElement(new ElementLabel("查找的选项不同会影响查找结果"));
        custom.addElement(new ElementDropdown("索引设置",Tools.strings,0));
        custom.addElement(new ElementToggle("自动排序",false));
        custom.addElement(new ElementInput("输入索引","请根据索引设置输入"));
        send(player,custom,SEEK);
    }

    /** 我的商品*/
    static void sendItems(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--我的商品","");
        pItems items = pItems.getInstance(player.getName());
        LinkedList<iTypes> types = items.getAllItems();
        for(iTypes item:types){
            simple.addButton(getButton(item));
        }
        simple.setContent("当前数量: "+simple.getButtons().size());
        send(player,simple,CHOSE);
    }

    /** 商品分类*/
    static void sendTypes(Player player){
        FormWindowSimple simple = new FormWindowSimple(sMarket.PLUGIN_NAME+"--分类","");
        for(sType type:sType.values()){
            simple.addButton(new ElementButton(type.getName(),new ElementButtonImageData("path",type.getImage())));
        }
        send(player,simple,TYPES);
    }

    /** 上架商品*/
    public static void sendAddSetting(Player player){
        FormWindowCustom custom = new FormWindowCustom(sMarket.PLUGIN_NAME+"--上架");
        Item hand = sMarket.handItem.get(player);
        String message = "§l§a上架物品: §r"+ ItemIDSunName.getIDByName(hand)+" §7("+hand.getId()+":"+hand.getDamage()+")§r"+"\n\n"+
                         "§l§a当前数量: §r§4"+hand.getCount();
        custom.addElement(new ElementLabel(message));
        //分类
        int diy = 0;
        if(hand.isTool() || hand.isArmor()){
            diy = 1;
        }
        custom.addElement(new ElementDropdown("§l§e物品分类",new LinkedList<String>(){
            {
                add("方块 (干垃圾)");
                add("工具 (可回收垃圾)");
                add("食物 (湿垃圾)");
                add("其他 (有害垃圾)");
            }
        },diy));
        custom.addElement(new ElementInput("§l§e请设置简介","请输入内容","很便宜哦"));
        custom.addElement(new ElementInput("§l§e请设置单价","请输入数值","10.0"));
        LinkedList<String> strings = new LinkedList<>();
        for(int i = 0;i <= hand.count;i++){
            if(i == 0){
                strings.add("§c拒绝上架");
            }else if(i == hand.count){
                strings.add("§a上架全部");
            }else{
                strings.add("§b上架"+i+"个");
            }
        }
        custom.addElement(new ElementStepSlider("§l§e请设置上架数量",strings,hand.count));
        send(player,custom,UPDATA);
    }


    private static void send(Player player, FormWindow window, int id){
        player.showFormWindow(window,id);
    }
}