package market.utils;

import cn.nukkit.Server;
import market.load.loadMoney;
import market.sMarket;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill {

    public static final String SELL = "出售";
    public static final String BUY = "购买";
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
        builder.append("|§7时间: §r").append(date).append("\n");
        builder.append("|§7类型: §r").append(this.type).append("\n");
        builder.append("|§7交互玩家: §r").append(this.playerName);
        if(Server.getInstance().getPlayer(this.playerName) != null){
            builder.append(" §a[在线]").append("\n");
        }else{
            builder.append(" §c[离线]").append("\n");
        }
        builder.append("|§7").append(this.type).append("物品: §r");
        if(this.itemName.split("\\n").length > 1){
            builder.append("\n").append(this.itemName).append("\n");
        }else{
            builder.append(this.itemName).append("\n");
        }
        builder.append("|§7数量: §r").append(this.count).append("\n");
        builder.append("|§7金钱: §r").append(money.getMonetaryUnit()).append(this.money).append("\n");
        builder.append("|§7交易状态: §r").append(this.sellType).append("\n");
        builder.append("§r---------------------------------\n");
        return builder.toString();
    }
}
