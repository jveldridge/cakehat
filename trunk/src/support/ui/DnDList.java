package support.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 * A specialized {@link GenericJList} which allows for drag and drop between
 * instances of this class. Only other instances which are specified by
 * {@link #addDnDSource(gradesystem.components.DnDList)} will be allowed to
 * drag to this list. Drag and drop may occur within a list to allow for
 * reordering values of the list; to enable this add the instance to itself
 * as a drag and drop source.
 * <br/><br/>
 * Drag and drop between two lists occurs by dragging the selected values from
 * the source list to the destination list. A visual indicator will appear
 * showing where the values will be inserted. Before transferring the values
 * between lists the {@link DnDApprover} for each list, if present, will be
 * checked. First the removal will be checked, if it is allowed then the
 * addition will be checked. If the removal is not approved, then the addition
 * will not be checked. The transfer will occur by removing the values from the
 * source list, adding the values to the destination list, notifying the
 * {@link DnDListener}s of the source list  of the removal, and then notify the
 * {@link DnDListener}s of the destination list of the addition.
 * <br/><br/>
 * Drag and drop within a list occurs by dragging the selected values to
 * somewhere else within the list. Before reordering the values the
 * {@link DnDApprover}, if present, will be checked. If approved the reordering
 * will occur and all {@link DnDListener}s will be notified.
 *
 * @author jak2
 */
public class DnDList<E> extends GenericJList<E>
{
    private final List<DnDListener<E>> _listeners = new ArrayList<DnDListener<E>>();
    private DnDApprover<E> _approver;
    private GenericMutableListModel<E> _model;

    //Swing's Drag & Drop (DnD) support operates by having each drag and drop
    //element define the formats it will transfer data in. So for instance an
    //image might support being a BufferedImage class, a JPG file, or a String
    //description depending on the situation.
    //In this case DataFlavor is going to be used to not actually describe the
    //values being dragged between lists, but instead an identifier of the list
    //from which the values belong at the beginning of the drag. That identifier
    //will be an integer which is a key in _dndSources. This is some what of an
    //abuse of what DataFlavor is intended to be used for, but very well
    //satifies the needs of DnDList.
    private static final DataFlavor DATA_FLAVOR = new DataFlavor(Integer.class, "id");
    private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] { DATA_FLAVOR };

    //Map of identifier to DnDLists that can drop data into this one
    private final HashMap<Integer, DnDList<E>> _dndSources = new HashMap<Integer, DnDList<E>>();

    //To ensure each _id is unique (technically could break down from
    //multithreading, but Swing is not thread safe anyway)
    private static int LAST_ALLOCATED_ID = 0;
    
    //The id of this list, used by _dndSources
    private final int _id = LAST_ALLOCATED_ID++;

    public DnDList(E... values)
    {
        this(Arrays.asList(values));
    }

    public DnDList(Iterable<E> values, StringConverter<E> converter)
    {
        this(values);

        this.setStringConverter(converter);
    }

    public DnDList(Iterable<E> values)
    {
        super(values);

        this.setDragEnabled(true);
    }

    /**
     * Enabled by default.
     * <br/><br/>
     * Enables or disables dragging between this <code>DnDList</code> and any
     * other <code>DnDList</code>s added via
     * {@link #addDnDSource(gradesystem.components.DnDList)}.
     */
    @Override
    public void setDragEnabled(boolean enabled)
    {
        super.setDragEnabled(enabled);
        if(enabled)
        {
            this.setDropMode(DropMode.INSERT);
            this.setTransferHandler(new ListTransferHandler());
        }
    }

    /**
     * Handles transferring between two drag and drop enabled components. In
     * particular, this handler will only allow transfers from those that
     * have been added via {@link #addDnDSource(gradesystem.components.DnDList)}
     * and that are allowed by the {@link #_approver}.
     */
    private class ListTransferHandler extends TransferHandler
    {
        private final Transferable _dndTransferable = new Transferable()
        {
            @Override
            public DataFlavor[] getTransferDataFlavors()
            {
                return DATA_FLAVORS;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor df)
            {
                return DATA_FLAVOR.equals(df);
            }

            //The object returned from this method will be serialized and then
            //later deserialized meaning it will not literally be the same object
            //as such this cannot directly return the source DnDList, just an
            //identifier for it
            @Override
            public Integer getTransferData(DataFlavor df)
            {
                return DnDList.this._id;
            }
        };

        @Override
        public int getSourceActions(JComponent c)
        {
            return TransferHandler.MOVE;
        }

        //The data that is to be transferred to the recipient - it will undergo
        //serialization, and later, deserializaiton
        @Override
        protected Transferable createTransferable(JComponent c)
        {
            return _dndTransferable;
        }

        //Whether importing to this list is allowed
        //This is implemented such that true will only be returned if the source
        //is a list that has been added as a DnD source
        @Override
        public boolean canImport(TransferHandler.TransferSupport support)
        {
            boolean isValidSource = false;

            Transferable transferable = support.getTransferable();
            try
            {
                Object sourceId = transferable.getTransferData(DATA_FLAVOR);
                isValidSource = (DnDList.this.getSourceFromId(sourceId) != null);
            }
            //If either of these exceptions are raised, let false be returned
            catch (UnsupportedFlavorException ex) { }
            catch (IOException ex) { }

            return isValidSource;
        }

        //Called when the "drop" has occurred and the transfer is to be handled
        @Override
        public boolean importData(TransferHandler.TransferSupport support)
        {
            Transferable transferable = support.getTransferable();
            try
            {
                Object srcId = transferable.getTransferData(DATA_FLAVOR);
                DnDList<E> src = DnDList.this.getSourceFromId(srcId);

                //If the source is not an approved source drag and drop list
                if(src == null)
                {
                    return false;
                }

                //Indices, and the values at them, to be moved from the source
                //list to this list
                Map<Integer, E> selectedValuesMap = src.getGenericSelectedValuesMap();

                //Values to be moved from the source list to this list
                List<E> selectedValues = src.getGenericSelectedValues();

                //Use selected indices to remove values from the source
                //list, doing this with indices instead of values ensures
                //that if there are duplicates in the list, the proper ones
                //are removed
                int[] srcIndices = src.getSelectedIndices();

                //Reordering values within the same list
                if(src == DnDList.this)
                {
                    //Determine if transfer is allowed
                    boolean canTransfer = true;
                    if(DnDList.this.getDnDApprover() != null)
                    {
                        canTransfer = DnDList.this.getDnDApprover().canReorderValues(selectedValuesMap);
                    }

                    if(canTransfer)
                    {
                        //Remove
                        DnDList.this._model.removeElementsAt(srcIndices);

                        //Add
                        int dstIndex = this.getDropIndex(support);
                        //For each source index that came before the destination index, the destination index will need
                        //to be decremented by one because the destination index reflects the state of the list at the
                        //time of the drop - but this insertion occurs after the removal, so this will shift the
                        //insertion index appropriately
                        int decrementBy = 0;
                        for(int srcIndex : srcIndices)
                        {
                            if(dstIndex > srcIndex)
                            {
                                decrementBy++;
                            }
                        }
                        dstIndex -= decrementBy;
                        DnDList.this._model.insertElementsAt(selectedValues, dstIndex);
                        
                        //Select the indices that were just moved as they were selected before the move
                        int[] dstIndices = new int[srcIndices.length];
                        for(int i = 0; i < srcIndices.length; i++)
                        {
                            dstIndices[i] = dstIndex + i;
                        }
                        DnDList.this.setSelectedIndices(dstIndices);

                        //Notify, build immutable map of indices inserted at to values
                        Map<Integer, E> reorderedMap = new HashMap<Integer, E>();
                        int currIndex = dstIndex;
                        for(E value : selectedValues)
                        {
                            reorderedMap.put(currIndex, value);
                            currIndex++;
                        }
                        DnDList.this.notifyDnDListenersOfReorder(Collections.unmodifiableMap(reorderedMap));
                    }
                }
                //Transferring values between lists
                else
                {
                    Map<Integer, E> dstValuesMap = new HashMap<Integer, E>();
                    //Build the destination indices
                    int dstIndex = this.getDropIndex(support);
                    int[] dstIndices = new int[selectedValues.size()];
                    for(int i = 0; i < dstIndices.length; i++)
                    {
                        dstIndices[i] = dstIndex + i;
                        dstValuesMap.put(i, selectedValues.get(i));
                    }

                    //Determine if transfer is allowed
                    boolean canTransfer = true;
                    if(src.getDnDApprover() != null)
                    {
                        canTransfer = src.getDnDApprover().canRemoveValues(selectedValuesMap);
                    }
                    if(canTransfer && (DnDList.this.getDnDApprover() != null))
                    {
                        canTransfer = DnDList.this.getDnDApprover().canAddValues(Collections.unmodifiableMap(dstValuesMap));
                    }

                    if(canTransfer)
                    {
                        //Remove
                        src._model.removeElementsAt(srcIndices);

                        //Add
                        DnDList.this._model.insertElementsAt(selectedValues, dstIndex);

                        //Notify removal
                        src.notifyDnDListenersOfRemoval(selectedValues);

                        //Notify addition, build immutable map of indices inserted at to values
                        Map<Integer, E> addedMap = new HashMap<Integer, E>();
                        int currIndex = dstIndex;
                        for(E value : selectedValues)
                        {
                            addedMap.put(currIndex, value);
                            currIndex++;
                        }
                        DnDList.this.notifyDnDListenersOfAddition(Collections.unmodifiableMap(addedMap));
                    }
                }
            }
            //If either of these exceptions are raised, let false be returned
            catch (UnsupportedFlavorException ex) { }
            catch (IOException ex) { }

            return false;
        }

        /**
         * Determines the index of the list that the drag and drop "dropped"
         * at.
         *
         * @param support
         * @return
         */
        private int getDropIndex(TransferHandler.TransferSupport support)
        {
            int dstIndex;

            //Using JList's location locationToIndex(...) method and passing in
            //support.getDropLocation().getDropPoint() does not work properly
            //when the location is beyond the end of the list - in that case the
            //index is always reported as the last index in the list, not one
            //past the index in the list - and there is no easy way to determine
            //when this is the correct behavior. Asking the DropLocation for its
            //index, after casting, will report an index beyond the current
            //indices of the list - which is the correct behavior.

            //The drop location should always be a JList.DropLocation, but check
            if(support.getDropLocation() instanceof JList.DropLocation)
            {
                dstIndex = ((JList.DropLocation) support.getDropLocation()).getIndex();
            }
            //The above block should always be entered, but just in case, fall
            //back to this method which will always succeed but suffers from
            //the problem described above
            else
            {
                dstIndex = DnDList.this.locationToIndex(support.getDropLocation().getDropPoint());
            }

            return dstIndex;
        }
    }

    /**
     * Adds a <code>DnDList</code> that can drag values to this list.
     * <br/><br/>
     * To drag and drop within a list, add the list as a source.
     *
     * @param source
     */
    public void addDnDSource(DnDList<E> source)
    {
        _dndSources.put(source._id, source);
    }

    /**
     * Removes a <code>DnDList</code> that can drag values to this list.
     *
     * @param source
     */
    public void removeDnDSource(DnDList<E> source)
    {
        _dndSources.remove(source._id);
    }

    /**
     * Adds the <code>listener</code> so that it will be notified of drag
     * events.
     *
     * @param listener
     */
    public void addDnDListener(DnDListener<E> listener)
    {
        _listeners.add(listener);
    }

    /**
     * Removes the <code>listener</code> so that it is no longer notified of
     * drag events.
     *
     * @param listener
     */
    public void removeDnDListener(DnDListener<E> listener)
    {
        _listeners.remove(listener);
    }

    /**
     * Notifies all listeners that values have been removed from this list.
     *
     * @param removed
     */
    private void notifyDnDListenersOfRemoval(List<E> removed)
    {
        for(DnDListener<E> listener : _listeners)
        {
            listener.valuesRemoved(removed);
        }
    }

    /**
     * Notifies all listeners that values have been added to this list.
     * 
     * @param added
     * @param indices
     */
    private void notifyDnDListenersOfAddition(Map<Integer, E> added)
    {
        for(DnDListener<E> listener : _listeners)
        {
            listener.valuesAdded(added);
        }
    }

    /**
     * Notifies all listeners that values have been reordered in this list.
     *
     * @param reordered
     */
    private void notifyDnDListenersOfReorder(Map<Integer, E> reordered)
    {
        for(DnDListener<E> listener : _listeners)
        {
            listener.valuesReordered(reordered);
        }
    }

    /**
     * Sets the DnDApprover to <code>approver</code>. Replaces any existing
     * DnDApprover. May be set to <code>null</code> to automatically approve
     * all transfers.
     *
     * @param approver may be <code>null</code>
     */
    public void setDnDApprover(DnDApprover<E> approver)
    {
        _approver = approver;
    }

    /**
     * Returns the existing DnDApprover. If none has been set then the default
     * value of <code>null</code> will be returned.
     *
     * @return
     */
    public DnDApprover<E> getDnDApprover()
    {
        return _approver;
    }

    /**
     * Overridden to use a mutable list model as necessitated by drag and drop.
     *
     * @param values
     * @return
     */
    @Override
    protected GenericListModel<E> buildModel(Iterable<E> values)
    {
        _model = new GenericMutableListModel<E>(values);

        return _model;
    }
    
    @Override
    public GenericMutableListModel<E> getModel()
    {
        return _model;
    }

    /**
     * Helper method that finds the DnDList with <code>id</code>, or
     * <code>null</code> if no match is found.
     *
     * @param id
     * @return
     */
    private DnDList<E> getSourceFromId(Object id)
    {
        //Keys in _dndSources are of type Integer, so if id is not actually an
        //Integer than no match will be found
        return _dndSources.get(id);
    }

    /**
     * Sorts the elements of the list according to the <code>comparator</code>.
     *
     * @param comparator
     */
    public void sort(Comparator<E> comparator)
    {
        _model.sortElements(comparator);
    }
}