package market.load;

import cn.nukkit.Player;
import me.onebone.economyapi.EconomyAPI;
import money.Money;
import market.sMarket;

public class loadMoney {

    private static final int MONEY = 1;
    private static final int ECONOMY_API = 2;

    private int money = 0;

    public loadMoney(){
        if(sMarket.getApi().getServer().getPluginManager().getPlugin("EconomyAPI") != null){
            money = ECONOMY_API;
        }else if(sMarket.getApi().getServer().getPluginManager().getPlugin("Money") != null){
            money = MONEY;
        }
    }

    public String getMonetaryUnit(){
        if (this.money == ECONOMY_API) {
            return EconomyAPI.getInstance().getMonetaryUnit();
        }
        return "$";
    }

    public double myMoney(Player player){
        return myMoney(player.getName());
    }

    public double myMoney(String player){
        switch (this.money){
            case MONEY:
                if(Money.getInstance().getPlayers().contains(player)){
                    return Money.getInstance().getMoney(player);
                }
                break;
            case ECONOMY_API:
                return EconomyAPI.getInstance().myMoney(player) ;

            default:break;
        }
        return 0;
    }

    public boolean addMoney(Player player,double money){
        return addMoney(player.getName(),money);
    }

    public boolean addMoney(String player,double money){
        switch (this.money){
            case MONEY:
                if(Money.getInstance().getPlayers().contains(player)){
                    return Money.getInstance().addMoney(player,(float) money);
                }
                break;
            case ECONOMY_API:
                return (EconomyAPI.getInstance().addMoney(player,money) == 1);

            default:break;
        }
        return false;
    }
    public boolean reduceMoney(Player player,double money){
        return reduceMoney(player.getName(),money);
    }

    public boolean reduceMoney(String player,double money){
        switch (this.money){
            case MONEY:
                if(Money.getInstance().getPlayers().contains(player)){
                    return Money.getInstance().reduceMoney(player,(float) money);
                }
                break;
            case ECONOMY_API:
                return (EconomyAPI.getInstance().reduceMoney(player,money) == 1);

            default:break;
        }
        return false;
    }





}
