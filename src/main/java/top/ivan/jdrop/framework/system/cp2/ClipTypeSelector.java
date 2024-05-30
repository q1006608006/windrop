package top.ivan.jdrop.framework.system.cp2;


import java.awt.datatransfer.Transferable;

public interface ClipTypeSelector {

    ClipType select(Transferable ts);

}
