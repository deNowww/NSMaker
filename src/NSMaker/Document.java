package NSMaker;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Document extends NodeList {
    private File file;
//    private UndoHistoryTree<Document> history;
    private ArrayList<Document> history = new ArrayList<>();
    private int historyLocation = 0;
    private boolean isSaved = true;
    private static Document document;
    public static double DOCUMENT_SIZE = 1000000.0;  // would have done integer max but floating point errors fuck me if i do that
    
    public static File getFile() { return document.file; }
    public static void setFile(File file) { document.file = file; }
    
    public static boolean isSaved() {
        return document.isSaved;
    }
    public static void setSaved(boolean saved) {
        document.isSaved = saved;
    }
    
    protected Document() {
        super();
//        this.history = new UndoHistoryTree<>();
    }
    
    private Document(File file, JsonArray arr) {
        super(arr);
        this.file = file;
        Document.updateWidths();
    }
    
//    public static UndoHistoryTree<Document> getHistory() {
//        return document.history;
//    }
    
    public static ArrayList<Document> getHistory() {
        return document.history;
    }
    
    public static Document get() {
        return document;
    }
    
    public static void addCurrentStateToHistory() {
//        UndoHistoryTree<Document> added = document.history.addChild(Document.snapshotDocument());
//        document.history = added;
        document.historyLocation += 1;
        document.history.add(document.historyLocation, Document.snapshotDocument());
        for (int i=document.historyLocation+1; i<document.history.size(); ) {
            document.history.remove(i);
        }
        document.isSaved = false;
    }
    
    private static void silentAddCurrentStateToHistory() {
        document.history.add(document.historyLocation, Document.snapshotDocument());
    }
    
    public static void setTo(Document newDocument) {
        document.clear();
        document.addAll(newDocument);
        for (Node root : document) {
            root.setParent(document);
        }
    }
    
    public static int undo() {
//        UndoHistoryTree<Document> parent = document.history.getParent();
//        System.out.println(document.history);
//        System.out.println(parent.getContent());
//        document = parent.getContent();
//        document.history = parent;
        document.historyLocation -= 1;
        try {
            Document.setTo(document.history.get(document.historyLocation));
        } catch (IndexOutOfBoundsException ignored) { }
        return document.historyLocation;
    }
    
    public static int redo() {
        document.historyLocation += 1;
        try {
            Document.setTo(document.history.get(document.historyLocation));
        } catch (IndexOutOfBoundsException ignored) { }
        return document.historyLocation;
    }
    
    public static Document snapshotDocument() {
        Document snapshot = new Document();
        snapshot.addAll(document.snapshot());
        return snapshot;
    }
    
    public static Node getIndex(int index) {
        return document.get(index);
    }
    
    public static boolean isNull() {
        return document == null;
    }
    public static boolean documentIsEmpty() {
        return document.isEmpty();
    }
    
    public static void addNewRoot(double x, double y, Node node) {
        if (node instanceof Root) {
            ((Root) node).x = x;
            ((Root) node).y = y;
            document.add(node);
        } else {
            Root root = new Root(x, y);
            root.add(node);
            document.add(root);
        }
    }
    
    public static Pane getEditorView() {
        Pane pane = new Pane();
        Group group = new Group();
        Document.updateWidths();
        
        pane.setMinSize(DOCUMENT_SIZE, DOCUMENT_SIZE);
        pane.getStyleClass().add("editor-content");
        
        for (Node node : document) {
            group.getChildren().add(node.draw());
        }
        
        pane.getChildren().add(group);
        return pane;
    }
    
    public static void create() {
        document = new Document();
        document.history.clear();
        document.historyLocation = 0;
        Document.silentAddCurrentStateToHistory();
    }
    
    public static void fromFile(File file) throws FileNotFoundException, JsonException {
        FileReader fileReader = new FileReader(file);
        JsonArray objects = Jsoner.deserializeMany(fileReader);
        JsonArray o = (JsonArray) objects.get(0);
    
        document = new Document(file, o);
        Document.silentAddCurrentStateToHistory();
    }
    
    public static void toFile(File file) {
        List<Node> json = document;
        
        try (FileWriter fileWriter = new FileWriter(file)) {
            Jsoner.serialize(json, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean removeRoot(Root root) {
        return document.remove(root);
    }
    
    public static void updateWidths() {
        for (Node node : document) {
            Root root = (Root) node;
            Node deepest = root.getDeepestLargestNode();
            System.out.println("deepest: " + deepest);
            ArrayList<Node> path = deepest.getPath();
            deepest.setWidth(deepest.getDefaultWidth());
    
//            System.out.println(path.get(path.size() - 1).getParent().size());
//            System.out.println(path.get(path.size() - 1).getParent().getClass());
            System.out.println((path.get(path.size() - 1)));
            System.out.println(node);
    
            if (path.size() > 0) {
                if (path.get(0) instanceof NodeList) {
                    ((NodeList) path.get(0)).setWidthFromChildWidth(deepest.getDefaultWidth(), deepest);
                } else if (path.get(0) instanceof Decision) {
                    ((Decision) path.get(0)).setWidthFromChildWidth(deepest.getDefaultWidth(), deepest);
                }
                for (int i = 1; i < path.size(); i++) {
                    if (path.get(i) instanceof NodeList) {
                        ((NodeList) path.get(i)).setWidthFromChildWidth(path.get(i - 1).getWidth(), deepest);
                    } else if (path.get(i) instanceof Decision) {
                        ((Decision) path.get(i)).setWidthFromChildWidth(path.get(i - 1).getWidth(), deepest);
                    }
                }
            }
        }
    }
    
    public static Point2D minXY() {
        double minX = Document.DOCUMENT_SIZE;
        double minY = Document.DOCUMENT_SIZE;
        for (Node node : document) {
            Root root = (Root) node;
            if (root.x < minX) {
                minX = root.x;
            }
            if (root.y < minY) {
                minY = root.y;
            }
        }
        return new Point2D(minX, minY);
    }
    
    public static Point2D maxXY() {
        double maxX = 0;
        double maxY = 0;
        for (Node node : document) {
            Root root = (Root) node;
            if (root.x > maxX) {
                maxX = root.x;
            }
            if (root.y > maxY) {
                maxY = root.y;
            }
        }
        return new Point2D(maxX, maxY);
    }
    
    public String toString() {
        return "Document: " + super.toString();
    }
}
