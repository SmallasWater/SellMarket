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
import market.utils.ItemIDSunName;
import market.utils.Tools;
import market.utils.seekSetting;

import java.util.LinkedList;

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
                    }
                    switch (Integer.parseInt(data)){
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
                            default:break;
                    }
                    break;
                    case create.CHOSE:
                        if("null".equals(data)) {
                            return;
                        }
                        pItems items = pItems.getInstance(player.getName());
                        LinkedList<iTypes> types = items.getAllItems();
                        sMarket.clickItem.put(player,types.get(Integer.parseInt(data)));
                        create.sendSetting(player);
                        break;
                    case create.SEEK:
                        if("null".equals(data)) {
                            return;
                        }
                        Object[] datas = Tools.decodeData(data);
                        if(datas == null || datas.length < 1){
                            return;
                        }
                        int setting = (int)(double) datas[1];
                        boolean sqrt = (boolean) datas[2];
                        String seek = (String) datas[3];
                        sMarket.seekSetting.put(player,new seekSetting(seek,setting,sqrt));
                        create.sendSeekShow(player);
                        break;
                    case create.TYPES:
                        if("null".equals(data)) {
                            return;
                        }
                        sMarket.clickPos.put(player,Tools.getTypeByInt(Integer.parseInt(data)));
                        create.sendTypeShow(player);
                         break;
                    case create.SEEK_MENU:
                        if("null".equals(data)) {
                            return;
                        }
                        iTypes item = sMarket.seekItem.get(player).get(Integer.parseInt(data));
                        sMarket.clickItem.put(player,item);
                        if(item.getMaster().equals(player.getName())){
                            create.sendSetting(player);
                        }else{
                            create.sendBuyMenu(player);
                        }

                        break;
                    case create.SETTING:
                        if("null".equals(data)) {
                            return;
                        }
                        iTypes it = sMarket.clickItem.get(player);
                        datas = Tools.decodeData(data);
                        setting = (int)(double) datas[1];
                        String nameShow = (String) datas[2];
                        String text = (String) datas[3];
                        double money;
                        try {
                            money = Double.parseDouble((String) datas[4]);

                        }catch (Exception e){
                            player.sendMessage(sMarket.PLUGIN_NAME+"§c请输入正确的价格!!");
                            return;
                        }
                        int counts = (int)(double) datas[5];
                        iTypes types1 = new iTypes(it.master,Tools.getTypeByInt(setting),it.id,(counts),money,it.tag,text);
                        types1.setShowName(nameShow);
                        PlayerSettingItemEvent settingItemEvent =
                                new PlayerSettingItemEvent(player,types1);
                        Server.getInstance().getPluginManager().callEvent(settingItemEvent);

                        break;
                    case create.BUY_MENU:
                        if("null".equals(data)) {
                            return;
                        }
                        datas = Tools.decodeData(data);
                        iTypes its = sMarket.clickItem.get(player);
                        iTypes i = its.clone();
                        i.setCount((int)(double) datas[1]);
                        PlayerBuyItemEvent event1 = new PlayerBuyItemEvent(player,i);
                        Server.getInstance().getPluginManager().callEvent(event1);
                        break;
                    case create.TYPES_SHOW:
                        if("null".equals(data)) {
                            return;
                        }
                        item = Tools.getItemsByType(sMarket.clickPos.get(player)).get(Integer.parseInt(data));
                        sMarket.clickItem.put(player,item);
                        if(item.getMaster().equals(player.getName())){
                            create.sendSetting(player);
                        }else{
                            create.sendBuyMenu(player);
                        }

                        break;
                    case create.UPDATA:
                        if("null".equals(data)) {
                            return;
                        }
                        Item click = sMarket.handItem.get(player);
                        String id = click.getId()+":"+click.getDamage();
                        datas = Tools.decodeData(data);
                        if(datas == null || datas.length < 1){
                            return;
                        }
                        int chose = (int)(double) datas[1];
                        String message = (String) datas[2];
                        double d;
                        try {
                            d = Double.parseDouble((String) datas[3]);
                        }catch (Exception e){
                            player.sendMessage(sMarket.PLUGIN_NAME+"§c请输入正确的价格!!");
                            return;
                        }
                        if(d > sMarket.getApi().getMaxMoney() && d < sMarket.getApi().getMinMoney()){
                            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                                    ItemIDSunName.getIDByName(click)+"§c的单价由不能超过"+sMarket.getApi().getMaxMoney()+"或 小于"+sMarket.getApi().getMinMoney());
                            return;
                        }
                        int count = (int)(double) datas[4];
                        if(count == 0){
                            player.sendMessage(sMarket.PLUGIN_NAME+"§v你已取消上架");
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

                        break;
                    default:break;
            }
        }
    }

}
