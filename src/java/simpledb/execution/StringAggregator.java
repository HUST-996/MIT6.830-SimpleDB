package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private class CountHandler {
        HashMap<Field, Integer> aggResult;

        public CountHandler() {
            aggResult = new HashMap<>();
        }

        public HashMap<Field, Integer> getAggResult() {
            return aggResult;
        }

        public void handle(Field gbField) {
            if(aggResult.containsKey(gbField)) {
                aggResult.put(gbField, aggResult.get(gbField) + 1);
            }
            else {
                aggResult.put(gbField, 1);
            }
        }
    }

    private static final long serialVersionUID = 1L;

    int gbFieldIndex;

    Type gbFieldType;

    int aFieldIndex;

    CountHandler countHandler;

    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // TODO: some code goes here
        this.gbFieldIndex = gbfield;
        this.aFieldIndex = afield;
        this.gbFieldType = gbfieldtype;
        if(what != Op.COUNT) {
            throw new IllegalArgumentException("what != COUNT");
        }
        this.countHandler = new CountHandler();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // TODO: some code goes here
        Field gbField;
        if(gbFieldIndex == -1) {
            gbField = null;
        }
        else {
            gbField = tup.getField(gbFieldIndex);

        }
        countHandler.handle(gbField);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *         aggregateVal) if using group, or a single (aggregateVal) if no
     *         grouping. The aggregateVal is determined by the type of
     *         aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // TODO: some code goes here
        TupleDesc tupleDesc;
        HashMap<Field, Integer> aggResult = countHandler.getAggResult();
        List<Tuple> tuples = new ArrayList<>();
        Type[] fieldType;
        String[] fieldString;
        if(gbFieldIndex == NO_GROUPING) {
            fieldType = new Type[]{Type.INT_TYPE};
            fieldString = new String[]{"aggregateValue"};
            tupleDesc = new TupleDesc(fieldType, fieldString);
            Tuple tuple = new Tuple(tupleDesc);
            tuple.setField(0,new IntField(0));
            tuples.add(tuple);
        }
        else {
            fieldType = new Type[]{gbFieldType, Type.INT_TYPE};
            fieldString = new String[]{"groupByValue", "aggregateValue"};
            tupleDesc = new TupleDesc(fieldType, fieldString);
            for(Field field : aggResult.keySet()) {
                Tuple tuple = new Tuple(tupleDesc);
                tuple.setField(0, field);
                tuple.setField(1,new IntField(aggResult.get(field)));
                tuples.add(tuple);
            }
        }
        return new TupleIterator(tupleDesc, tuples);
    }

}
