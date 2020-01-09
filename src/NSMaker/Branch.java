package NSMaker;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import javafx.scene.Group;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Branch extends NodeList implements Node {
    public String text;
    public int width = 100;
    public Decision parent;
    
    public Branch(String text, Decision parent) {
        super();
        this.text = text;
        setParent(parent);
    }
    
    public Branch(JsonObject obj, Decision parent) {
        this.text = (String) obj.get("text");
        setParent(parent);
        // getting around super having to be first statement
        NodeList tmpList = NodeList.fromJsonArray((JsonArray) obj.get("content"), this);
//        for (Node node : tmpList) {
//            node.setParent(this);
//            this.add(node);
//        }
    }
    
    public Branch() {
        super();
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
//        JsonArray content = super.toJSONArray();
    
        JsonObject obj = new JsonObject();
        obj.put("text", this.text);
        obj.put("content", this.getContent());
        obj.toJson(writer);
    }
    
    public List<Node> getContent() {
        return super.getThis();
    }
    
    public boolean equals(Branch b) {
        return super.equals(b) && this.text.equals(b.text);
    }
    
    public String toString() {
        return this.text + ": " + super.toString();
    }
    
    @Override
    public Group draw() {
        return super.draw();
    }
    
    public double getContentHeight() {
        double height = super.draw(true).prefHeight(0.0) - 2;
        return height > 20 ? height : 0;
    }
    
    @Override
    public int getDefaultHitboxWidth() {
        return this.width - 10;
    }
    
    @Override
    public int getHitboxOffsetY() {
        return 0;
    }
    
    @Override
    public void setParent(NodeList parent) { }
    
    public void setParent(Decision parent) {
        this.parent = parent;
    }
    
    @Override
    public int getDefaultWidth() {
        return 100;
    }
    
    @Override
    public NodeList getParent() {
        return this.parent.getParent();
    }
    
    public Decision getRealParent() { // you're not my real dad!
        return this.parent;
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public void setWidth(int width) {
        this.width = width;
    }
    
    @Override
    public ArrayList<Node> getPath() {
        Node parent = this.getRealParent();
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
    
    }
    
    @Override
    public int getVisualOffsetY() {
        return 0;
    }
    
    @Override
    public Node snapshot(NodeList snapshotParent) {
        return null;
    }
    
    @Override
    public void setWidthFromChildWidth(int width, Node exclude) {
        super.setWidthFromChildWidth(width, exclude);
        this.width = width;
//        System.out.println(this + " " + width);
        if (this.parent != exclude) {
            this.parent.setWidthFromChildWidth(width, this);
        }
    }
}
