package dev.gump.worm.field;

public enum RelationshipEvent {
    NO_ACTION("NO ACTION"),
    CASCADE("CASCADE"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT");

    private String value;
    RelationshipEvent(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
