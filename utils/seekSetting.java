package market.utils;

public class seekSetting {

    /* 查询ID
       查询名称
       查询用户
       查询简介
       查询价格
       查询数量
       */

    /** 索引内容*/
    public String message;

    /** 是否排序*/
    public boolean sqrt;

    /** 索引设置*/
    public int type;

    public seekSetting(String message,int type,boolean sqrt){
        this.message = message;
        this.type = type;
        this.sqrt = sqrt;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public boolean isSqrt() {
        return sqrt;
    }
}
