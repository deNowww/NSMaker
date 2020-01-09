package NSMaker;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import javafx.scene.Group;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Root extends NodeList implements Node {
    // todo: may need to make these private to do more stuff with setter
    public double x;
    public double y;
    private NodeList parent;
    public int visualOffsetY = 0;
    
    public Root(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }
    
    public Root(NodeList nodes) {
        super();
        this.addAll(nodes);
    }
    
    public Root() {
        super();
    }
    
    public Root(JsonObject obj) {
        super((JsonArray) obj.get("content"));
        this.x = ((BigDecimal) obj.get("x")).doubleValue();
        this.y = ((BigDecimal) obj.get("y")).doubleValue();
    }
    
    @Override
    public String toJson() {
        final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return writable.toString();
    }
    
    public List<Node> getContent() {
        return super.getThis();
    }
    
    @Override
    public void toJson(Writer writer) throws IOException {
//        JsonArray content = super.toJSONArray();
        JsonObject obj = new JsonObject();
        obj.put("type", "root");
        obj.put("x", this.x);
        obj.put("y", this.y);
        obj.put("content", this.getContent());
        obj.toJson(writer);
    }
    
    public boolean equals(Root r) {
        return super.equals(r) && this.x == r.x && this.y == r.y;
    }
    
    public String toString() {
        return "Root node at (" + this.x + ", " + this.y + ") - children: " + super.toString();
    }
    
    @Override
    public Group draw() {
        Group group = super.draw();
        group.setLayoutX(this.x);
        group.setLayoutY(this.y);
        
        return group;
    }
    
    @Override
    public int getDefaultHitboxWidth() {
        return 200;
    }
    
    @Override
    public void setParent(NodeList parent) {
        this.parent = parent;
    }
    
    @Override
    public int getDefaultWidth() {
        return 0;
    }
    
    @Override
    public NodeList getParent() {
        return this.parent;
    }
    
    @Override
    public int getWidth() {
        return 0;
    }
    
    @Override
    public void setWidth(int width) { }
    
    @Override
    public ArrayList<Node> getPath() {
        return new ArrayList<>();
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
        Root snapshot = new Root(this.x, this.y);
        snapshot.parent = snapshotParent;
        snapshot.addAll(super.snapshot());
        for (Node node : snapshot) {
            node.setParent(snapshot);
        }
        return snapshot;
    }
}
