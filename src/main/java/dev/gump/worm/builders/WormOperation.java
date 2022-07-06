package dev.gump.worm.builders;

import dev.gump.worm.Worm;
import dev.gump.worm.typeadapter.WormTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WormOperation {
    private String field;
    private Object value;
    private List<WormOperation> operationsChildren = new ArrayList<>();
    private WormOperations operation;
    private WormJoint joint = WormJoint.AND;

    public WormOperation(WormOperations operation, WormOperation... operations) {
        this.operation = operation;
        this.operationsChildren = List.of(operations);
    }
    public WormOperation(WormOperations operation, String field) {
        this.operation = operation;
        this.field = field;
    }
    public WormOperation(WormOperations operation, String field, Object value) {
        this.operation = operation;
        this.field = field;
        this.value = value;
    }

    public WormOperation(WormOperations operation, String field, Object value, WormJoint joint) {
        this.operation = operation;
        this.field = field;
        this.value = value;
        this.joint = joint;
    }
    public WormOperation(WormOperations operation, String field, Object value, WormOperation... operations) {
        this.operation = operation;
        this.field = field;
        this.value = value;
        this.operationsChildren = List.of(operations);
    }

    public WormOperation(WormJoint joint, WormOperation... operations) {
        this.joint = joint;
        this.operationsChildren = List.of(operations);
    }

    public WormOperations getOperation() {
        return operation;
    }

    public WormJoint getJoint() {
        return joint;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public List<WormOperation> getOperationsChildren() {
        return operationsChildren;
    }

    public String buildChildrens(){
        StringBuilder builder = new StringBuilder("");
        if(getOperationsChildren().size() > 0){
            builder.append("(").append(getOperationsChildren().stream().map(WormOperation::buildChildrens).collect(Collectors.joining(" "+joint.toString()+ " "))).append(")");
        }else{
            if(field != null && operation != null){
                builder.append(field).append(" ").append(operation.getOperation()).append(" ");
                if(value != null) {
                    @SuppressWarnings("rawtypes")
                    WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(value.getClass());
                    if (typeAdapter == null) {
                        if (value.getClass().getName().toLowerCase().contains("string"))
                            builder.append("'").append(value).append("'");
                        else if(value.getClass() == Field.class)
                            builder.append(((Field) value).getField());
                        else
                            builder.append(value);
                    }else
                        builder.append("'").append(typeAdapter.toDatabase(value)).append("'");
                }
            }
        }
        return builder.toString();
    }
}
