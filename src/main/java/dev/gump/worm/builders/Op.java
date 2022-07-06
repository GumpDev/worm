package dev.gump.worm.builders;

public class Op {
    /**
     * Define a count field
     *
     * @param field the field in database
     * @return an field object that helps worm on query build
     */
    public static String count(String field){
        return "COUNT("+field+")";
    }
    /**
     * Define a sum field
     *
     * @param field the field in database
     * @return an field object that helps worm on query build
     */
    public static String sum(String field){
        return "SUM("+field+")";
    }
    /**
     * Define a field on Where
     *
     * @param field the field in database
     * @return an field object that helps worm on query build
     */
    public static Field field(String field){
        return new Field(field);
    }

    /**
     * Compare one field and a value is equals
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation equals(String field, Object value){
        return new WormOperation(WormOperations.EQUALS, field, value);
    }
    /**
     * Compare one field and a value is greater
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation greater(String field, Object value){
        return new WormOperation(WormOperations.BETTER_THAN, field, value);
    }
    /**
     * Compare one field and a value is less
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation less(String field, Object value){
        return new WormOperation(WormOperations.LESS_THAN, field, value);
    }
    /**
     * Compare one field and a value is like
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation like(String field, Object value){
        return new WormOperation(WormOperations.LIKE, field, "%"+value+"%");
    }
    /**
     * Compare one field and a value start with
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation startWith(String field, Object value){
        return new WormOperation(WormOperations.LIKE, field, "%"+value);
    }
    /**
     * Compare one field and a value ends with
     *
     * @param field the field in database
     * @param value the value that will be compared
     * @return an object that helps worm on query build
     */
    public static WormOperation endsWith(String field, Object value){
        return new WormOperation(WormOperations.LIKE, field, value+"%");
    }
    /**
     * verify if field is null
     *
     * @param field the field in database
     * @return an object that helps worm on query build
     */
    public static WormOperation isNull(String field){
        return new WormOperation(WormOperations.IS_NULL, field);
    }
    /**
     * verify if field is not null
     *
     * @param field the field in database
     * @return an object that helps worm on query build
     */
    public static WormOperation notNull(String field){
        return new WormOperation(WormOperations.NOT_NULL, field);
    }

    /**
     * Operations that will be considered Or
     *
     * @param wormOperation many sql operations
     * @return an object that helps worm on query build
     */
    public static WormOperation or(WormOperation... wormOperation){
        return new WormOperation(WormJoint.OR, wormOperation);
    }
    /**
     * Operations that will be considered And
     *
     * @param wormOperation many sql operations
     * @return an object that helps worm on query build
     */
    public static WormOperation and(WormOperation... wormOperation){
        return new WormOperation(WormJoint.AND, wormOperation);
    }
}
