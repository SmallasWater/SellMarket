package market.utils;


import cn.nukkit.utils.Config;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import market.sMarket;
import market.player.iTypes;
import market.player.pItems;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Tools {

    public static LinkedList<String> strings = new LinkedList<String>(){
        {
            add("§aOrdinary Search");
            add("§aSearch ID");
            add("§6Search Name");
            add("§cSearch Player");
            add("§eSearch Description");
            add("§bSearch Price");
            add("§dSearch Amounts");

        }
    };
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
        StringBuilder stringBuilder = new StringBuilder();
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

    public static LinkedHashMap<String,String> getType(){
        LinkedHashMap<String,String> linkedHashMap = new LinkedHashMap<>();
        Config config = sMarket.getApi().config;
        Map map = (Map) config.get("Custom Class");
        if(map != null){
            for(Object o:map.keySet()){
                if(o instanceof String){
                    Object get = map.get(o);
                    if(get instanceof String){
                        linkedHashMap.put((String) o,(String) get);
                    }
                }
            }
        }else{
            linkedHashMap.put("Block","textures/blocks/brick");
            linkedHashMap.put("Tool","textures/items/diamond_pickaxe");
            linkedHashMap.put("Food","textures/items/bread");
            linkedHashMap.put("Other","textures/ui/inventory_icon");
            config.set("Custom Class",linkedHashMap);
            config.save();
        }

        return linkedHashMap;
    }


    public static LinkedHashMap<String, pItems> getPlayerConfigs(){
        File file = new File(sMarket.getApi().getDataFolder()+"/Players");
        LinkedHashMap<String,pItems> spItemsLinkedHashMap = new LinkedHashMap<>();
        File[] files = file.listFiles();
        if(files != null){
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

    public static String getStringBySeekSetting(int i){
        int a = 0;
        for(String s:strings){
            if(a == i){
                return s;
            }
            a++;
        }
        return strings.get(0);
    }


    public static LinkedList<iTypes> getItemsByType(String type){
        LinkedList<iTypes> list = new LinkedList<>();
        for (iTypes item:getItemsAll()){
            if(item.getType().equals(type)){
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
        for(String playerName:sMarket.playerItems.keySet()){
            pItems items = sMarket.playerItems.get(playerName);
            get.addAll(items.getAllItems());
        }
        return get;
    }

    /** 获取索引比较值 */
    private static String getQuery(String target){
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

    public static String getTypeByInt(int i){
        int a = 0;
        for(String t:sMarket.sType.keySet()){
            if(a == i){
                return t;
            }
            a++;
        }
        return Tools.toList(sMarket.sType.keySet().toArray()).get(0);
    }


    /** 根据索引值获取物品*/
    public static LinkedList<iTypes> seekItemById(String seek,int type,boolean sort){
        LinkedList<iTypes> linkedList = getItemsAll();
        LinkedList<iTypes> items = new LinkedList<>();
        if(type == 0){
            for(iTypes item:linkedList) {
                if (seek.split(":").length > 1) {
                    if(item.getId().matches(getQuery(seek))){
                        items.add(item);
                    }
                } else {
                    if (seek.split("-").length > 1
                            || seek.split(">").length > 1
                            || seek.split("<").length > 1) {
                        if(seek.split("-").length > 1){
                            double min = Double.parseDouble(seek.split("-")[0].trim());
                            double max = Double.parseDouble(seek.split("-")[1].trim());
                            if(item.getMoney() >= min && item.getMoney() < max){
                                items.add(item);
                                continue;
                            }
                        }else if(seek.split(">").length > 1){
                            double max = Double.parseDouble(seek.split(">")[1].trim());
                            if(item.getMoney() > max){
                                items.add(item);
                                continue;
                            }

                        }else if(seek.split("<").length > 1){
                            double max = Double.parseDouble(seek.split("<")[1].trim());
                            if(item.getMoney() < max){
                                items.add(item);
                                continue;
                            }
                        } else{
                            try {
                                if(item.getMoney() == Double.parseDouble(seek)){
                                    items.add(item);
                                    continue;
                                }
                            }catch (Exception e){
                                break;
                            }
                        }
                        if (adds(seek, items, item)) {
                            break;
                        }
                    }else {
                        try {
                            if (isNumeric(seek)) {
                                if (item.getMoney() == Double.parseDouble(seek)) {
                                    items.add(item);
                                    continue;
                                }
                                if (item.getCount() == Integer.parseInt(seek)) {
                                    items.add(item);
                                }
                            } else {
                                if (item.getType().matches(getQuery(seek))) {
                                    items.add(item);
                                    continue;
                                }
                                if (item.getName().matches(getQuery(seek))) {
                                    items.add(item);
                                    continue;
                                }
                                if (item.getMaster().matches(getQuery(seek))) {
                                    items.add(item);
                                    continue;
                                }
                                if (item.getMessage().matches(getQuery(seek))) {
                                    items.add(item);
                                }
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            }
        }else{
            for(iTypes item:linkedList){
                if(type == 1){
                    if(item.getId().matches(getQuery(seek))){
                        items.add(item);
                    }
                }else if(type == 2){
                    if(item.getName().matches(getQuery(seek))){
                        items.add(item);
                    }
                }else if(type == 3){
                    if(item.getMaster().matches(getQuery(seek))){
                        items.add(item);
                    }
                }else if(type == 4){
                    if(item.getMessage().matches(getQuery(seek))){
                        items.add(item);
                    }
                }else if(type == 5){
                    if(seek.split("-").length > 1){
                        double min = Double.parseDouble(seek.split("-")[0].trim());
                        double max = Double.parseDouble(seek.split("-")[1].trim());
                        if(item.getMoney() >= min && item.getMoney() < max){
                            items.add(item);
                        }
                    }else if(seek.split(">").length > 1){
                        double max = Double.parseDouble(seek.split(">")[1].trim());
                        if(item.getMoney() > max){
                            items.add(item);
                        }
                    }else if(seek.split("<").length > 1){
                        double max = Double.parseDouble(seek.split("<")[1].trim());
                        if(item.getMoney() < max){
                            items.add(item);
                        }
                    } else{
                        if(isNumeric(seek)){
                            if(item.getMoney() == Double.parseDouble(seek)){
                                items.add(item);
                            }
                        }
                    }
                }else if(type == 6){
                    if (adds(seek, items, item)) {
                        break;
                    }
                }
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
    private static Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");
    private static boolean isNumeric(String str){
        if(str.indexOf(".")>0){
            if(str.indexOf(".")==str.lastIndexOf(".") && str.split("\\.").length==2){
                return NUMBER_PATTERN.matcher(str.replace(".","")).matches();
            }else {
                return false;
            }
        }else {
            return NUMBER_PATTERN.matcher(str).matches();
        }
    }


    private static boolean adds(String seek, LinkedList<iTypes> items, iTypes item) {
        if(seek.split("-").length > 1){
            int min = Integer.parseInt(seek.split("-")[0].trim());
            int max = Integer.parseInt(seek.split("-")[1].trim());
            if(item.getCount() >= min && item.getCount() < max){
                items.add(item);
            }
        }else if(seek.split(">").length > 1){
            int max = Integer.parseInt(seek.split(">")[1].trim());
            if(item.getCount() > max){
                items.add(item);
            }
        }else if(seek.split("<").length > 1){
            int max = Integer.parseInt(seek.split("<")[1].trim());
            if(item.getCount() < max){
                items.add(item);
            }
        } else{
            try {
                if(item.getCount() == Integer.parseInt(seek)){
                    items.add(item);
                }
            }catch (Exception e){
                return true;
            }
        }
        return false;
    }

    public static int getIntByType(String type){
        int i = 0;
        for(String type1:sMarket.sType.keySet()){
            if(type1.equals(type)){
                return i;
            }
            i++;
        }
        return 0;
    }

    public static LinkedList<String> toList(Object[] strings){
        LinkedList<String> strings1 = new LinkedList<>();
        for (Object o:strings){
            if(o instanceof String){
                strings1.add((String) o);
            }
        }
        return strings1;
    }



}
