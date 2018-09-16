import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//DONE
public class DragListener implements DropTargetListener{
    DefaultListModel listModel;
    public DragListener(DefaultListModel list) {
        listModel = list;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable t = dtde.getTransferable();
        DataFlavor [] df = t.getTransferDataFlavors();
        for(DataFlavor f:df) {
            try {
                if(f.isFlavorJavaFileListType()) {

                    List<File> files = (List<File>) t.getTransferData(f);
                    for(File file: files) {
                        if(getFileExtension(file).equals("png") || getFileExtension(file).equals("jpg") || getFileExtension(file).equals("jpeg") || getFileExtension(file).equals("JPG") || getFileExtension(file).equals("PNG") || getFileExtension(file).equals("JPEG")) {
                            listModel.addElement(file.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }


    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".")!= -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+ 1);
        else
            return "";
    }
}