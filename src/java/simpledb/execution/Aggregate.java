package simpledb.execution;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.execution.Aggregator.Op;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.NoSuchElementException;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;

    int aFieldIndex;

    int gFieldIndex;

    OpIterator[] children;

    Aggregator.Op aop;

    TupleDesc tupleDesc;

    Aggregator aggregator;

    OpIterator aggIterator;
    /**
     * Constructor.
     * <p>
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     *
     * @param child  The OpIterator that is feeding us tuples.
     * @param afield The column over which we are computing an aggregate.
     * @param gfield The column over which we are grouping the result, or -1 if
     *               there is no grouping
     * @param aop    The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
        // TODO: some code goes here
        children = new OpIterator[1];
        children[0] = child;
        this.aFieldIndex = afield;
        this.gFieldIndex = gfield;
        this.aop = aop;
        this.tupleDesc = child.getTupleDesc();
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link Aggregator#NO_GROUPING}
     */
    public int groupField() {
        // TODO: some code goes here
        if(gFieldIndex == Aggregator.NO_GROUPING) {
            return Aggregator.NO_GROUPING;
        }
        else {
            return gFieldIndex;
        }
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     */
    public String groupFieldName() {
        // TODO: some code goes here
        if(gFieldIndex == Aggregator.NO_GROUPING) {
            return null;
        }
        else {
            return tupleDesc.getFieldName(gFieldIndex);
        }
    }

    /**
     * @return the aggregate field
     */
    public int aggregateField() {
        // TODO: some code goes here
        return aFieldIndex;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     */
    public String aggregateFieldName() {
        // TODO: some code goes here
        return tupleDesc.getFieldName(aFieldIndex);
    }

    /**
     * @return return the aggregate operator
     */
    public Aggregator.Op aggregateOp() {
        // TODO: some code goes here
        return aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
        return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        super.open();
        children[0].open();

        Type gbFieldType = tupleDesc.getFieldType(gFieldIndex);
        Type aggFieldType = tupleDesc.getFieldType(aFieldIndex);
        if(aggFieldType == Type.INT_TYPE) {
            aggregator = new IntegerAggregator(gFieldIndex, gbFieldType, aFieldIndex, aop);
        }
        else {
            aggregator = new StringAggregator(gFieldIndex, gbFieldType, aFieldIndex, aop);
        }
        while (children[0].hasNext()) {
            aggregator.mergeTupleIntoGroup(children[0].next());
        }
        aggIterator = aggregator.iterator();
        aggIterator.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // TODO: some code goes here
        if(aggIterator.hasNext()) {
            return aggIterator.next();
        }
        return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        this.close();
        this.open();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * <p>
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        Type[] types;
        String[] fieldNames;
        if(gFieldIndex == Aggregator.NO_GROUPING) {
            types = new Type[]{tupleDesc.getFieldType(aFieldIndex)};
            fieldNames = new String[]{tupleDesc.getFieldName(aFieldIndex)};
        }
        else {
            types = new Type[]{tupleDesc.getFieldType(gFieldIndex), tupleDesc.getFieldType(aFieldIndex)};
            fieldNames = new String[]{tupleDesc.getFieldName(gFieldIndex), tupleDesc.getFieldName(aFieldIndex)};
        }
        return new TupleDesc(types, fieldNames);
    }

    public void close() {
        // TODO: some code goes here
        super.close();
        aggIterator.close();
        children[0].close();
    }

    @Override
    public OpIterator[] getChildren() {
        // TODO: some code goes here
        return children;
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // TODO: some code goes here
        this.children = children;
    }

}
