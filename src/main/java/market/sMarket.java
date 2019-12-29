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
import market.utils.*;
import market.from.*;
import updata.AutoData;
import updata.utils.UpData;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


public class sMarket extends PluginBase {

    public static String PLUGIN_NAME = "§7[§cS§6e§el§al§bM§9a§dr§ck§6e§et§7]";

    private static sMarket api;

    public Config config;

    private Config black;

    private Config adminConfig;

    private Config adminMenu;
    //黑名单

    public static LinkedHashMap<String, pItems> playerItems = new LinkedHashMap<>();

    public static LinkedHashMap<Player, Item> handItem = new LinkedHashMap<>();

    public static LinkedHashMap<Player, LinkedList<Item>> invItems = new LinkedHashMap<>();

    public static LinkedList<banItem> banItems = new LinkedList<>();

    public static LinkedHashMap<Player, iTypes> clickItem = new LinkedHashMap<>();

    public static LinkedHashMap<Player, String> clickPos = new LinkedHashMap<>();

    public static LinkedHashMap<Player,LinkedList<iTypes>> seekItem = new LinkedHashMap<>();

    public static LinkedHashMap<Player, seekSetting> seekSetting = new LinkedHashMap<>();

    public static LinkedHashMap<String,String> sType = new LinkedHashMap<>();

    private static LinkedHashMap<String, LinkedList<Bill>> playerBill = new LinkedHashMap<>();


    private static LinkedHashMap<String,Object> lastMenu = new LinkedHashMap<String, Object>(){
        {
            put("Item Market","textures/ui/MashupIcon");
            put("My Items","textures/ui/Friend2");
            put("Search Items","textures/ui/magnifyingGlass");
            put("Blacklisted Items","textures/blocks/barrier");
            put("Market Information","textures/ui/mute_off");
        }
    };

    public static LinkedHashMap<String,Object> menu = new LinkedHashMap<>();

    public static loadMoney money;




    @Override
    public void onEnable() {
        api = this;
        if(!upData()){
            return;
        }
        saveDefaultConfig();
        saveBlack();
        reloadConfig();
        File file = new File(this.getDataFolder()+"/Players");
        if(!file.exists()){
            if(!file.mkdirs()){
                Server.getInstance().getLogger().info(PLUGIN_NAME+"Create /Players/ Folder Failed");
            }
        }
        this.config = getConfig();
        sType = Tools.getType();
        this.black = new Config(this.getDataFolder()+"/blackItems.yml",Config.YAML);
        //缓存
        Server.getInstance().getLogger().info(PLUGIN_NAME+"Starting..Sell Market");
        long t1 = System.currentTimeMillis();
        money = new loadMoney();
        playerItems = Tools.getPlayerConfigs();
        LinkedHashMap<String,Object> map = loadMenu();
        if(map.size() > 0){
            menu = map;
        }else{
            menu = lastMenu;
        }
        if(!"".equals(config.getString("Title"))){
            PLUGIN_NAME = config.getString("Title");
        }
        File adminFile = new File(this.getDataFolder()+"/admins.yml");
        if(!adminFile.exists()){
            saveResource("admins.yml");
        }
        this.adminConfig = new Config(adminFile,Config.YAML);

        File adminMenuFile = new File(this.getDataFolder()+"/adminMenu.yml");
        if(!adminMenuFile.exists()){
            saveResource("adminMenu.yml");
        }
        this.adminMenu = new Config(adminMenuFile,Config.YAML);

        long t2 = System.currentTimeMillis();

        Server.getInstance().getLogger().info(PLUGIN_NAME+"SellMarket load complete, time used:"+((t2 - t1) % (1000 * 60))+"ms");

        for(String s:black.getStringList("Baned Item")){
            banItems.add(banItem.toItem(s));
        }
        //初始化成就
        Achievement.add("SellMarket",new Achievement("§More Shop"));
        Achievement.add("AddItem",new Achievement("§bFirest Item!"));
        Achievement.add("BuyItem",new Achievement("§dFirest Deal!"));
        Achievement.add("admins",new Achievement("§l§eAdmin is excellent!"));
        Achievement.add("setItem",new Achievement("§cItem not suitable!"));

        this.getServer().getPluginManager().registerEvents(new playerChangeEvent(),this);
        this.getServer().getPluginManager().registerEvents(new listener(),this);

    }

    private boolean upData(){
        if(Server.getInstance().getPluginManager().getPlugin("AutoUpData") != null){
            UpData data = AutoData.get(this,this.getFile(),"SmallasWater","SellMarket");
            if(data != null){
                if(data.canUpdate()){
                    this.getLogger().info("New Version Detected v"+data.getNewVersion());
                    String message = data.getNewVersionMessage();
                    for(String info : message.split("\\n")){
                        this.getLogger().info("What is new:"+info);
                    }
                    if(!data.toUpData()){
                        this.getLogger().info("Update failed");
                    }else{
                        return false;
                    }
                }
            }else{
                this.getLogger().info("Update check failed");
            }
        }
        return true;
    }

    public static LinkedList<Bill>getPlayerBills(String playerName) {
        if(playerBill.containsKey(playerName)){
            return playerBill.get(playerName);
        }
        return new LinkedList<>();
    }

    private LinkedList<String>getAdmins() {
        return new LinkedList<>(adminConfig.getStringList("ops"));
    }

    private LinkedList<String>getBanPlayers() {
        return new LinkedList<>(adminMenu.getStringList("BanPlayers"));
    }

    public boolean isAdmin(String name){
        return getAdmins().contains(name);
    }

    public boolean isBlack(String name){
        return getBanPlayers().contains(name);
    }

    private LinkedList<String>getAdminsMenus() {
        return new LinkedList<>(adminMenu.getStringList("AdminMenu"));
    }

    public boolean isAdminMenu(String name){
        return getAdminsMenus().contains(name);
    }


    private void changeAdmin(String name){
        LinkedList<String> strings = getAdmins();
        if(strings.contains(name)){
            strings.remove(name);
        }else {
            strings.add(name);
        }
        adminConfig.set("ops",strings);
        adminConfig.save();
    }

    private void changeBanPlayers(String name){
        LinkedList<String> strings = getBanPlayers();
        if(strings.contains(name)){
            strings.remove(name);
        }else {
            strings.add(name);
        }
        adminMenu.set("BanPlayers",strings);
        adminMenu.save();
    }

    private LinkedHashMap<String,Object> loadMenu(){
        Map map = (Map) config.get("Home page");
        LinkedHashMap<String,Object> menus = new LinkedHashMap<>();
        if(map != null){
            for (Object o : map.keySet()){
                menus.put(o.toString(), map.get(o));
            }
        }
        return menus;
    }

    public static void addBile(String playerName,Bill bill){
        LinkedList<Bill> bills = getPlayerBills(playerName);
        bills.add(bill);
        setPlayerBills(playerName,bills);
    }

    private static void setPlayerBills(String playerName,LinkedList<Bill> playerBills) {
        playerBill.put(playerName,playerBills);
    }

    public int getCountMax(){
        return this.config.getInt("Item max amount",120);
    }

    public double getMinMoney(){
        return (double) this.config.getInt("Least Price for Single Item");
    }

    public double getMaxMoney(){
        return (double) this.config.getInt("Max Price for Single Item");
    }

    public double getMaxCount(){
        return (double) this.config.getInt("Player restrictions for number of items on the market");
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
        if("sm".equals(command.getName()) || "transaction".equals(command.getName())){
            if(args.length > 0){
                switch (args[0]){
                    case "help":
                        sender.sendMessage("§l§6=========="+PLUGIN_NAME+"§6==========");
                        sender.sendMessage("§b/sm §ahelp §7Help");
                        sender.sendMessage("§b/sm §aadd §7Adding handheld items to the market");
                        sender.sendMessage("§b/sm §ainv §7Open Backpack Item List GUI");
                        sender.sendMessage("§b/sm §abill §7Query recent bills");
                        if(sender.isOp()){
                            sender.sendMessage("§b/sm §ablack §7Add / remove handheld items to blacklist");
                            sender.sendMessage("§b/sm §aban <Player> §7Add Player to blacklist");
                            sender.sendMessage("§b/sm §aadmin <Player> §7Add / Remove Unlimited Items Player");
                        }
                        sender.sendMessage("§l§6================================");
                        break;
                    case "add":
                        if(sender instanceof Player){
                            if(((Player) sender).getGamemode() == 1 && !sender.isOp()){
                                sender.sendMessage(PLUGIN_NAME+"§cIn Creative Mode Cannot Add Items to List");
                                return true;
                            }
                            banItem item = new banItem(((Player) sender).getInventory().getItemInHand());
                            if(item.getItem().getId() != 0){
                                if(Tools.inArray(item,banItems)){
                                    sender.sendMessage(PLUGIN_NAME+"§cThis item is on the blacklist!");
                                    return true;
                                }
                                if(isBlack(sender.getName())){
                                    sender.sendMessage(PLUGIN_NAME+"§cSorry, you have been blacklisted by the administrator and cannot add items to the market. If you want to continue selling items, please contact the administrator to remove the restriction.");
                                    return true;
                                }
                                handItem.put((Player) sender,item.getItem());
                                create.sendAddSetting((Player) sender);
                            }else {
                                sender.sendMessage(PLUGIN_NAME+"§cPlease help items in hand");
                            }

                        }else{
                            sender.sendMessage("Please use the command in game");
                        }
                        break;
                    case "ban":
                        if(!sender.isOp()){
                            return false;
                        }
                        if(args.length > 1){
                            if(isBlack(args[1])){
                                sender.sendMessage(PLUGIN_NAME+"§e Remove blcaklisted Player"+args[1]+"Success");
                            }else{
                                sender.sendMessage(PLUGIN_NAME+"§e Add blacklisted Player"+args[1]+"Success");
                            }
                            changeBanPlayers(args[1]);
                        }else{
                            return false;
                        }

                        break;
                    case "inv":
                        if(sender instanceof Player){
                            if(((Player) sender).getGamemode() == 1 && !sender.isOp()){
                                sender.sendMessage(PLUGIN_NAME+"§cIn Creative Mode Cannot Add Items to List");
                                return true;
                            }
                            if(isBlack(sender.getName())){
                                sender.sendMessage(PLUGIN_NAME+"§cSorry, you have been blacklisted by the administrator and cannot add items to the market. If you want to continue selling items, please contact the administrator to remove the restriction.");
                                return true;
                            }
                            create.sendAddInventory((Player) sender);
                        }else{
                            return false;
                        }
                        break;
                    case "black": case "ab":case "rb":
                        if(sender instanceof Player){
                            if(!sender.isOp()){
                                return false;
                            }
                            banItem item = banItem.get(((Player) sender).getInventory().getItemInHand());

                            if(Tools.inArray(item,banItems)){
                                if("ab".equals(args[0]) || "Add to BlackList".equals(args[0])){
                                    sender.sendMessage(PLUGIN_NAME+"§c"+item.getName()+"List Exist");
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
                                sender.sendMessage(PLUGIN_NAME+"§a"+item.getName()+"Successfully Removed!");
                            }else{
                                if("rb".equals(args[0])){
                                    sender.sendMessage(PLUGIN_NAME+"§c"+item.getName()+"Not in the market..");
                                    break;
                                }
                                banItems.add(item);
                                Server.getInstance().getLogger().info("blackItems:"+banItems.toString());
                                sender.sendMessage(PLUGIN_NAME+"§a"+item.getName()+"Added Successfully!");
                            }
                            LinkedList<String> bans = new LinkedList<>();
                            for(banItem ban:banItems){
                                bans.add(ban.toString());
                            }
                            black.set("Baned Item",bans);
                            black.save();

                        }else{
                            sender.sendMessage(PLUGIN_NAME+"§c(Please don't use this in console)");
                        }
                        break;
                    case "admin":
                        if(sender.isOp()){
                            if(args.length > 1){
                                if(isAdmin(args[1])){
                                    sender.sendMessage(PLUGIN_NAME+"§e Remove Admin"+args[1]+"Success!");
                                }else{
                                    sender.sendMessage(PLUGIN_NAME+"§e Add Admin"+args[1]+"Success!");
                                }
                                changeAdmin(args[1]);
                            }else{
                                return false;
                            }
                        }else{
                            return false;
                        }
                        break;
                    case "bill":
                        if(sender instanceof Player){
                            LinkedList<Bill> bills = getPlayerBills(sender.getName());
                            sendBill(sender, bills);
                        }else{
                            sender.sendMessage("No bill in console");
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

    public static void sendBill(CommandSender sender, LinkedList<Bill> bills) {
        if(bills.size() > 0){
            StringBuilder builder = new StringBuilder();
            for (Bill bill:bills) {
                builder.append(bill.toString());
            }
            create.sendBill((Player) sender,builder.toString());
        }else{
            sender.sendMessage(PLUGIN_NAME+"§7No bills recently.");
        }
    }

    public static sMarket getApi() {
        return api;
    }


}
