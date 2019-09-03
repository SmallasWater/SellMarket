package market.utils;

import cn.nukkit.utils.Config;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import market.load.sMarket;
import market.player.iTypes;
import market.player.pItems;
import market.player.sType;

import java.io.File;
import java.util.*;

public class Tools {
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static LinkedHashMap<String, pItems> getPlayerConfigs(){
        File file = new File(sMarket.getApi().getDataFolder()+"/Players");
        LinkedHashMap<String,pItems> spItemsLinkedHashMap = new LinkedHashMap<>();
        File[] files = file.listFiles();
        if(files != null){
            Arrays.sort(files);
            for(File file1:files){
                if(file1.isFile()){
                    String names = file1.getName().substring(0,file1.getName().lastIndexOf("."));
                    pItems file2 = new pItems(names,new Config(sMarket.getApi().getPlayerFile(names),Config.YAML));
                    spItemsLinkedHashMap.put(names,file2);
                }
            }
        }
        return spItemsLinkedHashMap;

    }

    /** 解码 Data */
    public static Object[] decodeData(String data){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        @SuppressWarnings("serial")
        Object[] d = gson.fromJson(data,new TypeToken<Object[]>(){}.getType());
        return d;
    }

    public static boolean inArrayItem(String ids){
        List<String> blacks = sMarket.getApi().black.getStringList("封禁物品");
        if(ids.split(":").length > 1){
            if(Integer.parseInt(ids.split(":")[1]) == 0){
                if(blacks.contains(ids.split(":")[0])){
                    return true;
                }
            }
            return blacks.contains(ids);
        }else{
            String id2 = ids+":0";
            if(blacks.contains(id2)){
                return true;
            }
            return blacks.contains(ids);
        }
    }

    public static LinkedList<iTypes> getItemsByType(sType type){
        LinkedList<iTypes> list = new LinkedList<>();
        for (iTypes item:getItemsAll()){
            if(item.getType() == type){
                list.add(item);
            }
        }
        return list;
    }

    public static boolean inArray(banItem object,LinkedList<banItem> list){
        for(banItem o:list){
            if(o.equals(object)){
                return true;
            }
        }
        return false;
    }

    public static LinkedList<iTypes> getItemsAll(){
        LinkedList<iTypes> get = new LinkedList<>();
        for(String playerName:sMarket.getApi().playerItems.keySet()){
            pItems items = sMarket.getApi().playerItems.get(playerName);
            get.addAll(items.getAllItems());
        }
        return get;
    }

    /** 获取索引比较值 */
    public static String getQuery(String target){
        StringBuilder builder = new StringBuilder(target);
        char[] searchChars = target.toCharArray();
        int max = 0;
        for(int i = 0;i<target.length();i++){
            int index = builder.indexOf(searchChars[i]+"");
            if(max==0){
                max = index;
            }else{
                index = builder.indexOf(searchChars[i]+"",max);
                max = index;
            }
            builder.insert(index,"[\\s\\S]*");
        }
        return "("+builder.append("[\\s\\S]*").toString()+")";
        //之前的
    }

    public static sType getTypeByInt(int i){
        int a = 0;
        for(sType t:sType.values()){
            if(a == i){
                return t;
            }
            a++;
        }
        return sType.Block;
    }


    /** 根据索引值获取物品*/
    public static LinkedList<iTypes> seekItemById(String seek,int type,boolean sort){
        LinkedList<iTypes> linkedList = getItemsAll();
        LinkedList<iTypes> items = new LinkedList<>();
        for(iTypes item:linkedList){
            switch (type){
                case 0:
                    if(item.getId().matches(getQuery(seek))){
                        items.add(item);
                    }
                    break;
                case 1:
                    if(item.getName().matches(getQuery(seek))){
                        items.add(item);
                    }
                    break;
                case 2:
                    if(item.getMaster().matches(getQuery(seek))){
                        items.add(item);
                    }
                    break;
                case 3:
                    if(item.getMessage().matches(getQuery(seek))){
                        items.add(item);
                    }
                    break;
                case 4:
                    if(item.getMoney() == Double.parseDouble(seek)){
                        items.add(item);
                    }
                    break;
                case 5:
                    if(item.getCount() == Integer.parseInt(seek)){
                        items.add(item);
                    }
                    break;
                    default:break;
            }
        }
        if(sort){
            Comparator<iTypes> comparator = (s1, s2) -> {
                if(s1.getMoney() != s2.getMoney()){
                    return (int)Math.floor(s1.getMoney()) - (int)Math.floor(s2.getMoney());
                } else if (!s1.getMessage().equals(s2.getMessage())) {
                    return s1.getMessage().compareTo(s2.getMessage());
                } else {
                    return s1.getCount() - s2.getCount();
                }
            };
            items.sort(comparator);
        }
        return items;
    }

    public static int getIntByType(sType type){
        int i = 0;
        for(sType type1:sType.values()){
            if(type1 == type){
                return i;
            }
            i++;
        }
        return 0;
    }



}
