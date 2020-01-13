package NSMaker;

import com.github.cliftonlabs.json_simple.JsonObject;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class Action implements Node {
    public String text;
    public int width = 200;
    public NodeList parent;
    private String oldText;
    
    private int visualOffsetY = 0;
    
    public Action(String text, NodeList parent) {
        this.text = text;
        this.setParent(parent);
    }
    
    public Action(JsonObject obj, NodeList parent) {
        this.text = (String) obj.get("text");
        this.setParent(parent);
    }
    
    @Override
    public String toJson() {
        final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);
        } catch (final IOException ignored) { }
        return writable.toString();
    }
    
    @Override
    public void toJson(Writer writer) throws IOException {
        JsonObject obj = new JsonObject();
        obj.put("type", "action");
        obj.put("text", text);
        obj.toJson(writer);
    }
    
    public String toString() {
        return "Action: " + this.text;
    }
    
    @Override
    public Group draw() {
        SVGPath path = new SVGPath();
        path.setContent("m0 0 l" + width + " 0 l0 70 l-" + width + " 0 z");
        path.setStroke(Color.BLACK);
        path.setStrokeWidth(1.0);
        path.setFill(Color.WHITE);
        TextField text = new TextField(this.text);
        text.setPrefWidth(width - 20);
//        text.setStyle("-fx-background-color: #eee; -fx-background-insets: 0; -fx-background-radius: 0;");
        text.setLayoutY(20);
        text.setLayoutX(10);
        text.setAlignment(Pos.CENTER);
        text.textProperty().addListener(((observableValue, oldValue, newValue) -> this.text = newValue));
        text.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (newValue) {
                this.oldText = text.getText();
            } else {
                Main.getController().addCurrentStateToHistory(String.format("Name: %s -> %s", this.oldText, this.text));
            }
        }));
        Group drawGroup = new Group();
        drawGroup.getChildren().add(path);
        drawGroup.getChildren().add(text);
        drawGroup.setTranslateY(visualOffsetY);
        System.out.println(visualOffsetY);
        drawGroup.setOnMouseClicked(mouseEvent -> Main.getController().clickedOnNode(this, mouseEvent));
        return drawGroup;
    }
    
    @Override
    public void setParent(NodeList parent) {
        this.parent = parent;
    }
    
    @Override
    public NodeList getParent() {
        return this.parent;
    }
    
    @Override
    public int getDefaultWidth() {
        return 200;
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public void setWidth(int width) {
//        System.out.println(this + " " + width);
        this.width = width;
    }
    
    @Override
    public ArrayList<Node> getPath() {
        System.out.println("this: " + this);
        Node parent = (Node)this.getParent();
        ArrayList<Node> path = new ArrayList<>();
        System.out.println(String.format("this: %s; first parent: %s", this, parent));
        while (!(parent instanceof Root)) {
            path.add(parent);
            if (parent instanceof Branch) {
                parent = ((Branch) parent).getRealParent();
            } else {
                parent = (Node)parent.getParent();
            }
        }
        path.add(parent);
        return path;
    }
    
    @Override
    public void setVisualOffsetY(int offset) {
        this.visualOffsetY = offset;
    }
    
    @Override
    public int getVisualOffsetY() {
        return this.visualOffsetY;
    }
    
    @Override
    public Node snapshot(NodeList snapshotParent) {
        Action snapshot = new Action(this.text, snapshotParent);
        snapshot.width = this.width;
        snapshot.visualOffsetY = this.visualOffsetY;
        return snapshot;
    }
    
    @Override
    public String minimalToString() {
        return "Action (" + this.text + ")";
    }
    
}
