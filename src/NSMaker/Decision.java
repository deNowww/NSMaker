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

public class Decision implements Node {
    public String text;
    public LeftBranch left;
    public RightBranch right;
    private int width = 200;
    public NodeList parent;
    public int visualOffsetY = 0;
    
    public Decision(String text, String leftText, String rightText, NodeList parent) {
        this.parent = parent;
        this.text = text;
        this.left = new LeftBranch(leftText, this);
        this.right = new RightBranch(rightText, this);
    }
    
    public Decision(JsonObject obj, NodeList parent) {
        this.parent = parent;
        this.text = (String) obj.get("text");
        this.left = new LeftBranch((JsonObject) obj.get("left"), this);
        this.right = new RightBranch((JsonObject) obj.get("right"), this);
    }
    
    public boolean equals(Decision d) {
        return this.text.equals(d.text) && this.left.equals(d.left) && this.right.equals(d.right);
    }
    
    public String toString() {
        return "Decision: " + this.text + " " + left.toString() + " " + right.toString();
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
        obj.put("type", "decision");
        obj.put("text", this.text);
        obj.put("left", this.left);
        obj.put("right", this.right);
        obj.toJson(writer);
    }
    
    @Override
    public Group draw() {
        return this.draw(false);
    }
    
    public Group draw(boolean excludeHitboxes) {
        SVGPath path = new SVGPath();
        path.setContent("m0 0 l" + width + " 0 l -" + width/2 + " 70 z");
        path.setStroke(Color.BLACK);
        path.setStrokeWidth(1.0);
        path.setFill(Color.WHITE);
        TextField text = new TextField(this.text);
        text.setPrefWidth(width/2.0);
//        text.setStyle("-fx-background-color: #eee; -fx-background-insets: 0; -fx-background-radius: 0;");
        text.setLayoutY(4);
        text.setLayoutX(width/4.0);
        text.setAlignment(Pos.CENTER);
        text.textProperty().addListener(((observableValue, oldValue, newValue) -> this.text = newValue));
        Group rightGroup = right.draw(excludeHitboxes);
        Group leftGroup = left.draw(excludeHitboxes);
        Group drawGroup = new Group();
        drawGroup.setOnMouseClicked(mouseEvent -> Main.getController().clickedOnNode(this, mouseEvent));
        drawGroup.getChildren().addAll(path, text, leftGroup, rightGroup);
        drawGroup.setTranslateY(visualOffsetY);
        return drawGroup;
    }
    
    public int depth() {
        return this.depth(0);
    }
    
    public int depth(int base) {
        return Math.max(left.depth(base+1), right.depth(base+1));
    }
    
    @Override
    public void setParent(NodeList parent) {
        this.parent = parent;
    }
    
    @Override
    public int getDefaultWidth() {
        return 200;
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
        this.left.setWidth(width/2);
        this.right.setWidth(width/2);
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
        Decision snapshot = new Decision(this.text, this.left.text, this.right.text, snapshotParent);
        snapshot.left = this.left.snapshot(snapshot);
        snapshot.right = this.right.snapshot(snapshot);
        snapshot.width = this.width;
        snapshot.visualOffsetY = this.visualOffsetY;
        return snapshot;
    }
    
    public void setWidthFromChildWidth(int width, Node exclude) {
        this.width = width * 2;
//        System.out.println(this + " " + width * 2);
        if (this.left != exclude && !this.left.recursivelyContains(exclude)) {
            this.left.setWidthFromChildWidth(width, this);
        }
        if (this.right != exclude && !this.right.recursivelyContains(exclude)) {
            this.right.setWidthFromChildWidth(width, this);
        }
    }
    
    public ArrayList<Hitbox> getAllHitboxes() {
        ArrayList<Hitbox> all = new ArrayList<>();
        all.addAll(left.getAllHitboxes());
        all.addAll(right.getAllHitboxes());
        return all;
    }
}
