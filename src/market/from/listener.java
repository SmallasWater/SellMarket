package market.from;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ModalFormResponsePacket;
import market.events.PlayerBuyItemEvent;
import market.events.PlayerSettingItemEvent;
import market.events.PlayerUpperItemEvent;
import market.sMarket;
import market.player.iTypes;
import market.player.pItems;
import market.utils.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import static market.sMarket.PLUGIN_NAME;

public class listener implements Listener {

    @EventHandler
    public void getUI(DataPacketReceiveEvent event){
        String data;
        ModalFormResponsePacket ui;
        Player player = event.getPlayer();
        if((event.getPacket() instanceof ModalFormResponsePacket)){
            ui = (ModalFormResponsePacket)event.getPacket();
            data = ui.data.trim();
            int fromId = ui.formId;
            switch (fromId){
                case create.MENU:
                    if("null".equals(data)){
                        return;
                    }else {
                        int uiData = Integer.parseInt(data);
                        if(sMarket.menu.get("物品市场") instanceof Map) {
                            sendMenu(player,uiData);
                        }else{
                            switch (Integer.parseInt(data)) {
                                case 0:
                                    create.sendTypes(player);
                                    break;
                                case 1:
                                    create.sendItems(player);
                                    break;
                                case 2:
                                    create.sendSeekItems(player);
                                    break;
                                case 3:
                                    create.sendBlack(player);
                                    break;
                                case 4:
                                    create.sendMessage(player);
                                    break;
                                default:
                                    break;
                            }
                        }

                    }
                    break;
                    case create.CHOSE:
                        if("null".equals(data)) {
                            create.sendMenu(player);
                            return;
                        }else{
                            pItems items = pItems.getInstance(player.getName());
                            LinkedList<iTypes> types = items.getAllItems();
                            sMarket.clickItem.put(player,types.get(Integer.parseInt(data)));
                            create.sendSetting(player);
                            return;
                        }

                    case create.SEEK:
                        if("null".equals(data)) {
                            create.sendMenu(player);
                            return;
                        }else{
                            Object[] datas = Tools.decodeData(data);
                            if(datas == null || datas.length < 1){
                                return;
                            }
                            int setting = (int)(double) datas[1];
                            boolean sqrt = (boolean) datas[2];
                            String seek = (String) datas[3];
                            sMarket.seekSetting.put(player,new seekSetting(seek,setting,sqrt));
                            create.sendSeekShow(player);
                            return;
                        }
                    case create.TYPES:
                        if("null".equals(data)) {
                            create.sendMenu(player);
                            return;
                        }else{
                            sMarket.clickPos.put(player,Tools.getTypeByInt(Integer.parseInt(data)));
                            create.sendTypeShow(player);
                            return;
                        }
                    case create.SEEK_MENU:
                        if("null".equals(data)) {
                            create.sendSeekItems(player);
                            return;
                        }else{
                            iTypes item = sMarket.seekItem.get(player).get(Integer.parseInt(data));
                            sMarket.clickItem.put(player,item);
                            String master = item.getMaster();
                            if(pItems.getInstance(master).inArray(item) != null){
                                if(item.getMaster().equals(player.getName())){
                                    create.sendSetting(player);
                                    return;
                                }else{
                                    create.sendBuyMenu(player);
                                    return;
                                }
                            }else{
                                player.sendMessage(PLUGIN_NAME+"§c"+item.getName()+"物品不在了");
                            }
                        }
                        break;
                    case create.SETTING:
                        if("null".equals(data)) {
                            create.sendMenu(player);
                            return;
                        }else{
                            iTypes it = sMarket.clickItem.get(player);
                            Object[] datas = Tools.decodeData(data);
                            int setting = (int)(double) datas[1];
                            String nameShow = (String) datas[2];
                            String text = (String) datas[3];
                            double money;
                            try {
                                money = Double.parseDouble((String) datas[4]);

                            }catch (Exception e){
                                player.sendMessage(PLUGIN_NAME+"§c请输入正确的价格!!");
                                return;
                            }
                            int counts = (int)(double) datas[5];
                            iTypes types1 = new iTypes(it.master,Tools.getTypeByInt(setting),it.id,(counts),money,it.tag,text);
                            types1.setShowName(nameShow);
                            PlayerSettingItemEvent settingItemEvent =
                                    new PlayerSettingItemEvent(player,types1);
                            Server.getInstance().getPluginManager().callEvent(settingItemEvent);
                            return;
                        }
                    case create.BUY_MENU:
                        if("null".equals(data)) {
                            return;
                        }else{
                            Object[] datas = Tools.decodeData(data);
                            iTypes its = sMarket.clickItem.get(player);
                            iTypes i = its.clone();
                            i.setCount((int)(double) datas[1]);
                            Bill buy = new Bill(new Date()
                                    ,i.master
                                    ,ItemIDSunName.getIDByName(i.getItem())
                                    ,((int)(i.count * i.money))
                                    ,i.getItem().getCount()
                                    ,Bill.BUY
                                    ,"§c交易异常 (物品遗失)");
                            Bill sell = new Bill(new Date()
                                    ,player.getName()
                                    ,ItemIDSunName.getIDByName(i.getItem())
                                    ,((int)(i.count * i.money))
                                    ,i.getItem().getCount()
                                    ,Bill.SELL
                                    ,"§c交易异常 (物品遗失)");
                            PlayerBuyItemEvent event1 = new PlayerBuyItemEvent(player,i,buy,sell);
                            Server.getInstance().getPluginManager().callEvent(event1);
                        }
                        return;
                    case create.TYPES_SHOW:
                        if("null".equals(data)) {
                            return;
                        }else{
                            try{
                                iTypes item = Tools.getItemsByType(sMarket.clickPos.get(player)).get(Integer.parseInt(data));
                                sMarket.clickItem.put(player,item);
                                if(item.getMaster().equals(player.getName())){
                                    create.sendSetting(player);
                                    return;
                                }else{
                                    create.sendBuyMenu(player);
                                    return;
                                }
                            }catch (Exception e){
                                player.sendMessage(PLUGIN_NAME+"§c这个物品不在了");
                                return;
                            }
                        }
                    case create.UPDATA:
                        if("null".equals(data)) {
                            return;
                        }else{
                            Item click = sMarket.handItem.get(player);
                            String id = click.getId()+":"+click.getDamage();
                            Object[] datas = Tools.decodeData(data);
                            if(datas == null || datas.length < 1){
                                return;
                            }
                            int chose = (int)(double) datas[1];
                            String message = (String) datas[2];
                            double d;
                            try {
                                d = Double.parseDouble((String) datas[3]);
                            }catch (Exception e){
                                player.sendMessage(PLUGIN_NAME+"§c请输入正确的价格!!");
                                return;
                            }
                            if(d > sMarket.getApi().getMaxMoney() || d < sMarket.getApi().getMinMoney()){
                                player.sendMessage(PLUGIN_NAME+"§e"+"§a"+
                                        ItemIDSunName.getIDByName(click)+"§c的单价由不能超过"+sMarket.getApi().getMaxMoney()+"或 小于"+sMarket.getApi().getMinMoney());
                                return;
                            }
                            int count = (int)(double) datas[4];
                            if(count == 0){
                                player.sendMessage(PLUGIN_NAME+"§c你已取消上架");
                                return;
                            }
                            String tag = "";
                            if(click.hasCompoundTag()){
                                tag = Tools.bytesToHexString(click.getCompoundTag());
                            }
                            PlayerUpperItemEvent upperItemEvent =
                                    new PlayerUpperItemEvent(player,
                                            new iTypes(player.getName(),Tools.getTypeByInt(chose),id,count,d,tag,message));
                            Server.getInstance().getPluginManager().callEvent(upperItemEvent);
                            return;
                        }
                case create.ADD_INVENTORY:
                    if("null".equals(data)) {
                        return;
                    }else{
                        try {
                            if(sMarket.invItems.containsKey(player)){

                                Item item = sMarket.invItems.get(player).get(Integer.parseInt(data));
                                banItem ban = new banItem(item);
                                if(player.getInventory().contains(item)){
                                    if(Tools.inArray(ban,sMarket.banItems)){
                                        player.sendMessage(PLUGIN_NAME+"§c此物品在黑名单!");
                                        return ;
                                    }
                                    sMarket.handItem.put(player,item);
                                    create.sendAddSetting(player);
                                    return ;
                                }else{
                                    player.sendMessage(PLUGIN_NAME+"§c物品不在了");
                                }
                            }else{
                                player.sendMessage(PLUGIN_NAME+"§c背包数据异常");
                            }
                        }catch (Exception e){
                            player.sendMessage(PLUGIN_NAME+"§c操作异常");
                        }

                    }
                    return;
                    default:break;
            }
        }
    }

    private void sendMenu(Player player,int data){
        LinkedList<String> lists = new LinkedList<>(sMarket.menu.keySet());
        String name = lists.get(data);
        if("物品市场".equals(name)){
            create.sendTypes(player);
        }else if("我的商品".equals(name)){
            create.sendItems(player);
        }else if("查找商品".equals(name)){
            create.sendSeekItems(player);
        }else if("黑名单物品".equals(name)){
            create.sendBlack(player);
        }else if("市场信息".equals(name)){
            create.sendMessage(player);
        }else if("账单".equals(name)){
            LinkedList<Bill> bills = sMarket.getPlayerBills(player.getName());
            sMarket.sendBill(player, bills);
        }else if("背包".equals(name)){
            if(player.getGamemode() == 1 && !player.isOp()){
                player.sendMessage(PLUGIN_NAME+"§c创造模式无法上架物品");
            }
            if(sMarket.getApi().isBlack(player.getName())){
                player.sendMessage(PLUGIN_NAME+"§c抱歉，您被管理员拉黑了 无法上架物品 如想继续上架请联系管理员解除限制");
            }
            create.sendAddInventory(player);
        }
    }
}
