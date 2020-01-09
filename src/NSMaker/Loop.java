package NSMaker;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.List;

public class Loop extends NodeList {
    public String text;
    public int width = 220;
    
    public Loop(String text) {
        super();
        this.text = text;
    }
    
    public Loop(JsonObject obj) {
        super((JsonArray) obj.get("content"));
        this.text = (String) obj.get("text");
    }
    
    public Loop() {
        super();
    }
    
    @Override
    public int getDefaultHitboxWidth() {
        return this.width - 20;
    }
    
    public List<Node> getContent() {
        return super.getThis();
    }
    
    public String toString() {
        return "Loop: " + this.text + " " + super.toString();
    }
    
    @Override
    public void setWidthFromChildWidth(int width, Node exclude) {
        super.setWidthFromChildWidth(width, exclude);
        if (this instanceof Node) {
//            ((Node) this).setWidth(width + 20);
            this.width = width + 20;
        } else {
            this.width = width + 20;
        }
    }
}
