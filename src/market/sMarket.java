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
import java.util.List;
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
            put("物品市场","textures/ui/MashupIcon");
            put("我的商品","textures/ui/Friend2");
            put("查找商品","textures/ui/magnifyingGlass");
            put("黑名单物品","textures/blocks/barrier");
            put("市场信息","textures/ui/mute_off");
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
                Server.getInstance().getLogger().info(PLUGIN_NAME+"创建/Players/文件夹失败");
            }
        }
        this.config = getConfig();
        sType = Tools.getType();
        this.black = new Config(this.getDataFolder()+"/blackItems.yml",Config.YAML);
        //缓存
        Server.getInstance().getLogger().info(PLUGIN_NAME+"初始化..玩家商店");
        long t1 = System.currentTimeMillis();
        money = new loadMoney();
        playerItems = Tools.getPlayerConfigs();
        LinkedHashMap<String,Object> map = loadMenu();
        if(map.size() > 0){
            menu = map;
        }else{
            menu = lastMenu;
        }
        if(!"".equals(config.getString("标题"))){
            PLUGIN_NAME = config.getString("标题");
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

        Server.getInstance().getLogger().info(PLUGIN_NAME+"玩家商店初始化完成 用时:"+((t2 - t1) % (1000 * 60))+"ms");

        for(String s:black.getStringList("封禁物品")){
            banItems.add(banItem.toItem(s));
        }
        //初始化成就
        Achievement.add("SellMarket",new Achievement("§d更多的商店"));
        Achievement.add("AddItem",new Achievement("§b第一件商品!"));
        Achievement.add("BuyItem",new Achievement("§d交易第一单!"));
        Achievement.add("admins",new Achievement("§l§e管理员了不起啊"));
        Achievement.add("setItem",new Achievement("§c不合理的商品"));

        this.getServer().getPluginManager().registerEvents(new playerChangeEvent(),this);
        this.getServer().getPluginManager().registerEvents(new listener(),this);

    }

    private boolean upData(){
        if(Server.getInstance().getPluginManager().getPlugin("AutoUpData") != null){
            UpData data = AutoData.get(this,this.getFile(),"SmallasWater","SellMarket");
            if(data != null){
                if(data.canUpdate()){
                    this.getLogger().info("检测到新版本 v"+data.getNewVersion());
                    String message = data.getNewVersionMessage();
                    for(String info : message.split("\\n")){
                        this.getLogger().info("更新内容: "+info);
                    }
                    if(!data.toUpData()){
                        this.getLogger().info("更新失败");
                    }else{
                        return false;
                    }
                }
            }else{
                this.getLogger().info("更新检查失败");
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

    private List<String> getAdmins() {
        return adminConfig.getStringList("ops");
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

    private List<String>getAdminsMenus() {
        return adminMenu.getStringList("AdminMenu");
    }

    public boolean isAdminMenu(String name){
        return getAdminsMenus().contains(name);
    }


    private void changeAdmin(String name){
        List<String> strings = getAdmins();
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
        Map map = (Map) config.get("主页");
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
        return this.config.getInt("物品数量最大值",120);
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
                    case "help": case "帮助":
                        sender.sendMessage("§l§6=========="+PLUGIN_NAME+"§6==========");
                        sender.sendMessage("§b/sm §ahelp §7查看帮助");
                        sender.sendMessage("§b/sm §aadd §7添加手持物品到市场");
                        sender.sendMessage("§b/sm §ainv §7打开背包物品列表GUI");
                        sender.sendMessage("§b/sm §abill §7查询近期的账单");
                        if(sender.isOp()){
                            sender.sendMessage("§b/sm §ablack §7添加/删除手持物品到黑名单");
                            sender.sendMessage("§b/sm §aban <玩家> §7将玩家加入黑名单");
                            sender.sendMessage("§b/sm §aadmin <玩家> §7添加/删除 无限物品玩家");
                        }
                        sender.sendMessage("§l§6================================");
                        break;
                    case "添加": case "add":
                        if(sender instanceof Player){
                            if(((Player) sender).getGamemode() == 1 && !sender.isOp()){
                                sender.sendMessage(PLUGIN_NAME+"§c创造模式无法上架物品");
                                return true;
                            }
                            banItem item = new banItem(((Player) sender).getInventory().getItemInHand());
                            if(item.getItem().getId() != 0){
                                if(Tools.inArray(item,banItems)){
                                    sender.sendMessage(PLUGIN_NAME+"§c此物品在黑名单!");
                                    return true;
                                }
                                if(isBlack(sender.getName())){
                                    sender.sendMessage(PLUGIN_NAME+"§c抱歉，您被管理员拉黑了 无法上架物品 如想继续上架请联系管理员解除限制");
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
                    case "ban": case "拉黑":
                        if(!sender.isOp()){
                            return false;
                        }
                        if(args.length > 1){
                            if(isBlack(args[1])){
                                sender.sendMessage(PLUGIN_NAME+"§e 移除黑名单玩家"+args[1]+"成功");
                            }else{
                                sender.sendMessage(PLUGIN_NAME+"§e 增加黑名单玩家"+args[1]+"成功");
                            }
                            changeBanPlayers(args[1]);
                        }else{
                            return false;
                        }

                        break;
                    case "inv": case "背包":
                        if(sender instanceof Player){
                            if(((Player) sender).getGamemode() == 1 && !sender.isOp()){
                                sender.sendMessage(PLUGIN_NAME+"§c创造模式无法上架物品");
                                return true;
                            }
                            if(isBlack(sender.getName())){
                                sender.sendMessage(PLUGIN_NAME+"§c抱歉，您被管理员拉黑了 无法上架物品 如想继续上架请联系管理员解除限制");
                                return true;
                            }
                            create.sendAddInventory((Player) sender);
                        }else{
                            return false;
                        }
                        break;
                    case "黑名单": case "black": case "ab": case "添加黑名单": case "rb": case "移除黑名单":
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
                    case "admin": case "管理":
                        if(sender.isOp()){
                            if(args.length > 1){
                                if(isAdmin(args[1])){
                                    sender.sendMessage(PLUGIN_NAME+"§e 移除管理者"+args[1]+"成功");
                                }else{
                                    sender.sendMessage(PLUGIN_NAME+"§e 增加管理者"+args[1]+"成功");
                                }
                                changeAdmin(args[1]);
                            }else{
                                return false;
                            }
                        }else{
                            return false;
                        }
                        break;
                    case "账单": case "bill":
                        if(sender instanceof Player){
                            LinkedList<Bill> bills = getPlayerBills(sender.getName());
                            sendBill(sender, bills);
                        }else{
                            sender.sendMessage("控制台没有账单");
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
            sender.sendMessage(PLUGIN_NAME+"§7近期没有账单哦");
        }
    }

    public static sMarket getApi() {
        return api;
    }


}
