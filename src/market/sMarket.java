package market;

import cn.nukkit.Achievement;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import market.load.loadMoney;
import market.load.playerChangeEvent;
import market.player.iTypes;
import market.player.pItems;
import market.player.sType;
import market.utils.Tools;
import market.from.*;
import market.utils.banItem;
import market.utils.seekSetting;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;


public class sMarket extends PluginBase {

    public static final String PLUGIN_NAME = "§7[§cS§6e§el§al§bM§9a§dr§ck§6e§et§7]";

    private static sMarket api;

    public Config config;

    private Config black;
    //黑名单

    public static LinkedHashMap<String, pItems> playerItems = new LinkedHashMap<>();

    public static LinkedHashMap<Player, Item> handItem = new LinkedHashMap<>();

    public static LinkedList<banItem> banItems = new LinkedList<>();

    public static LinkedHashMap<Player, iTypes> clickItem = new LinkedHashMap<>();

    public static LinkedHashMap<Player, sType> clickPos = new LinkedHashMap<>();

    public static LinkedHashMap<Player,LinkedList<iTypes>> seekItem = new LinkedHashMap<>();

    public static LinkedHashMap<Player, seekSetting> seekSetting = new LinkedHashMap<>();

    public static loadMoney money;


    @Override
    public void onEnable() {
        api = this;
        this.getLogger().info(PLUGIN_NAME+"启动成功");
        saveDefaultConfig();
        saveBlack();
        reloadConfig();
        File file = new File(this.getDataFolder()+"/Players");
        if(!file.exists()){
            if(!file.mkdirs()){
                this.getLogger().info(PLUGIN_NAME+"创建/Players/文件夹失败");
            }
        }
        this.config = getConfig();
        this.black = new Config(this.getDataFolder()+"/blackItems.yml",Config.YAML);
        //缓存
        this.getLogger().info(PLUGIN_NAME+"初始化..玩家商店");

        long t1 = System.currentTimeMillis();

        money = new loadMoney();

        playerItems = Tools.getPlayerConfigs();

        long t2 = System.currentTimeMillis();

        this.getLogger().info(PLUGIN_NAME+"玩家商店初始化完成 用时:"+((t2 - t1) % (1000 * 60))+"ms");

        for(String s:black.getStringList("封禁物品")){
            banItems.add(banItem.toItem(s));
        }
        //初始化成就
        Achievement.add("SellMarket",new Achievement("§d更多的商店"));
        Achievement.add("AddItem",new Achievement("§b第一件商品!"));
        Achievement.add("BuyItem",new Achievement("§d交易第一单!"));
        Achievement.add("setItem",new Achievement("§c不合理的商品"));

        this.getServer().getPluginManager().registerEvents(new playerChangeEvent(),this);
        this.getServer().getPluginManager().registerEvents(new listener(),this);

    }

    public double getMinMoney(){
        return (double) this.config.getInt("单价最小值");
    }

    public double getMaxMoney(){
        return (double) this.config.getInt("单价最大值");
    }

    public double getMaxCount(){
        return (double) this.config.getInt("玩家上架限制");
    }

    private void saveBlack(){
        if(!new File(this.getDataFolder()+"/blackItems.yml").exists()){
            saveResource("blackItems.yml",false);
        }
    }

    public File getPlayerFile(String playerName){
        return new File(this.getDataFolder()+"/Players/"+playerName+".yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if("sm".equals(command.getName()) || "交易".equals(command.getName())){
            if(args.length > 0){
                switch (args[0]){
                    case "help":
                    case "帮助":
                        sender.sendMessage("§l§6=========="+PLUGIN_NAME+"§6==========");
                        sender.sendMessage("§b/sm §ahelp §7查看帮助");
                        sender.sendMessage("§b/sm §aadd §7添加手持物品到市场");
                        if(sender.isOp()){
                            sender.sendMessage("§b/sm §ablack §7添加/删除手持物品到黑名单");
                        }
                        sender.sendMessage("§l§6================================");
                        break;
                    case "添加":
                    case "add":
                        if(sender instanceof Player){
                            banItem item = new banItem(((Player) sender).getInventory().getItemInHand());
                            if(item.getItem().getId() != 0){
                                if(Tools.inArray(item,banItems)){
                                    sender.sendMessage(PLUGIN_NAME+"§c此物品在黑名单!");
                                    return true;
                                }
                                handItem.put((Player) sender,item.getItem());
                                create.sendAddSetting((Player) sender);
                            }else {
                                sender.sendMessage(PLUGIN_NAME+"§c请手持物品");
                            }

                        }else{
                            sender.sendMessage("请在游戏内执行");
                        }
                        break;
                    case "黑名单":
                    case "black":
                    case "ab":
                    case "添加黑名单":
                    case "rb":
                    case "移除黑名单":
                        if(sender instanceof Player){
                            if(!sender.isOp()){
                                return false;
                            }
                            banItem item = banItem.get(((Player) sender).getInventory().getItemInHand());

                            if(Tools.inArray(item,banItems)){
                                if("ab".equals(args[0]) || "添加黑名单".equals(args[0])){
                                    sender.sendMessage(PLUGIN_NAME+"§c"+item.getName()+"存在列表");
                                    break;
                                }
                                LinkedList<banItem> items = new LinkedList<>();
                                for(banItem item1:banItems){
                                    if(item1.equals(item)){
                                        continue;
                                    }
                                    items.add(item1);
                                }
                                banItems = items;
                                sender.sendMessage(PLUGIN_NAME+"§a"+item.getName()+"移除成功!");
                            }else{
                                if("删除黑名单".equals(args[0]) || "rb".equals(args[0])){
                                    sender.sendMessage(PLUGIN_NAME+"§c"+item.getName()+"不在列表..");
                                    break;
                                }
                                banItems.add(item);
                                Server.getInstance().getLogger().info("blackItems:"+banItems.toString());
                                sender.sendMessage(PLUGIN_NAME+"§a"+item.getName()+"添加成功!");
                            }
                            LinkedList<String> bans = new LinkedList<>();
                            for(banItem ban:banItems){
                                bans.add(ban.toString());
                            }
                            black.set("封禁物品",bans);
                            black.save();

                        }else{
                            sender.sendMessage(PLUGIN_NAME+"§c(请不要用控制台执行)");
                        }
                        break;
                        default:return false;
                }
            }else {
                if(sender instanceof Player){
                    create.sendMenu((Player) sender);
                }else{
                    return false;
                }
            }
        }
        return true;
    }

    public static sMarket getApi() {
        return api;
    }
}
