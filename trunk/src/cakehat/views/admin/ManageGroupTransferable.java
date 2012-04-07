package cakehat.views.admin;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Class used within {@link DnDGroupTree} and {@link DnDStudentList} for drag and
 * drop. It is the object transfered when doing the drag and drop and contains the
 * dataflavor of the component it is coming from
 * @author wyegelwe
 */
class ManageGroupTransferable implements Transferable{

    private final DataFlavor _flavor;

    ManageGroupTransferable(DataFlavor flavor){
        _flavor = flavor;
    }
    @Override
    public Object getTransferData(DataFlavor flavor){
        return "DATA"; //needed to return something =)
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{_flavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
       return true;
    }
}
