package market.utils;

import cn.nukkit.Server;
import market.load.loadMoney;
import market.sMarket;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill {

    public static final String SELL = "Sell";
    public static final String BUY = "Buy";
    private Date date;
    private String playerName,type,itemName,sellType;
    private int money;
    private int count;


    public Bill(Date date,String playerName,String itemName,int money,int count,String type,String sellType){
        this.date = date;
        this.playerName = playerName;
        this.money = money;
        this.count = count;
        this.type = type;
        this.itemName = itemName;
        this.sellType = sellType;
    }

    public void setSellType(String sellType) {
        this.sellType = sellType;
    }

    @Override
    public String toString() {
        loadMoney money = sMarket.money;
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss");
        String date = dateFormat.format(this.date);
        builder.append("|§7Time: §r").append(date).append("\n");
        builder.append("|§7Type: §r").append(this.type).append("\n");
        builder.append("|§7Interact with player: §r").append(this.playerName);
        if(Server.getInstance().getPlayer(this.playerName) != null){
            builder.append(" §a[Online]").append("\n");
        }else{
            builder.append(" §c[Offline]").append("\n");
        }
        builder.append("|§7").append(this.type).append("Item: §r");
        if(this.itemName.split("\\n").length > 1){
            builder.append("\n").append(this.itemName).append("\n");
        }else{
            builder.append(this.itemName).append("\n");
        }
        builder.append("|§7Amount: §r").append(this.count).append("\n");
        builder.append("|§7Money: §r").append(money.getMonetaryUnit()).append(this.money).append("\n");
        builder.append("|§7Transaction Status: §r").append(this.sellType).append("\n");
        builder.append("§r---------------------------------\n");
        return builder.toString();
    }
}
