<?php

namespace SellShop;
#development-cycle: #20181109
#author: 若水
#last-edit: 20181126
/*
 *   ____       _ _ ____  _
 / ___|  ___| | / ___|| |__   ___  _ __
 \___ \ / _ \ | \___ \| '_ \ / _ \| '_ \
  ___) |  __/ | |___) | | | | (_) | |_) |
 |____/ \___|_|_|____/|_| |_|\___/| .__/
                                  |_|
*/


use onebone\economyapi\EconomyAPI;
use pocketmine\command\Command;
use pocketmine\command\CommandSender;
use pocketmine\event\Listener;
use pocketmine\event\player\PlayerJoinEvent;
use pocketmine\event\server\DataPacketReceiveEvent;
use pocketmine\item\Item;
use pocketmine\item\ItemFactory;
use pocketmine\nbt\tag\CompoundTag;
use pocketmine\network\mcpe\protocol\ModalFormResponsePacket;
use pocketmine\Player;
use pocketmine\plugin\PluginBase;
use pocketmine\Server;
use pocketmine\utils\Config;
use pocketmine\utils\TextFormat;
use SellShop\Files\File__By_PlayerBuy_This;
use SellShop\Files\File_By_get_Player_Item_Config;
use SellShop\Files\File_Final;
use SellShop\Item_List\getItem_Name;
use SellShop\UI_API_IN_GET\sendUI;
use SellShop\UI_API_IN_GET\UIList;

class Main extends PluginBase implements Listener
{
    //    Config
    const SETTING_ON_PLAYER_SELL_ITEM_MONEY   = "玩家上架物品手续费[百分比]";
    const SETTING_ITEM_LIMIT                  = "上架物品上限";
    const SETTING_BAN_ITEMs                   = "禁止上架物品[物品id:damage或者RPG物品中文名称]";
    const SETTING_MESSAGE                     = "购买提醒";
    //  API ID
    const DEFAULT_PLAYER_CONFIG               = [
            "食物"      =>[],
            "建材"      =>[],
            "矿物"      =>[],
            "其他"      =>[],
    ];
    const DEFAULT_ITEM_CONFIG                 = [
            "ID:Damage"     =>[],
            "Name"     =>"",
            "NBT"           =>"",
            "Count"         =>0,
            "Money_One"     =>0
    ];
    private  $config          = null;   //Config

    private $player_int       = [];     //获取玩家点击的TYPE

    private $player_buy       = [];     //获取玩家购买

    private $player_setting   = [];     //玩家设置物品

    private $player_del       = [];     //获取玩家删除

    private $send_type        = [];     //发送一个type选择界面

    private $add_item_setting = [];     //添加前设置

    private $int_on_addItem   = [];

    private $LookUp           = []; //查找的玩家
    /*
     * [PlayerName] = [
     * "type=>
     * ]*/



    /**Player_buy
     * [PlayerName] = [
     * "Player"=>卖家
     *  "type"=>null,
     * "data"=>0
     * "
     * ]
    */

//    private $player_sell = [];  //获取玩家出售 暂时不知道怎么用


    private $File_Final;          //:?File_Final


    public function onEnable()/* : void /* TODO: uncomment this for next major version */{

        if(!is_dir($this->getDataFolder())) mkdir($this->getDataFolder(), 0777, true);
        if(!is_dir($this->getDataFolder()."Players/")) mkdir($this->getDataFolder()."Players/", 0777, true);
        new File_Final($this);
        new File_By_get_Player_Item_Config($this);
        new File__By_PlayerBuy_This($this);
       // new File_By_Player_Sell_This($this);
        new sendUI($this);
        new UIList($this);
        $this->getServer()->getPluginManager()->registerEvents($this,$this);
        $this->config = $this->Config();
        if($this->config->getAll() == null){
            $this->config->setAll([
                self::SETTING_ON_PLAYER_SELL_ITEM_MONEY  => 5,
                self::SETTING_ITEM_LIMIT                 => 5,
                self::SETTING_BAN_ITEMs                  => [],
                self::SETTING_MESSAGE                    => true,
                "公告提示"=>"§c注意①:请将需要上架物品放在物品栏中,不需要上架的物品放在箱子里，避免发生扣错物品的问题\n"
            ]);
            $this->config->save();
        }
        
    }
    public function getPlayer_Int(){
        return $this->player_int;
    }

    public function setPlayer_int(array $int):void{
        $this->player_int = $int;
    }

    public function Config():?Config{
        return new Config($this->getDataFolder()."Config.yml",Config::YAML,[]);
    }

    public function getDelPlayer(){
        return $this->player_del;
    }

    public function setDelPlayer(array $array):void{
        $this->player_del = $array;
    }

    public function getSettingPlayer(){
        return $this->player_setting;
    }

    public function setSettingPlayer(array $array):void{
        $this->player_setting = $array;
    }

    public function getBuyPlayer(){
        return $this->player_buy;
    }

    public function setBuyPlayer(array $array):void{
        $this->player_buy = $array;
    }

    public function getLookPlayer(){
        return $this->LookUp;
    }
    public function setLookPlayer(array $array):void{
        $this->LookUp = $array;
    }

    public function get_add_item_setting(){
        return $this->add_item_setting;
    }

    public function getInt_addItem(){
        return $this->int_on_addItem;
    }

    public function set_add_item_setting(array $array):void{
        $this->add_item_setting = $array;
    }

    public function getIntPlayer(){
        return $this->player_int;
    }

    public function onJoin(PlayerJoinEvent $e){
        //测试用
        File_Final::getInstance()->getPlayer_Config($e->getPlayer());
        
    }
    public function onData(DataPacketReceiveEvent $e){
        $player = $e->getPlayer();
        $pk = $e->getPacket();
        if($pk instanceof ModalFormResponsePacket){
            $form_id = $pk->formId;
            $data = json_decode($pk->formData);
            switch($form_id) {
                case sendUI::MENU:
                    if ($pk->formData == "null\n") return;
                    switch ((int)$data) {
                        case 0:
                            $this->send_type[$player->getName()] = 0;
                            break;
                        case 1:
                            $this->send_type[$player->getName()] = 1;
                            break;
                        case 2:
                            $this->send_type[$player->getName()] = 2;
                            break;
                        case 3:
                            $this->send_type[$player->getName()] = 3;
                            break;
                        case 4:
                            //$this->send_type[$player->getName()] = 4;
                            sendUI::getInstance()->sendLookUp($player);
                            return;
                    }
                    sendUI::getInstance()->sendType($player);
                    return;
                    break;
                case sendUI::MENU_TYPE://分支开始
                    if ($pk->formData == "null\n") {
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    switch ($this->send_type[$player->getName()]) {
                        case 0:
                            switch ((int)$data) {
                                case 0:
                                    $this->player_int[$player->getName()] = "食物";
                                    break;
                                case 1:
                                    $this->player_int[$player->getName()] = "建材";
                                    break;
                                case 2:
                                    $this->player_int[$player->getName()] = "矿物";
                                    break;
                                case 3:
                                    $this->player_int[$player->getName()] = "其他";
                                    break;
                            }
                            sendUI::getInstance()->sendMenu_JY($player);
                            return;
                            break;
                        case 1:
                            switch ((int)$data) {
                                case 0:
                                    $this->player_int[$player->getName()] = "食物";
                                    //sendUI::getInstance()->sendMenu_JY($player);
                                    break;
                                case 1:
                                    $this->player_int[$player->getName()] = "建材";
                                    break;
                                case 2:
                                    $this->player_int[$player->getName()] = "矿物";
                                    break;
                                case 3:
                                    $this->player_int[$player->getName()] = "其他";
                                    break;
                            }
                            sendUI::getInstance()->sendPlayer_AddItem_Int($player);
                            return;
                            break;
                        case 2:
                            switch ((int)$data) {
                                case 0:
                                    $this->player_int[$player->getName()] = "食物";
                                    //sendUI::getInstance()->sendMenu_JY($player);
                                    break;
                                case 1:
                                    $this->player_int[$player->getName()] = "建材";
                                    break;
                                case 2:
                                    $this->player_int[$player->getName()] = "矿物";
                                    break;
                                case 3:
                                    $this->player_int[$player->getName()] = "其他";
                                    break;
                            }
                            sendUI::getInstance()->sendPlayer_Inventory_List($player);
                            return;
                            break;
                        case 3:
                            switch ((int)$data) {
                                case 0:
                                    $this->player_int[$player->getName()] = "食物";
                                    //sendUI::getInstance()->sendMenu_JY($player);
                                    break;
                                case 1:
                                    $this->player_int[$player->getName()] = "建材";
                                    break;
                                case 2:
                                    $this->player_int[$player->getName()] = "矿物";
                                    break;
                                case 3:
                                    $this->player_int[$player->getName()] = "其他";
                                    break;
                            }
                            sendUI::getInstance()->sendPlayer_removeItem($player);
                            return;
                            break;
                        case 4:
                            switch ((int)$data) {
                                case 0:
                                    $this->player_int[$player->getName()] = "食物";
                                    //sendUI::getInstance()->sendMenu_JY($player);
                                    break;
                                case 1:
                                    $this->player_int[$player->getName()] = "建材";
                                    break;
                                case 2:
                                    $this->player_int[$player->getName()] = "矿物";
                                    break;
                                case 3:
                                    $this->player_int[$player->getName()] = "其他";
                                    break;
                            }

                            break;
                    }
                    break;
                case sendUI::MENU_JY:
                    if ($pk->formData == "null\n") {
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    $items = File_Final::getInstance()->getItem_in_data($this->player_int[$player->getName()], (int)$data);
                    if($items == null) {
                       // sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    if ($items[1] == $player->getName()) {
                        $this->player_setting[$player->getName()] = $items[0];
                        sendUI::getInstance()->sendSetting($player);
                        return;
                    }
                    $this->player_buy[$player->getName()] = [
                        "Player" => $items[1],
                        "type" => $items[2],
                        "data" => $items[0]
                    ];
                    sendUI::getInstance()->sendMenu_Player_Buy($player);
                    break;
                case sendUI::ADD_ITEM:
                    if ($pk->formData == "null\n") {
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    $i = 0;
                    foreach ($player->getInventory()->getContents() as $item) {
                        if ($i == (int)$data) {
                            $this->add_item_setting[$player->getName()] = $item;
                            $this->int_on_addItem[$player->getName()] = (int)$i;
                            sendUI::getInstance()->sendPlayer_AddItem_Set($player);
                            break;
                        }
                        $i++;
                    }
                    break;
                case sendUI::MENU_BUY:
                    if ((int)$data[1] == 0) {
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    $p = $this->player_buy[$player->getName()]["Player"];
                    $arrays = File_Final::getInstance()->getPlayer_Config($p)->getAll();
                    $bool = File_Final::getInstance()->Player_Buy_Item($player, $this->player_buy[$player->getName()]["data"], (int)$data[1]);
                    if ($bool) {
                        $items = $arrays[$this->player_buy[$player->getName()]["type"]][(int)$this->player_buy[$player->getName()]["data"]];
                        if($this->Config()->get(self::SETTING_MESSAGE,true) == true){
                            $p = Server::getInstance()->getPlayer($p);
                            if($p != null){
                                $p->sendMessage("§b玩家".$player->getName()."购买了您的".$items["Name"]."§7*".(int)$data[1]."§6+ €".intval($items["Money_One"]*(int)$data[1]));
                            }
                        }
                        $player->sendMessage(TextFormat::GREEN . "§b恭喜你交易成功~  §e花费 €".$items["Money_One"]*(int)$data[1]."§6获得".$items["Name"]."*".(int)$data[1]);
                        unset($this->player_int[$player->getName()], $this->player_buy[$player->getName()],$items);
                    }else {
                        $player->sendMessage(TextFormat::RED . "很抱歉~交易失败失败啦");
                        unset($this->player_int[$player->getName()], $this->player_buy[$player->getName()],$items);
                    }
                    break;
                case sendUI::ADD_ITEM_SETTING:
                    $item = $this->add_item_setting[$player->getName()];
                    if ($item instanceof Item) {
                        if ($data[1] != null || $data[1] != '') {
                            $item->setCustomName($data[1]);
                        }else{
                            $item->setCustomName(getItem_Name::getName($item));
                        }
                        $money = 100;
                        if ($data[2] != null || $data[2] != '' || (int)$data[2] > 0) {
                            $money = intval($data[2]);
                        }
                        if ((int)$data[3] == 0) {
                            sendUI::getInstance()->sendPlayer_AddItem_Int($player);
                            return;
                        }
                        $item->setCount((int)$data[3]);
                        //----新增手续费
                        $m = intval($this->Config()->get(self::SETTING_ON_PLAYER_SELL_ITEM_MONEY));
                        if($m != 0) $m = $m/100;
                        $moneyDel = ($money*(int)$data[3])*$m;
                        if(EconomyAPI::getInstance()->reduceMoney($player,$moneyDel,true) == 0){
                            $player->sendMessage(TextFormat::RED."对不起，你的金钱不足以支付手续费--€".$moneyDel);
                            unset($this->add_item_setting[$player->getName()],$this->player_int[$player->getName()], $this->player_buy[$player->getName()]);
                            return;
                        }
                        //----
                        $bool = File_Final::getInstance()->addItem_by_add($player, $item, $money, $this->getPlayer_Int()[$player->getName()]);
                        if ($bool) {
                            unset($this->player_int[$player->getName()], $this->player_buy[$player->getName()]);
                            $player->sendMessage(TextFormat::GREEN . "货物添加成功   - €".$moneyDel."手续费");
                            return;
                        } else {
                            $player->sendMessage(TextFormat::RED . "货物添加失败    扣除的手续费已经返还 + €".$moneyDel);
                            EconomyAPI::getInstance()->addMoney($player,$moneyDel,true);
                            unset($this->player_int[$player->getName()], $this->player_buy[$player->getName()]);
                            return;
                        }
                     }
                    break;
                case sendUI::DEL_ITEM:
                    if($pk->formData == "null\n"){
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                   
                    $this->player_del[$player->getName()] = (int)$data;
                    sendUI::getInstance()->sendModal_Del($player);
                    break;
                case sendUI::DEL_TRUE:
                    if($pk->formData == "null\n"){
                        unset($this->player_del[$player->getName()]);
                        return;
                    }
                    $d = $this->player_del[$player->getName()];
                    $player_list = File_Final::getInstance()->getPlayer_Config($player)->getAll();
                    if((int)$data == 1){

                        $arr = $player_list[$this->player_int[$player->getName()]];
                        if($arr[$d]["NBT"] != "not"){
                            $item = Item::get(explode(':',$arr[$d]["ID:Damage"])[0],explode(':',$arr[$d]["ID:Damage"])[1],$arr[$d]["Count"]);
                            $item->setCompoundTag($arr[$d]["NBT"]);
                            $item->setCustomName($arr[$d]["Name"]);
                        }else{
                            $item = ItemFactory::get(explode(':',$arr[$d]["ID:Damage"])[0],explode(':',$arr[$d]["ID:Damage"])[1],$arr[$d]["Count"]);
                        }

                        //$item->setCount($arr[$d]["Count"]);
                        $player->getInventory()->addItem($item);
                     //   $inv = array_search($d,$player_list[$this->getIntPlayer()[$player->getName()]]);
                        $inv = array_splice($player_list[$this->player_int[$player->getName()]], $d, 1);
                        File_Final::getInstance()->savePlayer($player,$player_list);
                        $player->sendMessage(TextFormat::GREEN."删除成功 货物已经发放至背包");
                        unset($this->player_del[$player->getName()]);
                        return;
                    }else{
                        unset($this->player_del[$player->getName()]);
                       // sendUI::getInstance()->sendPlayer_removeItem($player);
                    }
                    break;
                case sendUI::ITEM_LIST:
                    if($pk->formData == "null\n"){
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    $this->player_setting[$player->getName()] = (int)$data;
                    sendUI::getInstance()->sendSetting($player);
                    break;
                case sendUI::MENU_SETTING://物品设置  这个获取到了位置player_setting
                    if($pk->formData == "null\n"){
                        sendUI::getInstance()->sendMenu($player);
                       // unset($this->player_setting[$player->getName()]);
                        return;
                    }
                    switch ((int)$data){
                        case 0:
                            sendUI::getInstance()->sendRemove_item($player);
                            break;
                        case 1:
                            sendUI::getInstance()->sendPlayer_AddItem_Int($player);
                            break;
                        case 2:
                            $this->player_del[$player->getName()] = $this->player_setting[$player->getName()];
                            sendUI::getInstance()->sendModal_Del($player);
                            break;
                        case 3:
                            sendUI::getInstance()->sendSetName($player);
                            break;
                        case 4:
                            sendUI::getInstance()->sendSetMoney($player);
                            break;
                    }
                    break;
                case sendUI::REMOVE_ITEM:
                    if((int)$data[1] == 0){
                        sendUI::getInstance()->sendMenu($player);
                      // unset($this->player_setting[$player->getName()]);
                       return;
                    }
                    $player_list = File_Final::getInstance()->getPlayer_Config($player)->getAll();
                    $type = $this->player_int[$player->getName()];
                    $d = $this->player_setting[$player->getName()];
                    if((int)$data[1] >= $player_list[$type][$d]["Count"]){
                        $this->player_del[$player->getName()] = $this->player_setting[$player->getName()];
                        sendUI::getInstance()->sendModal_Del($player);
                        return;
                    }else{
                     
                        $player_list[$type][$d]["Count"] -= (int)$data[1];
                        if($player_list[$type][$d]["NBT"] != "not"){
                            $item = ItemFactory::get(explode(':',$player_list[$type][$d]["ID:Damage"])[0],explode(':',$player_list[$type][$d]["ID:Damage"])[1],(int)$data[1]);
                            if($item instanceof Item)
                                    $item->setCompoundTag($player_list[$type][$d]["NBT"]);
                        }else{
                            $item = ItemFactory::get(explode(':',$player_list[$type][$d]["ID:Damage"])[0],explode(':',$player_list[$type][$d]["ID:Damage"])[1],(int)$data[1]);
                        }

                   
                      //  $item->setCount((int)$data[1]);
                        
                        $player->getInventory()->addItem($item);
                  
                        File_Final::getInstance()->savePlayer($player,$player_list);
                      $player->sendMessage("§b成功减少货物数量~ §e物品已发放至背包");
                      //  sendUI::getInstance()->sendMenu($player);
                    }
                    break;
                case sendUI::SET_NAME:
                    if ($data[0] == null || $data[0] == '') {
                        sendUI::getInstance()->sendMenu($player);
                        unset($this->player_setting[$player->getName()]);
                        return;
                       //
                    }
                    $type = $this->player_int[$player->getName()];
                    $d = $this->player_setting[$player->getName()];
                    $player_list = File_Final::getInstance()->getPlayer_Config($player)->getAll();
                    $player_list[$type][$d]["Name"] = $data[0];
                    File_Final::getInstance()->savePlayer($player,$player_list);
                    $player->sendMessage(TextFormat::GREEN."修改成功");
                    break;
                case sendUI::SET_MONEY:
                    if ($data[0] == null || $data[0] == '') {
                        sendUI::getInstance()->sendMenu($player);
                        unset($this->player_setting[$player->getName()]);
                        return;

                    }
                    $type = $this->player_int[$player->getName()];
                    $d = $this->player_setting[$player->getName()];
                    $player_list = File_Final::getInstance()->getPlayer_Config($player)->getAll();
                    if((float)$data[0] >= 0){
                        $m = intval($this->Config()->get(self::SETTING_ON_PLAYER_SELL_ITEM_MONEY))+1;
                        if($m != 0)
                            $m = $m/100;
                        $moneyDel = ((float)$data[0]*(int)$player_list[$type][$d]["Count"])*$m;
                        $pM=EconomyAPI::getInstance()->myMoney($player);
                       if($pM>=$moneyDel){
                            EconomyAPI::getInstance()->reduceMoney($player,$moneyDel,true);
                            $player_list[$type][$d]["Money_One"] = (float)$data[0];
                            File_Final::getInstance()->savePlayer($player,$player_list);
                            $player->sendMessage(TextFormat::GREEN."修改成功~  §6- €".$moneyDel."手续费");
                            unset($this->player_int[$player->getName()],$this->player_setting[$player->getName()]);
                            return;
                       }else{
                            $player->sendMessage(TextFormat::RED."修改失败~  §6你的金币不足支付 €".$moneyDel."手续费");
//sendUI::getInstance()->sendSetMoney($player);
                            return;
                       }
                    }else{
                        $player->sendMessage(TextFormat::RED."修改失败~价格必须大于等于0");
                        unset($this->player_int[$player->getName()],$this->player_setting[$player->getName()]);
                        return;
                    }
                    break;
                case sendUI::CHA_PLAYER:
                    if ($data[1] == null || $data[1] == '') {
                        sendUI::getInstance()->sendMenu($player);
                        unset($this->player_setting[$player->getName()]);
                        return;
                    }
                    $players = Server::getInstance()->getPlayer($data[1]);
                    if($players != null){
                        $playerName = $players->getName();
                    }else{
                        $playerName = $data[1];
                    }
                    if(File_Final::exitFile($playerName))
                    {

                        if($playerName == $player->getName()){
                            sendUI::getInstance()->sendTextUI($player,"§c玩家名不能为自己，请重新输入");
                            unset($this->LookUp[$player->getName()]);
                            return;
                        }
                        $this->LookUp[$player->getName()] = $playerName;
                    }else{
                        sendUI::getInstance()->sendTextUI($player,"§c未知的玩家名，请重新输入");
                        unset($this->LookUp[$player->getName()]);
                        return;
                    }
                    sendUI::getInstance()->sendShowList($player);
                    break;
                case sendUI::LOOKUP:
                    if($pk->formData == "null\n"){
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    $buttons = File_Final::getPlayerAllItems($this->getLookPlayer()[$player->getName()]);
                    foreach ($buttons as $data_=>$value){
                            if($data_ == (int)$data){
                                $this->player_buy[$player->getName()] = [
                                    "Player" =>$this->getLookPlayer()[$player->getName()],
                                    "type" =>$value["type"],
                                    "data" =>$value["id"]
                                ];
                                sendUI::getInstance()->sendMenu_Player_Buy($player);
                                return;
                        }
                    }
                    break;
                case 0x12222:
                    if($pk->formData == "null\n"){
                        return;
                    }
                    if((int)$data == 1){
                        sendUI::getInstance()->sendLookUp($player);
                        return;
                    }else{
                        sendUI::getInstance()->sendMenu($player);
                        return;
                    }
                    break;
            }
        }
    }
    public function onCommand(CommandSender $sender, Command $command, string $label, array $args): bool
    {
        if($command->getName() == "sp"){
            if($sender instanceof Player){
                sendUI::getInstance()->sendMenu($sender);
            }
        }
        return true;
    }


}