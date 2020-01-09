package NSMaker;

import java.util.ArrayList;

/**
 * currently not in use. will use this at a later date.
 */

public class UndoHistoryTree<T> {
    private UndoHistoryTree<T> parent;
    private ArrayList<UndoHistoryTree<T>> children;
    private T content;
    
    public UndoHistoryTree(T content) {
        this.parent = null;
        this.children = new ArrayList<>();
        this.content = content;
    }
    
    public UndoHistoryTree() {
        this.parent = null;
        this.children = new ArrayList<>();
        this.content = null;
    }
    
    private UndoHistoryTree(UndoHistoryTree<T> parent, T content) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.content = content;
    }
    
    
    public void addChild(UndoHistoryTree<T> child) {
        child.setParent(this);
        children.add(child);
    }
 
    public UndoHistoryTree<T> addChild(T childContent) {
        UndoHistoryTree<T> added = new UndoHistoryTree<>(this, childContent);
        children.add(added);
        return added;
    }
    
    public boolean removeChild(UndoHistoryTree<T> child) {
        return children.remove(child);
    }
    
    public UndoHistoryTree<T> getParent() {
        return parent;
    }
    
    private void setParent(UndoHistoryTree<T> parent) {
        this.parent = parent;
    }
    
    private void switchParent(UndoHistoryTree<T> newParent) {
        this.parent.removeChild(this);
        this.parent = newParent;
        this.parent.addChild(this);
    }
    
    public T getContent() {
        return content;
    }
    
    public void setContent(T content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        // todo: better toString method
//        StringBuilder s = new StringBuilder("O\n|\n");
//
//        UndoHistoryTree<T> newParent = this.parent;
//        do {
//            s.insert(0, "o\n|\n");
//        } while ((newParent = newParent.parent) != null);
//
//        return s.toString();
        
        return String.format("content: %s, parent: \n%s", this.content, this.parent);
    }
}
