package NSMaker;

import com.github.cliftonlabs.json_simple.JsonObject;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class RightBranch extends Branch {
    
    private int visualOffsetY = 0;
    private String oldText;
    
    public RightBranch() {
        super();
    }
    
    public RightBranch(String text, Decision parent) {
        super(text, parent);
    }
    
    public RightBranch(JsonObject obj, Decision parent) {
        super(obj, parent);
    }
    
    public RightBranch snapshot(Decision snapshotParent) {
        RightBranch snapshot = new RightBranch(this.text, snapshotParent);
        snapshot.visualOffsetY = this.visualOffsetY;
        snapshot.width = this.width;
        snapshot.addAll(super.snapshot());
        for (Node node : snapshot) {
            node.setParent(snapshot);
        }
        return snapshot;
    }
    
    @Override
    public int getVisualOffsetY() {
        return this.visualOffsetY;
    }
    
    @Override
    public Group draw() {
        return this.draw(false);
    }
    
    public Group draw(boolean excludeHitboxes) {
        SVGPath path = new SVGPath();
        path.setContent("m" + width + " 0 l0 70 l-" + width + " 0 z");
        path.setStroke(Color.BLACK);
        path.setStrokeWidth(1.0);
        path.setFill(Color.WHITE);
        double height = Math.max(parent.left.getContentHeight(), this.getContentHeight());
        SVGPath whitespace = new SVGPath();
        whitespace.setContent("m0 70 l" + width + " 0 l0 " + height + " l-" + width + " 0 z");
        whitespace.setStroke(Color.BLACK);
        whitespace.setStrokeWidth(1.0);
        whitespace.setFill(Color.WHITE);
        TextField text = new TextField(this.text);
        text.setPrefWidth((width*0.85)/2.0);
//        text.setStyle("-fx-background-color: #eee; -fx-background-insets: 0; -fx-background-radius: 0;");
        text.setLayoutY(38);
        text.setLayoutX(width - 5 - (width*0.85)/2.0);
        text.setAlignment(Pos.CENTER);
        text.textProperty().addListener(((observableValue, oldValue, newValue) -> this.text = newValue));
        text.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (newValue) {
                this.oldText = text.getText();
            } else {
                Main.getController().addCurrentStateToHistory(String.format("Right branch - name change (\"%s -> %s\")", this.oldText, this.text));
            }
        }));
        Group content = super.draw(excludeHitboxes);
        content.setLayoutY(70);
        Group drawGroup = new Group();
        drawGroup.setTranslateX(width);
        drawGroup.getChildren().addAll(path, text, whitespace);
        drawGroup.getChildren().add(content);
        drawGroup.setTranslateY(visualOffsetY);
        return drawGroup;
    }
    
    @Override
    public int getHitboxOffsetX() {
        return 10;
    }
}
