package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private abstract class AggHandler {
        HashMap<Field, Integer> aggResult;

        abstract void handle(Field gbField, IntField aggField);
        public AggHandler() {
            aggResult = new HashMap<>();
        }

        public HashMap<Field, Integer> getAggResult() {
            return aggResult;
        }
    }

    private class CountHandler extends AggHandler {

        @Override
        void handle(Field gbField, IntField aggField) {
            if(aggResult.containsKey(gbField)) {
                aggResult.put(gbField, aggResult.get(gbField) + 1);
            }
            else {
                aggResult.put(gbField, 1);
            }
        }
    }

    private class SumHandler extends AggHandler {

        @Override
        void handle(Field gbField, IntField aggField) {
            int val = aggField.getValue();
            if(aggResult.containsKey(gbField)) {
                aggResult.put(gbField, aggResult.get(gbField) + val);
            }
            else {
                aggResult.put(gbField, val);
            }
        }
    }

    private class MaxHandler extends AggHandler {

        @Override
        void handle(Field gbField, IntField aggField) {
            int val = aggField.getValue();
            if(aggResult.containsKey(gbField)) {
                aggResult.put(gbField, Math.max(val, aggResult.get(gbField)));
            }
            else {
                aggResult.put(gbField, val);
            }
        }
    }

    private class MinHandler extends AggHandler {

        @Override
        void handle(Field gbField, IntField aggField) {
            int val = aggField.getValue();
            if(aggResult.containsKey(gbField)) {
                aggResult.put(gbField, Math.min(val, aggResult.get(gbField)));
            }
            else {
                aggResult.put(gbField, val);
            }
        }
    }

    private class AvgHandler extends AggHandler {

        HashMap<Field, Integer> sum;

        HashMap<Field, Integer> count;

        private AvgHandler() {
            sum = new HashMap<>();
            count = new HashMap<>();
        }
        @Override
        void handle(Field gbField, IntField aggField) {
            int val = aggField.getValue();
            if(sum.containsKey(gbField)) {
                sum.put(gbField, sum.get(gbField) + val);
                count.put(gbField, count.get(gbField) + 1);
            }
            else {
                sum.put(gbField, val);
                count.put(gbField, 1);
            }
            aggResult.put(gbField,sum.get(gbField) / count.get(gbField));
        }
    }

    private static final long serialVersionUID = 1L;

    int gbFieldIndex;

    Type gbFieldType;

    int aFieldIndex;

    AggHandler aggHandler;
    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or
     *                    NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null
     *                    if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // TODO: some code goes here
        this.gbFieldIndex = gbfield;
        this.aFieldIndex = afield;
        this.gbFieldType = gbfieldtype;
        switch (what) {
            case MIN:
                aggHandler = new MinHandler();
                break;
            case MAX:
                aggHandler = new MaxHandler();
                break;
            case SUM:
                aggHandler = new SumHandler();
                break;
            case COUNT:
                aggHandler = new CountHandler();
                break;
            case AVG:
                aggHandler = new AvgHandler();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported aggregation operator");
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // TODO: some code goes here
        Field gbField;
        IntField aggField = (IntField) tup.getField(aFieldIndex);
        if(gbFieldIndex == -1) {
            gbField = null;
        }
        else {
            gbField = tup.getField(gbFieldIndex);
        }
        aggHandler.handle(gbField, aggField);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        // TODO: some code goes here
        HashMap<Field, Integer> aggResult = aggHandler.getAggResult();
        TupleDesc tupleDesc;
        Type[] fieldTypes;
        String[] fieldNames;
        List<Tuple> tuples = new ArrayList<>();
        if(gbFieldIndex == NO_GROUPING) {
            fieldTypes = new Type[]{Type.INT_TYPE};
            fieldNames = new String[]{"aggregateValue"};
            tupleDesc = new TupleDesc(fieldTypes, fieldNames);
            Tuple tuple = new Tuple(tupleDesc);
            tuple.setField(0, new IntField(0));
            tuples.add(tuple);
        }
        else {
            fieldTypes = new Type[]{gbFieldType, Type.INT_TYPE};
            fieldNames = new String[]{"groupByValue", "aggregateValue"};
            tupleDesc = new TupleDesc(fieldTypes, fieldNames);
            for(Field field : aggResult.keySet()) {
                Tuple tuple = new Tuple(tupleDesc);
                tuple.setField(0, field);
                IntField intField = new IntField(aggResult.get(field));
                tuple.setField(1,intField);
                tuples.add(tuple);
            }
        }
        return new TupleIterator(tupleDesc, tuples);
    }

}
