package market.player;


/** @author 若水*/
public enum sType {
    /** 方块*/
    Block("方块","textures/blocks/brick"),
    /** 工具*/
    Tools("工具","textures/items/diamond_pickaxe"),
    /** 食物*/
    Food("食物","textures/items/bread"),
    /** 其他*/
    Author("其他","textures/ui/inventory_icon");

    private String name;
    private String image;
    sType(String name,String image){
        this.name = name;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

}

