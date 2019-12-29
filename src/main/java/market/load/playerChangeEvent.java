package market.load;


import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import market.events.PlayerBuyItemEvent;
import market.events.PlayerRemoveItemEvent;
import market.events.PlayerSettingItemEvent;
import market.events.PlayerUpperItemEvent;
import market.player.iTypes;
import market.player.pItems;
import market.utils.Bill;
import market.utils.ItemIDSunName;
import market.sMarket;
import java.io.File;

public class playerChangeEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        File file = sMarket.getApi().getPlayerFile(player.getName());
        if(!file.exists()){
            Config config = new Config(file,Config.YAML);
            pItems items = new pItems(player.getName(),config);
            items.save();
            player.awardAchievement("SellMarket");
            sMarket.playerItems.put(player.getName(),items);
        }
    }
    @EventHandler
    public void onUpData(PlayerUpperItemEvent event){
        Player player = event.getPlayer();
        pItems items = pItems.getInstance(player.getName());
        iTypes newI = event.getTypes();
        Item newItem = newI.getItem();
        iTypes old = items.inArray(newI);
        if(sMarket.getApi().isAdminMenu(newI.getType())){
            if(!player.isOp() && !sMarket.getApi().isAdmin(player.getName())){
                player.awardAchievement("admins");
                player.sendMessage(sMarket.PLUGIN_NAME+"§cSorry, this class can only be changed by Admin");
                return;
            }
        }
        if(old != null){
            if(old.message.equals(newI.message)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7's description has changed to §a"+newI.message);
            }
            if(old.money != newI.money){
                if(newI.money > sMarket.getApi().getMaxMoney() || newI.money < sMarket.getApi().getMinMoney()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(newI.getItem())+"§c's sinle price can't be higher than"+sMarket.getApi().getMaxMoney()+"or lower than"+sMarket.getApi().getMinMoney());
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7's single price has changed from §a"+old.money+"§b to §a"+newI.money);
            }
            if(old.count + newI.count > sMarket.getApi().getCountMax()){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§c's amount can't be higher than §a "+sMarket.getApi().getCountMax()+" §c  Current amount: §6"+old.count);
                return;

            }else{
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7's single price has changed from §a"+old.count+"§b to §a"+(old.count+newI.count));
            }

        }else{
            if(items.getSellItems(newI.type).size() >= sMarket.getApi().getMaxCount()){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§cItem amount can't be higher than "+sMarket.getApi().getMaxCount());
                return;
            }
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(newI.getItem())+"§aAdd to market__§eSingle item price: §7"+newI.money+" §eAmount: §7"+newI.count);
        }


        player.getInventory().removeItem(newItem);
        items.addSellItem(newI);
        player.awardAchievement("AddItem");

        items.save();
    }

    @EventHandler
    public void onRemove(PlayerRemoveItemEvent event){
        Player player = event.getPlayer();
        pItems items = pItems.getInstance(player.getName());
        iTypes newI = event.getTypes();
        Item newItem = newI.getItem();

        iTypes old = items.inArray(newI);
        if(old != null){
            if(old.count > newI.count){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7's amount reduce"+(old.count - newI.count));
            }else if(old.count == newI.count){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§7Removed from market already");
            }else{
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(newI.getItem())+"§c's amount isn't enough "+newI.count);
                return;
            }
            newItem.setCount(old.count - newI.count);
            newI.setCount(old.count - newI.count);
            player.getInventory().addItem(newItem);
            items.removeSellItem(newI);
            items.save();
        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"Sorry~§a"+
                    ItemIDSunName.getIDByName(newI.getItem())+"§cDon't exist");
        }
    }

    @EventHandler
    public void onSetting(PlayerSettingItemEvent event){
        Player player = event.getPlayer();
        if(sMarket.getApi().isBlack(player.getName())){
            player.sendMessage(sMarket.PLUGIN_NAME+"§cSorry, you have been blacklisted by the administrator and cannot put items on market. If you want to continue to sell items, please contact the administrator to remove the restriction.");
            return;
        }
        iTypes types = event.getTypes();
        iTypes clicks = sMarket.clickItem.get(player);
        player.awardAchievement("setItem");
        pItems items = pItems.getInstance(player.getName());
        String type = types.type;
        types.setType(clicks.type);
        iTypes old = items.inArray(clicks);
        if(old != null){
            if(old.money != types.money){
                if(types.money > sMarket.getApi().getMaxMoney() || types.money < sMarket.getApi().getMinMoney()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(types.getItem())+"§c's single price can't be higher than "+sMarket.getApi().getMaxMoney()+"or lower than "+sMarket.getApi().getMinMoney());
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d's single price has changed to : "
                        +(sMarket.money.getMonetaryUnit())+types.money);
            }
            if(!old.message.equals(types.message)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d's description has changed to : "
                        +types.message);
            }
            if(!old.showName.equals(types.showName)){
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d's name has changed to : "
                        +types.showName);
            }
            if(old.count != types.count){
                PlayerRemoveItemEvent event1 = new PlayerRemoveItemEvent(player,types);
                Server.getInstance().getPluginManager().callEvent(event1);
            }

            if(!old.type.equals(type)){
                if(items.getSellItems(type).size() == sMarket.getApi().getMaxCount()){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§c item amount on the market can't be higher than "+sMarket.getApi().getMaxCount());
                    return;
                }
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                        ItemIDSunName.getIDByName(types.getItem())+"§d's class has changed to : "
                        +type);
                int count = old.getCount();
                items.removeSellItem(old);
                types.setCount(count);
                types.setType(type);
                items.addSellItem(types);
            }
            items.setSellItem(types);
            items.save();
        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(types.getItem())+"§c Don't exist");
        }
    }

    @EventHandler
    public void onBuy(PlayerBuyItemEvent event){
        Player player = event.getPlayer();
        iTypes iType = event.getTypes();
        pItems items = pItems.getInstance(iType.master);
        Item newItem = iType.getItem();
        Bill buyer = event.getBuyer();
        Bill seller = event.getSeller();
        if(items.inArray(iType) != null){
            iTypes old = items.inArray(iType);
            if(old.count >= iType.count){
                if(iType.count == 0){
                    player.sendMessage(sMarket.PLUGIN_NAME+"§cYou have quit buying");
                    return;
                }
                double money = (iType.count * iType.money);
                if(sMarket.money.myMoney(player) >= money){
                    sMarket.money.reduceMoney(player,money);
                    String sellType;
                    items.save();
                    Player master = Server.getInstance().getPlayer(old.master);
                    player.awardAchievement("BuyItem");
                    if(player.getInventory().canAddItem(newItem)){
                        if(sMarket.money.addMoney(old.master,money)){
                            player.getInventory().addItem(newItem);
                            sellType = "§aTransaction Finished";
                        }else{
                            sellType = "§cTransaction Error(Money add failed)";
                            sendError(event, player, buyer, seller, old, money, sellType, master);
                            return;
                        }
                    }else{
                        if(sMarket.money.addMoney(old.master,money)){
                            sMarket.money.reduceMoney(old.master,money);
                            sellType = "§cTransaction Error(Item give failed)";
                            sendError(event, player, buyer, seller, old, money, sellType, master);
                            return;
                        }else{
                            sellType = "§cTransaction Error(Money add failed, Item give failed)";
                            sendError(event, player, buyer, seller, old, money, sellType, master);
                            return;
                        }
                    }
                    if(master != null){
                        master.sendMessage(sMarket.PLUGIN_NAME+"§e"+player.getName()+"bought your good §a"+
                                ItemIDSunName.getIDByName(newItem)+"§7gained "+sMarket.money.getMonetaryUnit()+money);
                    }
                    if(!sMarket.getApi().isAdmin(old.master)){
                        items.removeSellItem(iType);
                    }
                    buyer.setSellType(sellType);
                    seller.setSellType(sellType);
                    sMarket.addBile(player.getName(),buyer);
                    sMarket.addBile(old.master,seller);
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                            ItemIDSunName.getIDByName(newItem)+"§dTransaction Success!! Cost: "+(sMarket.money.getMonetaryUnit())+money);

                }else{
                    buyer.setSellType("§cTransaction Error (Not enough money)");
                    seller.setSellType("§cTransaction Error (Not enough money)");
                    sMarket.addBile(player.getName(),buyer);
                    sMarket.addBile(old.master,seller);
                    player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§cYour money isn't enough. Need"+(sMarket.money.getMonetaryUnit())+(money - sMarket.money.myMoney(player))+" more");
                }
            }else{
                buyer.setSellType("§cTransaction Error (Not enough goods)");
                seller.setSellType("§cTransaction Error (Not enough goods)");
                sMarket.addBile(player.getName(),buyer);
                sMarket.addBile(old.master,seller);
                player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§cSeller's goods aren't enough");

            }
        }else{
            player.sendMessage(sMarket.PLUGIN_NAME+"§e"+"§a"+
                    ItemIDSunName.getIDByName(newItem)+"§cNo more goods~~");
        }

    }

    private void sendError(PlayerBuyItemEvent event, Player player, Bill buyer, Bill seller, iTypes old, double money, String sellType, Player master) {
        sMarket.money.addMoney(player,money);
        event.setCancelled();
        master.sendMessage(sMarket.PLUGIN_NAME+"§cSorry, error dealing the transaction. For more information please type /sm bill , §eYour money has been returned");
        buyer.setSellType(sellType);
        seller.setSellType(sellType);
        sMarket.addBile(player.getName(),buyer);
        sMarket.addBile(old.master,seller);
    }

}
