package demo.sql.enums;

public enum EnumOutType {

    EXCEL("excel"), CSV("csv"), DB("db"), JSON("json");

    private String type;

    EnumOutType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static EnumOutType getOutTypeByType(String type) {
        for (EnumOutType outType : EnumOutType.values()) {
            if (outType.getType().equals(type)) {
                return outType;
            }
        }
        return null;
    }
}
