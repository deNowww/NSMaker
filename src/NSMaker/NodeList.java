package NSMaker;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import javafx.scene.Group;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class NodeList extends ArrayList<Node> {
    
//    public Node parent;
    private ArrayList<Hitbox> hitboxes = new ArrayList<>();
    
    public NodeList() {
        super();
    }
    
    public NodeList(JsonArray arr) {
        for (Object obj : arr) {
            JsonObject jsonObj = (JsonObject) obj;
            Node node;
            switch ((String) jsonObj.get("type")) {
                case "root":
                    node = new Root(jsonObj);
                    break;
                case "action":
                    node = new Action(jsonObj, this);
                    break;
                case "decision":
                    node = new Decision(jsonObj, this);
                    break;
                case "loop-top":
                    node = new TopLoop(jsonObj, this);
                    break;
                case "loop-bot":
                    node = new BotLoop(jsonObj, this);
                    break;
                default:
                    System.out.println("Fatal error while reading file: type '" + jsonObj.get("type") + "' does not exist.");
                    System.exit(1);
                    return; // only necessary to make the compiler happy
            }
            this.add(node);
        }
    }
    
    public static NodeList fromJsonArray(JsonArray arr, NodeList parent) {
        for (Object obj : arr) {
            JsonObject jsonObj = (JsonObject) obj;
            Node node;
            switch ((String) jsonObj.get("type")) {
                case "root":
                    node = new Root(jsonObj);
                    break;
                case "action":
                    node = new Action(jsonObj, parent);
                    break;
                case "decision":
                    node = new Decision(jsonObj, parent);
                    break;
                case "loop-top":
                    node = new TopLoop(jsonObj, parent);
                    break;
                case "loop-bot":
                    node = new BotLoop(jsonObj, parent);
                    break;
                default:
                    System.out.println("Fatal error while reading file: type '" + jsonObj.get("type") + "' does not exist.");
                    System.exit(1);
                    return null; // only necessary to make the compiler happy
            }
            parent.add(node);
        }
        return parent;
    }
    
    public List<Node> getThis() {
        return new ArrayList<>(this);
    }
    
    @Override
    public boolean add(Node e) {
        e.setParent(this);
        return super.add(e);
    }
    
    @Override
    public void add(int index, Node e) {
        e.setParent(this);
        super.add(index, e);
    }
    
    public void silentAdd(Node e) {
        super.add(e);
    }
    
    public NodeList snapshot() {
        NodeList snapshot = null;
        try {
            snapshot = this.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        for (Node node : this) {
            snapshot.add(node.snapshot(snapshot));
        }
        return snapshot;
    }
    
    public static double getTotalVisualOffsetY(Node node) {
        double total = node.getVisualOffsetY();
        Node nextParent = node;
        while (!((nextParent = (Node) nextParent.getParent()) instanceof Root)) {
            if (nextParent == null) {
                break; // cbf putting this in the one-liner custerfuck that is the while condition
            }
            total += nextParent.getVisualOffsetY();
        }
        return total;
    }
    
    public Group draw() {
        return this.draw(false);
    }
    
    // hoverbox has the wrong coordinates when it's the bottom one
    // seems to have coordinates relative to top left of the window
    
    public Group draw(boolean excludeHitboxes) {
        Group group = new Group();
    
        if (!excludeHitboxes) {
            hitboxes.clear();
            Hitbox initialHitbox = new Hitbox(0, this.getHitboxOffsetX() - 10, this.getHitboxOffsetY() - 10, this);
            if (this instanceof BotLoop) {
                initialHitbox.setTranslateY(-getTotalVisualOffsetY((Node) this)); // counter any translations on parents
            }
            hitboxes.add(initialHitbox);
        }
        
        int cy = 0;
        for (int i=0; i<this.size(); i++) {
            Node node = this.get(i);
            Group nodeGroup;
            if (node instanceof NodeList) {
                nodeGroup = ((NodeList) node).draw(excludeHitboxes);
            } else if (node instanceof Decision) {
                nodeGroup = ((Decision) node).draw(excludeHitboxes);
            } else {
                nodeGroup = node.draw();
            }
            nodeGroup.setLayoutY(cy);
            // deal with hitboxes fucking with the prefHeight
            if (node instanceof Decision) {
                cy -= 9;
            }
            if (node instanceof Loop) {
                cy -= 9;
            }
            cy += nodeGroup.prefHeight(0) - 2;
            group.getChildren().add(nodeGroup);
            
            if (!excludeHitboxes) {
                Hitbox hitbox = new Hitbox(i + 1, -10, cy - 10, this);
                hitboxes.add(hitbox);
            }
        }
    
        if (!excludeHitboxes) {
            int width;
            if (this.size() > 0 && !(this instanceof Branch)) {
                width = this.get(0).getWidth() + 20; // in theory, everything should be the same width
            } else {
                width = this.getDefaultHitboxWidth() + 20;
            }
            for (Hitbox hitbox : hitboxes) {
                group.getChildren().add(hitbox);
                hitbox.setWidthXY(width);
            }
        }
        
        return group;
    }
    
    
    // these should all be overriden by subclasses
    public int getHitboxOffsetX() {
        return 0;
    }
    public int getHitboxOffsetY() {
        return 0;
    }
    public int getDefaultHitboxWidth() {
        return 0;
    }
    
//    @Override
//    public List<Rectangle> getHitboxes() {
//        return null;
//    }
//
//    public NodeList removeFrom(int index) {
//        NodeList removed = new NodeList();
//
//        for (int i=index; i<this.size(); i+=0) {
//            removed.add(this.remove(index));
//        }
//
//        return removed;
//    }
//
    public JsonArray toJSONArray() {
        JsonArray content = new JsonArray();
        for (Node node : this) {
            content.add(((Jsonable)node).toJson());
        }
        return content;
    }
    
    public boolean equals(NodeList nl) {
        for (int i=0; i<this.size(); i++) {
            if (!this.get(i).equals(nl.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public int depth() {
        return this.depth(0); // who needs default params anyway, right?
    }
    
    public int depth(int base) {
        int depth = base;
        for (Node node : this) {
            if (node instanceof NodeList) {
                int newDepth = ((NodeList) node).depth(base + 1);
                if (newDepth > depth) {
                    depth = newDepth;
                }
            }
            if (node instanceof Decision) {
                int newDepth = ((Decision) node).depth(base);
                if (newDepth > depth) {
                    depth = newDepth;
                }
            }
        }
        return depth;
    }
    
    public ArrayList<Hitbox> getAllHitboxes() {
        ArrayList<Hitbox> all = new ArrayList<>(this.getHitboxes());
        for (Node node : this) {
            if (node instanceof NodeList) {
                all.addAll(((NodeList) node).getAllHitboxes());
            }
            if (node instanceof Decision) {
                all.addAll(((Decision) node).getAllHitboxes());
            }
        }
        return all;
    }
    
    public ArrayList<Hitbox> getHitboxes() {
        return this.hitboxes;
    }
    
    public String toString() {
        if (this.size() > 0) {
            StringBuilder sb = new StringBuilder("[ " + this.get(0).toString());
            for (int i=1; i<this.size(); i++) {
                sb.append(", ").append(this.get(i).toString());
            }
            sb.append(" ]");
            return sb.toString();
        }
        return "[  ]";
    }
    
    public Node getDeepestLargestNode() {
        Node deepest = null;
        int deepestPathLength = 0;
        
        for (Node node : this) {
            if (node instanceof NodeList) {
                Node newDeepest = ((NodeList) node).getDeepestLargestNode();
                if (newDeepest == null) {
                    newDeepest = node;
                }
                int newDeepestLength = newDeepest.getPath().size();
                if (deepest == null || newDeepestLength > deepestPathLength || (newDeepestLength == deepestPathLength && newDeepest.getDefaultWidth() > deepest.getDefaultWidth())) {
                    deepest = newDeepest;
                }
            } else if (node instanceof Decision) {
                Node newDeepest;
                Node deepestLeft = ((Decision) node).left.getDeepestLargestNode();
                Node deepestRight = ((Decision) node).right.getDeepestLargestNode();
                if (deepestLeft == null) {
                    if (deepestRight == null) { // l==null,r==null
                        newDeepest = node;
                    } else { // l==null,r!=null
                        newDeepest = deepestRight;
                    }
                } else {
                    if (deepestRight == null) { // l!=null,r==null
                        newDeepest = deepestLeft;
                    } else { // l!=null,r!=null
                        int leftSize = deepestLeft.getPath().size();
                        int rightSize = deepestRight.getPath().size();
                        if (leftSize == rightSize) {
                            if (deepestLeft.getDefaultWidth() > deepestRight.getDefaultWidth()) {
                                newDeepest = deepestLeft;
                            } else {
                                newDeepest = deepestRight;
                            }
                        } else if (leftSize > rightSize) {
                            newDeepest = deepestLeft;
                        } else { // r > l
                            newDeepest = deepestRight;
                        }
                    }
                }
                int newDeepestLength = newDeepest.getPath().size();
                if (deepest == null || newDeepestLength > deepestPathLength || (newDeepestLength == deepestPathLength && newDeepest.getDefaultWidth() > deepest.getDefaultWidth())) {
                    deepest = newDeepest;
                }
            } else {
                if (deepest == null || node.getPath().size() > deepestPathLength || (node.getPath().size() == deepestPathLength && node.getDefaultWidth() > deepest.getDefaultWidth())) {
                    deepest = node;
                }
            }
            deepestPathLength = deepest.getPath().size();
        }
        
        return deepest;
    }
    
    public boolean recursivelyContains(Node target) {
        if (target == null) {
            return false;
        }
        for (Node node : this) {
            if (node == target) {
                return true;
            }
            if (node instanceof NodeList) {
                return ((NodeList) node).recursivelyContains(target);
            }
        }
        return false;
    }
    
    public void setWidthFromChildWidth(int width, Node exclude) {
        System.out.println(String.format("exclude: %s", exclude));
        for (Node node : this) {
            if (node != exclude) {
                if (node instanceof NodeList) {
                    if (!((NodeList) node).recursivelyContains(exclude)) {
                        if (node instanceof Loop) {
                            ((Loop) node).setWidthFromChildWidth(width - 20, null); // we know that it doesn't contain it, no point checking each time
                        } else {
                            ((Loop) node).setWidthFromChildWidth(width, null);
                        }
                    }
                } else if (node instanceof Decision) {
                    ((Decision) node).setWidthFromChildWidth(width/2, exclude);
                } else {
                    node.setWidth(width);
                }
            }
        }
    }
}
