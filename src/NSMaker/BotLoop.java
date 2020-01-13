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

public class BotLoop extends Loop implements Node {
    
    private NodeList parent;
    public int visualOffsetY = 0;
    private String oldText;
    
    public BotLoop() {
        super();
    }
    
    public BotLoop(String text, NodeList parent) {
        super(text);
        setParent(parent);
    }
    
    public BotLoop(JsonObject obj, NodeList parent) {
        super(obj);
        setParent(parent);
    }
    
    @Override
    public String toJson() {
        final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);
        } catch (final IOException e) {
        }
        return writable.toString();
    }
    
    @Override
    public void toJson(Writer writer) throws IOException {
        JsonObject obj = new JsonObject();
        obj.put("type", "loop-bot");
        obj.put("text", this.text);
        obj.put("content", super.getContent());
        obj.toJson(writer);
    }
    
    @Override
    public Group draw() {
        return this.draw(false);
    }
    
    @Override
    public Group draw(boolean excludeHitboxes) {
        Group content = super.draw(excludeHitboxes);
        content.setLayoutX(20);
        content.setLayoutY(0);
        SVGPath path = new SVGPath();
        int contentHeight = (int) super.draw(true).prefHeight(0.0) - 2;
        double height = contentHeight > 20 ? contentHeight : 70;
        path.setContent("m0 0 l20 0 l0 " + height + " l" + (width - 20) + " 0 l0 47 l-" + width + " 0 z");
        path.setStroke(Color.BLACK);
        path.setStrokeWidth(1.0);
        path.setFill(Color.WHITE);
        TextField text = new TextField(this.text);
        text.setPrefWidth(width - 40);
        text.setStyle("-fx-background-color: #eee; -fx-background-insets: 0; -fx-background-radius: 0;");
        text.setLayoutY(height + 10);
        text.setLayoutX(30);
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
        drawGroup.setOnMouseClicked(mouseEvent -> Main.getController().clickedOnNode(this, mouseEvent));
        drawGroup.getChildren().addAll(path, text, content);
        drawGroup.setTranslateY(visualOffsetY);
        return drawGroup;
    }
    
    @Override
    public int getHitboxOffsetY() {
        return 0;
    }
    
    @Override
    public void setParent(NodeList parent) {
        this.parent = parent;
    }
    
    @Override
    public int getDefaultWidth() {
        return 220;
    }
    
    @Override
    public NodeList getParent() {
        return this.parent;
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.parent.setWidthFromChildWidth(width, this);
    }
    
    @Override
    public ArrayList<Node> getPath() {
        Node parent = (Node)this.getParent();
        ArrayList<Node> path = new ArrayList<>();
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
        BotLoop snapshot = new BotLoop(this.text, snapshotParent);
        snapshot.width = this.width;
        snapshot.visualOffsetY = this.visualOffsetY;
        snapshot.addAll(super.snapshot());
        for (Node node : snapshot) {
            node.setParent(snapshot);
        }
        return snapshot;
    }
    
    @Override
    public String minimalToString() {
        return "Bottom loop (" + this.text + ")";
    }
}
