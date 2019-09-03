package market.load;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerAchievementAwardedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import market.events.PlayerBuyItemEvent;
import market.events.PlayerRemoveItemEvent;
import market.events.PlayerSettingItemEvent;
import market.events.PlayerUpperItemEvent;
import market.player.iTypes;
import market.player.pItems;
import market.player.sType;
import market.utils.ItemIDSunName;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;

public class playerChangeEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        File file = sMarket.getApi().getPlayerFile(player.getName());
        if(!file.exists()){
            sMarket.getApi().saveResource("pData.yml","/Players/"+player.getName()+".yml",false);
            Config config = new Config(file,Config.YAML);
            sMarket.getApi().playerItems.put(player.getName(),new pItems(player.getName(),config));
            PlayerAchievementAwardedEvent event1 = new PlayerAchievementAwardedEvent(player,"§d更多的商店");
            Server.getInstance().getPluginManager().callEvent(event1);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        pItems items = sMarket.getApi().playerItems.get(player.getName());
        items.save();
    }

    @EventHandler
    public void onUpData(PlayerUpperItemEvent event){
        Player player = event.getPlayer();
        pItems items = pItems.getInstance(player.getName());
        iTypes newI = event.types;
        Item newItem = newI.getItem();
        iTypes old = items.inArray(newI);
        if(old != null){
            if(old.message.equals(newI.message)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7的介绍修改为§a"+newI.message);
            }
            if(old.money != newI.money){
                if(newI.money > sMarket.getApi().getMaxMoney() || newI.money < sMarket.getApi().getMinMoney()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(newI.getItem())+"§c的单价由不能超过"+sMarket.getApi().getMaxMoney()+"或 小于"+sMarket.getApi().getMinMoney());
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7的单价由§a"+old.money+"§b->§a"+newI.money);
            }
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(newI.getItem())+"§7的数量由§a"+old.count+"§b->§a"+(old.count+newI.count));
        }else{
            if(items.getSellItems(newI.type).size() >= sMarket.getApi().getMaxCount()){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§c上架商品不能超过"+sMarket.getApi().getMaxCount()+"个");
                return;
            }
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(newI.getItem())+"§a添加至市场__§e单价: §7"+newI.money+" §e数量: §7"+newI.count);
        }
        player.getInventory().removeItem(newItem);
        items.addSellItem(newI);
        items.save();
    }

    @EventHandler
    public void onRemove(PlayerRemoveItemEvent event){
        Player player = event.getPlayer();
        pItems items = pItems.getInstance(player.getName());
        iTypes newI = event.types;
        Item newItem = newI.getItem();

        iTypes old = items.inArray(newI);
        if(old != null){
            if(old.count > newI.count){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7的数量减少"+(old.count - newI.count));
            }else if(old.count == newI.count){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7已下架");
            }else{
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§c的数量不足 "+newI.count);
                return;
            }
            newItem.setCount(old.count - newI.count);
            newI.setCount(old.count - newI.count);
            player.getInventory().addItem(newItem);
            items.removeSellItem(newI);
            items.save();
        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"抱歉~§a"+
                    ItemIDSunName.getIDByName(newI.getItem())+"§c不存在");
        }
    }

    @EventHandler
    public void onSetting(PlayerSettingItemEvent event){
        Player player = event.getPlayer();
        iTypes types = event.types;
        iTypes clicks = sMarket.getApi().clickItem.get(player);
        pItems items = pItems.getInstance(player.getName());
        sType type = types.type;
        types.setType(clicks.type);
        iTypes old = items.inArray(clicks);
        if(old != null){
            if(old.money != types.money){
                if(types.money > sMarket.getApi().getMaxMoney() || types.money < sMarket.getApi().getMinMoney()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(types.getItem())+"§c的单价由不能超过"+sMarket.getApi().getMaxMoney()+"或 小于"+sMarket.getApi().getMinMoney());
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d的单价修改为: "
                        +(EconomyAPI.getInstance().getMonetaryUnit())+types.money);
            }
            if(!old.message.equals(types.message)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d的简介修改为: "
                        +types.message);
            }
            if(!old.showName.equals(types.showName)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d的名称修改为: "
                        +types.showName);
            }
            if(old.count != types.count){
                PlayerRemoveItemEvent event1 = new PlayerRemoveItemEvent(player,types);
                Server.getInstance().getPluginManager().callEvent(event1);
            }

            if(old.type != type){
                if(items.getSellItems(type).size() == sMarket.getApi().getMaxCount()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§c 上架数量不能超过"+sMarket.getApi().getMaxCount()+"个");
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d的分类修改为: "
                        +types.type.getName());
                items.removeSellItem(old);

                types.setCount(types.getCount());
                types.setType(type);
                items.addSellItem(types);
            }
            items.setSellItem(types);
            items.save();
        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(types.getItem())+"§c不存在");
        }
    }

    @EventHandler
    public void onBuy(PlayerBuyItemEvent event){
        Player player = event.getPlayer();
        iTypes iType = event.types;
        pItems items = pItems.getInstance(iType.master);
        Item newItem = iType.getItem();
        iTypes old = items.inArray(iType);
        if(old != null){
            if(old.count >= iType.count){
                double money = (iType.count * iType.money);
                if(EconomyAPI.getInstance().myMoney(player) >= money){
                    items.removeSellItem(iType);
                    EconomyAPI.getInstance().reduceMoney(player,money);
                    player.getInventory().addItem(newItem);
                    Player master = Server.getInstance().getPlayer(old.master);
                    if(master != null){
                        master.sendMessage(sMarket.PLUGIN_NAME+"§e"+player.getName()+"购买了你的 §a"+
                                ItemIDSunName.getIDByName(newItem)+"§7获得"+EconomyAPI.getInstance().getMonetaryUnit()+money);
                    }
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(newItem)+"§d购买成功!! 花费: ~"+(EconomyAPI.getInstance().getMonetaryUnit())+money);
                }else{
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§c你的金钱不够哦 还差"+(EconomyAPI.getInstance().getMonetaryUnit())+(EconomyAPI.getInstance().myMoney(player)-money));
                }
            }else{
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§c店家的货物没有这么多哦");

            }

        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(newItem)+"§c没有了哦~~");
        }

    }

}
