package market.from;

import cn.nukkit.Player;

class SaveWindow {

    static void sendWindow(int id, Player player){
        switch (id){
            case create.ADD_INVENTORY:
                create.sendAddInventory(player);
                break;
            case create.BUY_MENU:
                create.sendBuyMenu(player);
                break;
            case create.CHOSE:
                create.sendItems(player);
            case create.MENU:
                create.sendMenu(player);
                break;
            case create.SEEK:
                create.sendSeekItems(player);
                break;
            case create.SEEK_MENU:
                create.sendSeekShow(player);
                break;
            case create.TYPES:
                create.sendTypes(player);
                break;
            case create.TYPES_SHOW:
                create.sendTypeShow(player);
                break;
            case create.SETTING:
                create.sendSetting(player);
                break;
            case create.UPDATA:
                create.sendAddSetting(player);
                break;
            default:break;
        }
        listener.lastWindow.put(player,create.MENU);
    }
}
