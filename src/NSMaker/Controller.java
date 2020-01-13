package NSMaker;

import com.github.cliftonlabs.json_simple.JsonException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.event.DocumentEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {
    
    @FXML public MenuItem undoBtn;
    @FXML public MenuItem redoBtn;
    @FXML public Menu mUndoHistoryTree;
    @FXML private TextField sidebarIconAction;
    @FXML private TextField sidebarIconDecision;
    @FXML private TextField sidebarIconTopLoop;
    @FXML private TextField sidebarIconBotLoop;
    @FXML private GridPane sidebar;
    @FXML public ScrollPane editor;
    @FXML private FlowPane sidebarAction;
    @FXML private FlowPane sidebarDecision;
    @FXML private FlowPane sidebarLoopTop;
    @FXML private FlowPane sidebarLoopBot;
    @FXML private Group holdingGroup;
    
    private Node holding;
    
    private Hitbox separated;
    private Hoverbox hoverbox;
    
    private Stage exportWindow = null;
    
    public Controller() { }
    
    @FXML
    private void initialize() {
        initSidebar();
        initEditor();
        initHoverboxEventFilter();
    }
    
    private void initHoverboxEventFilter() {
        // need the hoverbox to only reattach when the mouse exits its bounds, so MOUSE_EXITED is insufficient
        editor.getContent().addEventFilter(MouseEvent.MOUSE_MOVED, mouseEvent -> {
//            System.out.println(String.format("hov: %s; sep: %s", hoverbox, separated));
            if (hoverbox != null && !hoverbox.contains(mouseEvent.getX(), mouseEvent.getY())) {
                reattach();
            }
        });
    }
    
    private void initSidebar() {
        Rectangle clipRectAction = new Rectangle(sidebarAction.getWidth(), sidebarAction.getHeight());
        clipRectAction.heightProperty().bind(sidebarAction.heightProperty());
        clipRectAction.widthProperty().bind(sidebarAction.widthProperty());
        sidebarAction.setClip(clipRectAction);
        deselect(sidebarIconAction);
    
        Rectangle clipRectDecision = new Rectangle(sidebarDecision.getWidth(), sidebarDecision.getHeight());
        clipRectDecision.heightProperty().bind(sidebarDecision.heightProperty());
        clipRectDecision.widthProperty().bind(sidebarDecision.widthProperty());
        sidebarDecision.setClip(clipRectDecision);
        deselect(sidebarIconDecision);
    
        Rectangle clipRectLoopTop = new Rectangle(sidebarLoopTop.getWidth(), sidebarLoopTop.getHeight());
        clipRectLoopTop.heightProperty().bind(sidebarLoopTop.heightProperty());
        clipRectLoopTop.widthProperty().bind(sidebarLoopTop.widthProperty());
        sidebarLoopTop.setClip(clipRectLoopTop);
        deselect(sidebarIconTopLoop);
    
        Rectangle clipRectLoopBot = new Rectangle(sidebarLoopBot.getWidth(), sidebarLoopBot.getHeight());
        clipRectLoopBot.heightProperty().bind(sidebarLoopBot.heightProperty());
        clipRectLoopBot.widthProperty().bind(sidebarLoopBot.widthProperty());
        sidebarLoopBot.setClip(clipRectLoopBot);
        deselect(sidebarIconBotLoop);
    }
    
    // thanks @gearquicker - https://stackoverflow.com/a/52310277/8457833
    public static void deselect(TextField textField) {
        Platform.runLater(() -> {
            if (textField.getText().length() > 0 &&
                        textField.selectionProperty().get().getEnd() == 0) {
                deselect(textField);
            }else{
                textField.selectEnd();
                textField.deselect();
            }
        });
    }
    
    private void initEditor() {
        Pane content = new Pane();
        content.getStyleClass().add("editor-content");
        content.setMinSize(1000000.0, 1000000.0);
        content.setOnMouseClicked(this::mouseClickedOnEditor);
        editor.setContent(content);
        Platform.runLater(editor::requestFocus);
    }
    
    public void updateEditor() {
        Pane content = Document.getEditorView();
        content.setOnMouseClicked(this::mouseClickedOnEditor);
        editor.setContent(content);
        initHoverboxEventFilter();
    
        // don't softlock the app if something fucks up in separation
        if (separated != null) {
            reattach();
        }
    }
    
    @FXML
    public void mouseMoved(MouseEvent mouseEvent) {
        holdingGroup.setTranslateX(mouseEvent.getX());
        holdingGroup.setTranslateY(mouseEvent.getY());
    }
    
    @FXML
    private void actionClicked(MouseEvent mouseEvent) {
        setHolding(new Action(sidebarIconAction.getText(), null));
        mouseEvent.consume();
    }
    
    @FXML
    private void decisionClicked(MouseEvent mouseEvent) {
        setHolding(new Decision(sidebarIconDecision.getText(), "Yes", "No", null));
        mouseEvent.consume();
    }
    
    @FXML
    private void topLoopClicked(MouseEvent mouseEvent) {
        setHolding(new TopLoop(sidebarIconTopLoop.getText(), null));
        mouseEvent.consume();
    }
    
    @FXML
    private void botLoopClicked(MouseEvent mouseEvent) {
        setHolding(new BotLoop(sidebarIconBotLoop.getText(), null));
        mouseEvent.consume();
    }
    
    public void insertAction(ActionEvent actionEvent) {
        setHolding(new Action("Action", null));
    }
    
    public void insertDecision(ActionEvent actionEvent) {
        setHolding(new Decision("Decision", "Yes", "No", null));
    }
    
    public void insertTopLoop(ActionEvent actionEvent) {
        setHolding(new TopLoop("Loop", null));
    }
    
    public void insertBotLoop(ActionEvent actionEvent) {
        setHolding(new BotLoop("Loop", null));
    }
    
    public void setHolding(Node holding) {
        this.holding = holding;
        holdingGroup.getChildren().clear();
        holdingGroup.getChildren().addAll(this.holding.draw().getChildren());
    }
    
    public void removeHolding() {
        this.holding = null;
        holdingGroup.getChildren().clear();
    }
    
    public void separate(Hitbox top, MouseEvent event) {
        if (separated != null) {
            return; // don't try to separate if it's already separated
        }
//        System.out.println(String.format("should be separated from %s", top));
        NodeList parent = top.nodeListParent;
//        System.out.println(String.format("%d hitboxes", parent.getAllHitboxes().size()));


//        int aboveDepth;
//        try {
//            Node above = parent.get(top.index - 1);
//            if (above instanceof NodeList) {
//                aboveDepth = ((NodeList) above).depth();
//            } else if (above instanceof Decision) {
//                aboveDepth = ((Decision) above).depth();
//            } else {
//                aboveDepth = 0;
//            }
//        } catch (IndexOutOfBoundsException e) {
//            aboveDepth = 0;
//        }
//        int belowDepth;
//        try {
//            Node below = parent.get(top.index);
//            if (below instanceof NodeList) {
//                belowDepth = ((NodeList) below).depth();
//            } else if (below instanceof Decision) {
//                belowDepth = ((Decision) below).depth();
//            } else {
//                belowDepth = 0;
//            }
//        } catch (IndexOutOfBoundsException e) {
//            belowDepth = 0;
//        }
//        int depth = Math.max(aboveDepth, belowDepth);
        ArrayList<Hitbox> underMouse = hitboxesUnderPoint(parent.getAllHitboxes(), event.getSceneX(), event.getSceneY());
        if (underMouse.size() <= 1) {
            return; // don't try to separate if there's only one hitbox to separate
        }
        int offset = underMouse.size() * 20;
        System.out.println(String.format("hitboxes under mouse: %d, offset: %d", underMouse.size(), offset));
        System.out.println(String.format("hitboxes under mouse: %s", underMouse));
        boolean shouldUpdate = false;
        for (int i = top.index; i < parent.size(); i++) {
            if (parent.get(i).getVisualOffsetY() == 0) {
                shouldUpdate = true;
                System.out.println(String.format("moving index %d down %dpx", i, offset));
                parent.get(i).setVisualOffsetY(offset);
            }
        }
        
        if (shouldUpdate || top.index >= parent.size()) { // could have this where shouldUpdate is initialised,
            updateEditor();                               // but it's more readable this way
        }
        
        separated = top;
        
        // hoverbox
        Bounds bounds = separated.localToScene(separated.getBoundsInLocal());
        hoverbox = new Hoverbox(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), offset + 20);
        System.out.println(String.format("hoverbox spawned: %s", hoverbox));
//        System.out.println(String.format("%f %f %f %d", bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), offset + 20));
        Pane editorContent = (Pane) editor.getContent();
        editorContent.getChildren().add(hoverbox);
        hoverbox.toBack();
    
        underMouse = hitboxesUnderPoint(parent.getAllHitboxes(), event.getSceneX(), event.getSceneY());
        // index forwards if botloop, otherwise index backwards
        int count;
        int index;
        if (separated.index < separated.nodeListParent.size() && separated.nodeListParent.get(separated.index) instanceof BotLoop) {
            count = 1;
            index = 0;
        } else {
            count = -1;
            index = underMouse.size() - 1;
        }
        for (int i = 0; i < underMouse.size(); i++) {
            Hitbox hitbox = underMouse.get(index);
            System.out.println(String.format("translateY of hitbox %d: %s", i, hitbox.getTranslateY()));
            hitbox.setTranslateY(hitbox.getTranslateY() + (10 + 20 * i));
    
            index += count;
        }
        
        System.out.println(String.format("separated: %s", separated));
    }
    
    private ArrayList<Hitbox> hitboxesUnderPoint(ArrayList<Hitbox> hitboxes, double x, double y) {
        ArrayList<Hitbox> under = new ArrayList<>();
        for (Hitbox hitbox : hitboxes) {
            Bounds localBounds = hitbox.getBoundsInLocal();
            Bounds sceneBounds = hitbox.localToScene(localBounds, true);
            Scene scene = hitbox.getScene();
            
            if (sceneBounds.contains(x, y)) {
                under.add(hitbox);
            }
//            hitbox.setFill(Color.BLACK);
        }
        return under;
    }
    
    public void reattach() {
        NodeList parent = separated.nodeListParent;
        boolean shouldUpdate = false;
        for (int i = separated.index; i < parent.size(); i++) {
            if (parent.get(i).getVisualOffsetY() > 0) {
                shouldUpdate = true;
                System.out.println(String.format("reattaching index %d", i));
                parent.get(i).setVisualOffsetY(0);
            }
        }
        if (shouldUpdate) {
            updateEditor();
        }
        ArrayList<Hitbox> all = parent.getAllHitboxes();
        for (Hitbox hitbox : all) {
            hitbox.setTranslateY(0);
        }
        
        ((Pane)editor.getContent()).getChildren().remove(hoverbox);
        hoverbox = null;
        separated = null;
        System.out.println("reattaching at controller reattach func");
    }
    
    public Node getHolding() {
        return this.holding;
    }
    
    public void clickedOnNode(Node node, MouseEvent mouseEvent) {
        NodeList nodeParent = node.getParent();
        switch (mouseEvent.getButton()) {
            case PRIMARY:
                int index = nodeParent.indexOf(node);
                if (index < nodeParent.size() - 1) { // if it's the last element then it'll be the only one, so treat it as case SECONDARY
                    NodeList removed = new NodeList();
                    for (int i=index; i<nodeParent.size(); i+=0) {
                        Node old = nodeParent.remove(index);
                        removed.silentAdd(old);
                    }
                    if (index == 0 && nodeParent instanceof Root) { // if we're removing from the top of the root
                        Document.removeRoot((Root) nodeParent);
                    }
                    Root newRoot = new Root(removed);
                    setHolding(newRoot);
                    for (Node n : removed) {
                        n.setParent(newRoot);
                    }
                    break;
                }
            case SECONDARY:
                nodeParent.remove(node);
                if (nodeParent instanceof Root && nodeParent.size() == 0) { // 0, not 1, because we just removed from nodeParent
                    System.out.println(Document.removeRoot((Root) nodeParent));
                }
                setHolding(node);
                node.setParent(null);
                break;
        }
        addCurrentStateToHistory();
        updateEditor();
        mouseEvent.consume();
    }
    
    @FXML
    private void newFileAction() {
        Document.create();
        undoBtn.setDisable(true);
        redoBtn.setDisable(true);
        Main.stage.setTitle("Untitled - NSMaker");
        Document.setSaved(true);
        updateEditor();
    }
    
    @FXML
    private void openFileAction() {
        // todo: better error handling
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("NSMaker Files", "*.nsd"));
            File file = fc.showOpenDialog(Main.stage);
            Document.fromFile(file);
            Main.stage.setTitle(Document.getFile().getName() + " - NSMaker");
            Document.setSaved(true);
            updateEditor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) { }
    }
    
    @FXML
    public void mouseClickedOnSidebar(MouseEvent mouseEvent) {
        holding = null;
        holdingGroup.getChildren().clear();
        resetSidebarText();
    }
    
    @FXML
    private void mouseClickedOnEditor(MouseEvent mouseEvent) {
        if (holding != null) {
            Node node = holding;
            Document.addNewRoot(mouseEvent.getX(), mouseEvent.getY(), node);
            holding = null;
            holdingGroup.getChildren().clear();
            addCurrentStateToHistory();
            updateEditor();
        }
    }
    
    private void resetSidebarText() {
        sidebarIconAction.setText("Action");
        sidebarIconDecision.setText("Decision");
        sidebarIconBotLoop.setText("Loop");
        sidebarIconTopLoop.setText("Loop");
        Platform.runLater(editor::requestFocus);
    }
    
    @FXML
    public void saveFileAction() {
        if (Document.isNull() || Document.getFile() == null) {
            saveAsAction();
        } else {
            Document.toFile(Document.getFile());
            Document.setSaved(true);
        }
    }
    
    @FXML
    public void saveAsAction() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("NSMaker Documents", "*.nsd"));
            File file = fc.showSaveDialog(Main.stage);
            Document.toFile(file);
            Document.setFile(file);
            String name = Document.getFile().getName();
            Main.stage.setTitle(name + " - NSMaker");
            Document.setSaved(true);
        } catch (NullPointerException ignored) { }
    }
    
    @FXML
    public void exportFileAction() throws IOException {
        exportWindow = new Stage();
        exportWindow.initModality(Modality.APPLICATION_MODAL);
        exportWindow.initOwner(Main.stage);
    
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("ExportDialog.fxml").openStream());
        Scene exportScene = new Scene(root, 700, 400);
        exportScene.getStylesheets().addAll(this.getClass().getResource("ExportDialog.css").toExternalForm());
        exportWindow.setScene(exportScene);
        exportWindow.setTitle("Export");
        exportWindow.setResizable(false);
        
        exportWindow.show();
    
        Button cancelBtn = ((ExportDialogController) loader.getController()).cancelBtn;
        cancelBtn.setOnAction(event -> {
            exportWindow.close();
            exportWindow = null;
        });
        cancelBtn.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelBtn.fire();
            }
        });
    }
    
    public Stage getExportWindow() {
        return exportWindow;
    }
    
    public void undoAction(ActionEvent actionEvent) {
        Document.undo();
        if (Document.getHistory().getParent() == null) {
            undoBtn.setDisable(true);
        }
        if (Document.getHistory().getChildren().size() > 0) {
            redoBtn.setDisable(false);
        }
        setUndoHistoryTreeMenu();
        updateEditor();
    }
    
    public void redoAction(ActionEvent actionEvent) {
        Document.redo();
        if (Document.getHistory().getParent() != null) {
            undoBtn.setDisable(false);
        }
        if (Document.getHistory().getChildren().size() <= 0) {
            redoBtn.setDisable(true);
        }
        setUndoHistoryTreeMenu();
        updateEditor();
    }
    
    public static int indexOfHistoryTreeMenuItem(MenuItem menuItem) {
        Menu parent = menuItem.getParentMenu();
        int index = 0;
        for (MenuItem item : parent.getItems()) {
            if (item instanceof SeparatorMenuItem) {
                index++;
            }
            if (item == menuItem) {
                break;
            }
        }
        return index;
    }
    
    public UndoHistoryTree<String> getUndoHistoryTreeFromMenuItem(MenuItem menuItem) {
        ArrayList<Integer> indexes = new ArrayList<>();
        indexes.add(indexOfHistoryTreeMenuItem(menuItem));
        Menu parent = menuItem.getParentMenu();
        while (parent != mUndoHistoryTree) {
            indexes.add(0, indexOfHistoryTreeMenuItem(parent));
            parent = parent.getParentMenu();
        }
        UndoHistoryTree<String> tree = Document.getHistory().getRoot();
        for (int index : indexes) {
            tree = tree.getChildren().get(index);
        }
        return tree;
    }
    
    public void setUndoHistoryTreeMenu() {
        mUndoHistoryTree.getItems().clear();
        setUndoHistoryTreeMenu(mUndoHistoryTree, Document.getHistory().getRoot());
    }
    
    public void setUndoHistoryTreeMenu(Menu menu, UndoHistoryTree<String> start) {
        System.out.println(start);
        for (UndoHistoryTree<String> child : start.getChildren()) {
            CheckMenuItem childMenuItem = new CheckMenuItem("Test");
            if (child == Document.getHistory()) {
                childMenuItem.setSelected(true);
            }
            childMenuItem.setOnAction(event -> {
                try {
                    Document.setFromHistory(getUndoHistoryTreeFromMenuItem(childMenuItem));
                    setUndoHistoryTreeMenu();
                    updateEditor();
                } catch (JsonException e) {
                    e.printStackTrace();
                }
            });
            if (child.getChildren().size() > 0) {
                Menu childMenu = new Menu();
                menu.getItems().add(childMenu);
                setUndoHistoryTreeMenu(childMenu, child);
            }/* else {
                setUndoHistoryTreeMenu(menu, child);
            }*/
            menu.getItems().add(childMenuItem);
            menu.getItems().add(new SeparatorMenuItem());
        }
    }
    
    public void addCurrentStateToHistory() {
        if (undoBtn.isDisable()) {
            undoBtn.setDisable(false);
        }
        if (!redoBtn.isDisable()) {
            redoBtn.setDisable(true);
        }
        Document.addCurrentStateToHistory();
        setUndoHistoryTreeMenu();
    }
}
