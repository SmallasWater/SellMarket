package market.events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import cn.nukkit.item.Item;
import market.player.iTypes;

public class PlayerSettingItemEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    private iTypes types;

    public PlayerSettingItemEvent(Player player, iTypes types){
        this.types = types;
        this.player = player;
    }

    public iTypes getTypes() {
        return types;
    }
}
