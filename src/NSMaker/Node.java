package NSMaker;

import com.github.cliftonlabs.json_simple.Jsonable;
import javafx.scene.Group;

import java.util.ArrayList;

public interface Node extends Jsonable {
    Group draw();
    void setParent(NodeList parent);
    NodeList getParent();
    int getDefaultWidth();
    int getWidth();
    void setWidth(int width);
    ArrayList<Node> getPath();
    void setVisualOffsetY(int offset);
    int getVisualOffsetY();
    Node snapshot(NodeList snapshotParent);
    String minimalToString();
}
