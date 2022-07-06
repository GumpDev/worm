package dev.gump.worm.builders;

public enum WormOperations {
    LIKE("LIKE"),
    BETTER_THAN(">"),
    LESS_THAN("<"),
    EQUALS("="),
    IS_NULL("IS NULL"),
    NOT_NULL("IS NOT NULL");

    private final String operation;
    WormOperations(String operation){
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
