package NSMaker;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Hitbox extends Rectangle {
    private static final int HEIGHT = 20;
    public NodeList nodeListParent;
    private int relx;
    private int rely;
    public int index;
    private Timeline timeline = new Timeline();
    
    public Hitbox(int index, int relx, int rely, NodeList parent) {
        super();
        this.nodeListParent = parent;
        this.index = index;
        this.relx = relx;
        this.rely = rely;
        this.setFill(Color.BLACK);
        this.setOpacity(0.0);
        this.setPickOnBounds(false);
        
        this.setOnMouseEntered(event -> {
            this.setOpacity(0.5);
            timeline = new Timeline(new KeyFrame(Duration.seconds(1.0), actionEvent -> {
                System.out.println(String.format("timeline ended - %s", this.hashCode()));
                if (isHover()) {
                    separate(event);
                }
                timeline.stop();
            }));
            timeline.setCycleCount(1);
            timeline.play();
        });
        
        this.setOnMouseExited(event -> {
            this.setOpacity(0.0);
            timeline.stop();
        });
        
        this.setOnMouseClicked(event -> {
            Controller controller = Main.getController();
            Node holding = controller.getHolding();
            System.out.println("got clicked");
            if (holding != null) {
                if (holding instanceof Root) {
                    for (int i=0; i<((Root) holding).size(); i++) {
                        nodeListParent.add(index + i, ((Root) holding).get(i));
                    }
                } else {
                    nodeListParent.add(index, holding);
                }
                controller.removeHolding();
                controller.addCurrentStateToHistory();
                controller.updateEditor();
                event.consume();
            }
        });
    }
    
    public String toString() {
        return String.format("Hitbox %d of %s", index, nodeListParent);
    }
    
    public void separate(MouseEvent event) {
        System.out.println("hovered for 1 second - hitbox");
        Main.getController().separate(this, event);
    }
    
    public void setWidthXY(int width) {
        this.setX(relx);
        this.setY(rely);
        this.setWidth(width);
        this.setHeight(HEIGHT);
    
        // debug
        System.out.println(String.format("hitbox %d has x/y/w: %f/%f/%f", index, this.getX(), this.getY(), this.getWidth()));
    }
}
