package market.events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import market.player.iTypes;
import market.utils.Bill;

public class PlayerBuyItemEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    private iTypes types;

    private Bill buyer;

    private Bill seller;

    public PlayerBuyItemEvent(Player player, iTypes types, Bill buyer,Bill seller){
        this.player = player;
        this.types = types;
        this.buyer = buyer;
        this.seller = seller;
    }

    public Bill getBuyer() {
        return buyer;
    }

    public Bill getSeller() {
        return seller;
    }

    public iTypes getTypes() {
        return types;
    }


}
